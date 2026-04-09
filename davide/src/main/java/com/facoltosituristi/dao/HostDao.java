package com.facoltosituristi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.facoltosituristi.config.DatabaseConfig;
import com.facoltosituristi.model.Host;

public class HostDao {
    private static final Logger log = LoggerFactory.getLogger(HostDao.class);
    
    private String generaCodiceHost(long idUtente) {
        return "HOST-" + idUtente + "-" + System.currentTimeMillis();
    }
    
    public Host promoteToHost(long idUtente) {
        if (isHost(idUtente)) {
            log.warn("Utente ID {} è già un host", idUtente);
            return findById(idUtente);
        }
        
        String codiceHost = generaCodiceHost(idUtente);
        
        String sql = "INSERT INTO host (idUtente, codiceHost, isSuperHost, dataDiventatoSuper, totPrenotazioni) VALUES (?, ?, false, null, 0)";
        
        try (Connection conn = DatabaseConfig.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) { 
            
            pstmt.setLong(1, idUtente); 
            pstmt.setString(2, codiceHost);  
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                Host host = new Host(idUtente, codiceHost, false, null, 0);
                log.info("Utente ID {} promosso a Host nel DB con codice: {}", idUtente, codiceHost);
                return host;
            }
            throw new RuntimeException("Impossibile promuovere utente a host nel DB");
            
        } catch (SQLException e) {
            log.error("Errore nella promozione a host per utente ID {} nel DB: {}", idUtente, e.getMessage());
            throw new RuntimeException("Errore nella promozione a host", e);
        }
    }
    
    public boolean isHost(long idUtente) {
        String sql = "SELECT COUNT(*) FROM host WHERE idUtente = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, idUtente); 
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;  
            
        } catch (SQLException e) {
            log.error("Errore nel verificare se utente {} è host nel DB: {}", idUtente, e.getMessage());
            throw new RuntimeException("Errore nella verifica host", e);
        }
    }
    
    public Host findById(long idUtente) {
        String sql = "SELECT * FROM host WHERE idUtente = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, idUtente);  
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToHost(rs);
            }
            return null; 
            
        } catch (SQLException e) {
            log.error("Errore nel trovare host con ID utente {} nel DB: {}", idUtente, e.getMessage());
            throw new RuntimeException("Errore nel recupero dell'host", e);
        }
    }
    
    public Host findByCodiceHost(String codiceHost) {
        String sql = "SELECT * FROM host WHERE codiceHost = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, codiceHost); 
            ResultSet rs = pstmt.executeQuery();  
            
            if (rs.next()) {
                return mapResultSetToHost(rs); 
            }
            return null; 
            
        } catch (SQLException e) {
            log.error("Errore nel trovare host con codice {} nel DB: {}", codiceHost, e.getMessage());
            throw new RuntimeException("Errore nel recupero dell'host per codice", e);
        }
    }
    
    public List<Host> findAll() {
        List<Host> hosts = new ArrayList<>(); 
        String sql = "SELECT * FROM host ORDER BY idUtente"; 
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) { 
            
            while (rs.next()) {
                hosts.add(mapResultSetToHost(rs));
            }
            log.info("Trovati {} host nel DB", hosts.size());
            return hosts;  
            
        } catch (SQLException e) {
            log.error("Errore nel recupero di tutti gli host dal DB: {}", e.getMessage());
            throw new RuntimeException("Errore nel recupero degli host", e);
        }
    }
    
    public List<Host> findSuperHosts() {
        List<Host> superHosts = new ArrayList<>(); 
        String sql = "SELECT * FROM host WHERE isSuperHost = true ORDER BY idUtente";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                superHosts.add(mapResultSetToHost(rs));
            }
            log.info("Trovati {} super host nel DB", superHosts.size());
            return superHosts;
            
        } catch (SQLException e) {
            log.error("Errore nel recupero dei super host dal DB: {}", e.getMessage());
            throw new RuntimeException("Errore nel recupero dei super host", e);
        }
    }
    

    public void aggiornaContatorePrenotazioni(long idUtente) {
        String countSql = """
            SELECT COUNT(*) as totale 
            FROM prenotazione p 
            JOIN abitazione a ON p.idAbitazione = a.idAbitazione 
            WHERE a.idUtente = ? AND p.stato = 'COMPLETATA'
            """;
        
        String updateSql = "UPDATE host SET totPrenotazioni = ?, isSuperHost = ?, dataDiventatoSuper = ? WHERE idUtente = ?";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            
            int totPrenotazioni = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
                pstmt.setLong(1, idUtente);  
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    totPrenotazioni = rs.getInt("totale"); 
                }
            }
            
            boolean diventaSuperHost = totPrenotazioni >= 100; 
            java.sql.Date dataDiventatoSuper = null;
            
            if (diventaSuperHost) {
                String checkSuperHostSql = "SELECT isSuperHost FROM host WHERE idUtente = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(checkSuperHostSql)) {
                    pstmt.setLong(1, idUtente);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        boolean eraSuperHost = rs.getBoolean("isSuperHost");
                        if (!eraSuperHost) {
                            dataDiventatoSuper = new java.sql.Date(System.currentTimeMillis());
                            log.info("Host ID {} è diventato SUPER HOST! Prenotazioni COMPLETATE: {}", idUtente, totPrenotazioni);
                        }
                    }
                }
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, totPrenotazioni);  
                pstmt.setBoolean(2, diventaSuperHost);  
                
                if (dataDiventatoSuper != null) {
                    pstmt.setDate(3, dataDiventatoSuper);
                } else {
                    pstmt.setNull(3, java.sql.Types.DATE);
                }
                
                pstmt.setLong(4, idUtente); 
                pstmt.executeUpdate(); 
                
                log.debug("Aggiornato host ID {} nel DB: {} prenotazioni COMPLETATE, SuperHost: {}", idUtente, totPrenotazioni, diventaSuperHost);
            }
            
        } catch (SQLException e) {
            log.error("Errore nell'aggiornamento prenotazioni per host ID {} nel DB: {}", idUtente, e.getMessage());
            throw new RuntimeException("Errore nell'aggiornamento prenotazioni host", e);
        }
    }
    
    public List<Host> findHostsConPiuPrenotazioniUltimoMese() {
        List<Host> hosts = new ArrayList<>();
        String sql = """
            SELECT 
                h.idUtente, 
                h.codiceHost, 
                h.isSuperHost, 
                h.dataDiventatoSuper, 
                h.totPrenotazioni,
                COUNT(p.idPrenotazione) as prenotazioni_ultimo_mese
            FROM host h
            JOIN abitazione a ON h.idUtente = a.idUtente
            JOIN prenotazione p ON a.idAbitazione = p.idAbitazione
            WHERE p.dataInizioPrenotazione >= CURRENT_DATE - INTERVAL '1 month'
               AND p.stato = 'COMPLETATA'
            GROUP BY h.idUtente, h.codiceHost, h.isSuperHost, h.dataDiventatoSuper, h.totPrenotazioni
            ORDER BY prenotazioni_ultimo_mese DESC
            """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Host host = mapResultSetToHost(rs);
                hosts.add(host);
            }
            log.info("Trovati {} host con più prenotazioni COMPLETATE nell'ultimo mese nel DB", hosts.size());
            return hosts;
            
        } catch (SQLException e) {
            log.error("Errore nel trovare host con più prenotazioni ultimo mese nel DB: {}", e.getMessage());
            throw new RuntimeException("Errore nel recupero host con più prenotazioni", e);
        }
    }
    
    private Host mapResultSetToHost(ResultSet rs) throws SQLException {
        java.sql.Date sqlDate = rs.getDate("dataDiventatoSuper");
        java.time.LocalDate dataDiventatoSuper = (sqlDate != null) ? sqlDate.toLocalDate() : null;
        
        return new Host(
            rs.getLong("idUtente"),     
            rs.getString("codiceHost"),
            rs.getBoolean("isSuperHost"),
            dataDiventatoSuper,
            rs.getInt("totPrenotazioni") 
        );
    }
}
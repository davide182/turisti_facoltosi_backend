package com.facoltosituristi.service;

import java.util.List;

import com.facoltosituristi.dao.HostDao;
import com.facoltosituristi.model.Host;

public class HostService {
    private final HostDao hostDao = new HostDao();
    
    public Host promoteToHost(long idUtente) {
        if (idUtente <= 0) {
            throw new IllegalArgumentException("ID utente non valido");
        }
        
        return hostDao.promoteToHost(idUtente);
    }
    
    public boolean isHost(long idUtente) {
        if (idUtente <= 0) {
            throw new IllegalArgumentException("ID utente non valido");
        }
        
        return hostDao.isHost(idUtente);
    }
    
    public Host getHostById(long idUtente) {
        if (idUtente <= 0) {
            throw new IllegalArgumentException("ID utente non valido");
        }
        
        Host host = hostDao.findById(idUtente);
        if (host == null) {
            throw new RuntimeException("Host non trovato con ID utente: " + idUtente);
        }
        return host;
    }
    
    public Host getHostByCodice(String codiceHost) {
        if (codiceHost == null || codiceHost.isEmpty()) {
            throw new IllegalArgumentException("Codice host è obbligatorio");
        }
        
        Host host = hostDao.findByCodiceHost(codiceHost);
        if (host == null) {
            throw new RuntimeException("Host non trovato con codice: " + codiceHost);
        }
        return host;
    }
    
    public List<Host> getAllHosts() {
        return hostDao.findAll();
    }
    
    public List<Host> getAllSuperHosts() {
        return hostDao.findSuperHosts();
    }
    
    public boolean checkAndPromoteToSuperHost(long idUtente) {
        if (idUtente <= 0) {
            throw new IllegalArgumentException("ID utente non valido");
        }
        
        Host host = hostDao.findById(idUtente);
        if (host == null) {
            throw new RuntimeException("Host non trovato con ID utente: " + idUtente);
        }
        
        boolean eraSuperHost = host.isSuperHost();
        
        hostDao.aggiornaContatorePrenotazioni(idUtente);
        
        Host hostAggiornato = hostDao.findById(idUtente);
        boolean diventatoSuperHost = hostAggiornato != null && hostAggiornato.isSuperHost() && !eraSuperHost;
        
        return diventatoSuperHost;
    }
    
    public List<Host> getHostsConPiuPrenotazioniUltimoMese() {
        return hostDao.findHostsConPiuPrenotazioniUltimoMese();
    }
}
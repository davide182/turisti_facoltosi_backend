package com.facoltosituristi.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final Properties properties = new Properties();

    public static void loadConfig() {
        Properties props = new Properties();

        try (InputStream input = new FileInputStream("db.properties")) {
            
            props.load(input);
            log.info("File db.properties caricato con successo");

            Class.forName(props.getProperty("db.driver"));
            log.info("Driver PostgreSQL caricato con successo");
            
            properties.putAll(props);
            
        } catch (IOException | ClassNotFoundException e) {
            log.error("Errore nel caricamento del driver", e);
            throw new RuntimeException("Errore nella configurazione del database", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (properties.isEmpty()) { 
            loadConfig(); 
        }
        
        try {
            Connection conn = DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),    
                properties.getProperty("db.password")  
            );
            log.debug("Connessione al database stabilita");
            return conn;
        } catch (SQLException e) {
            log.error("Errore nell'stabilire la connessione al database", e);
            throw e;
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            log.info("Test connessione: SUCCESSO");
            return true;
        } catch (SQLException e) {
            log.error("Test connessione: FALLITO - {}", e.getMessage());
            return false;
        }
    }

    public static void resetConfig() {
        properties.clear();
        log.info("Configurazione database resettata");
    }
}
package com.facoltosituristi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.facoltosituristi.model.Abitazione;
import com.facoltosituristi.service.AbitazioneService;

import io.javalin.http.Context;

public class AbitazioneController {
    private static final Logger log = LoggerFactory.getLogger(AbitazioneController.class);
    private final AbitazioneService abitazioneService = new AbitazioneService();
    
    public AbitazioneController() {
    // costruttore vuoto
    }
    
    public void create(Context ctx) {
        try {
            Abitazione abitazione = ctx.bodyAsClass(Abitazione.class);
            Abitazione created = abitazioneService.createAbitazione(abitazione);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Abitazione creata con successo");
            response.put("data", created);
            response.put("id", created.getIdAbitazione());
            
            ctx.status(201).json(response);
            log.info("Abitazione creata via API: ID {} - '{}'", created.getIdAbitazione(), created.getNome());
            
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(createError("Validazione fallita", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore nella creazione dell'abitazione via API: {}", e.getMessage());
            ctx.status(500).json(createError("Errore interno", e.getMessage()));
        }
    }
    
    public void getById(Context ctx) {
        try {
            long id = Long.parseLong(ctx.pathParam("id"));
            Abitazione abitazione = abitazioneService.getAbitazioneById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", abitazione);
            
            ctx.json(response);
            log.info("Recuperata abitazione via API ID: {}", id);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json(createError("ID non valido", "L'ID deve essere un numero"));
        } catch (RuntimeException e) {
            ctx.status(404).json(createError("Abitazione non trovata", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore nel recupero abitazione via API: {}", e.getMessage());
            ctx.status(500).json(createError("Errore interno", e.getMessage()));
        }
    }
    
    public void getAll(Context ctx) {
        try {
            List<Abitazione> abitazioni = abitazioneService.getAllAbitazioni();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", abitazioni.size());
            response.put("data", abitazioni);
            
            ctx.json(response);
            log.info("Recuperate {} abitazioni via API", abitazioni.size());
            
        } catch (Exception e) {
            log.error("Errore nel recupero abitazioni via API: {}", e.getMessage());
            ctx.status(500).json(createError("Errore interno", e.getMessage()));
        }
    }
    
    public void getByUtente(Context ctx) {
        try {
            long idUtente = Long.parseLong(ctx.pathParam("idUtente"));
            List<Abitazione> abitazioni = abitazioneService.getAbitazioniByUtente(idUtente);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", abitazioni.size());
            response.put("data", abitazioni);
            
            ctx.json(response);
            log.info("Recuperate {} abitazioni per utente ID {} via API", abitazioni.size(), idUtente);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json(createError("ID non valido", "L'ID utente deve essere un numero"));
        } catch (Exception e) {
            log.error("Errore nel recupero abitazioni utente via API: {}", e.getMessage());
            ctx.status(500).json(createError("Errore interno", e.getMessage()));
        }
    }
    
    public void getByCodiceHost(Context ctx) {
        try {
            String codiceHost = ctx.pathParam("codiceHost");
            List<Abitazione> abitazioni = abitazioneService.getAbitazioniByCodiceHost(codiceHost);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", abitazioni.size());
            response.put("data", abitazioni);
            response.put("codiceHost", codiceHost);
            
            ctx.json(response);
            log.info("Recuperate {} abitazioni per host con codice {} via API", abitazioni.size(), codiceHost);
            
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(createError("Parametro non valido", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore nel recupero abitazioni per codice host via API: {}", e.getMessage());
            ctx.status(500).json(createError("Errore interno", e.getMessage()));
        }
    }
    
    public void update(Context ctx) {
        try {
            long id = Long.parseLong(ctx.pathParam("id"));
            Abitazione abitazione = ctx.bodyAsClass(Abitazione.class);
            Abitazione updated = abitazioneService.updateAbitazione(id, abitazione);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Abitazione aggiornata con successo");
            response.put("data", updated);
            
            ctx.json(response);
            log.info("Abitazione aggiornata via API ID: {}", id);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json(createError("ID non valido", "L'ID deve essere un numero"));
        } catch (RuntimeException e) {
            ctx.status(404).json(createError("Abitazione non trovata", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore nell'aggiornamento abitazione via API: {}", e.getMessage());
            ctx.status(500).json(createError("Errore interno", e.getMessage()));
        }
    }
    
    public void disable(Context ctx) {
        try {
            long id = Long.parseLong(ctx.pathParam("id"));
            boolean disabled = abitazioneService.disableAbitazione(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Abitazione disabilitata con successo");
            response.put("id", id);
            response.put("note", "L'abitazione è stata disabilitata (soft delete)");
            
            ctx.json(response);
            log.info("Abitazione disabilitata via API ID: {}", id);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json(createError("ID non valido", "L'ID deve essere un numero"));
        } catch (RuntimeException e) {
            ctx.status(404).json(createError("Abitazione non trovata", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore nella disabilitazione abitazione via API: {}", e.getMessage());
            ctx.status(500).json(createError("Errore interno", e.getMessage()));
        }
    }
    
    public void enable(Context ctx) {
        try {
            long id = Long.parseLong(ctx.pathParam("id"));
            boolean enabled = abitazioneService.enableAbitazione(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Abitazione abilitata con successo");
            response.put("id", id);
            
            ctx.json(response);
            log.info("Abitazione abilitata via API ID: {}", id);
            
        } catch (NumberFormatException e) {
            ctx.status(400).json(createError("ID non valido", "L'ID deve essere un numero"));
        } catch (RuntimeException e) {
            ctx.status(404).json(createError("Abitazione non trovata", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore nell'abilitazione abitazione via API: {}", e.getMessage());
            ctx.status(500).json(createError("Errore interno", e.getMessage()));
        }
    }
    
    public void getAbitazionePiuGettonataUltimoMese(Context ctx) {
        try {
            Abitazione abitazione = abitazioneService.getAbitazionePiuGettonataUltimoMese();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            
            if (abitazione != null) {
                response.put("data", abitazione);
                response.put("message", "Abitazione più gettonata nell'ultimo mese trovata");
                log.info("Abitazione più gettonata trovata via API: ID {}", abitazione.getIdAbitazione());
            } else {
                response.put("data", null);
                response.put("message", "Nessuna abitazione prenotata nell'ultimo mese");
                log.info("Nessuna abitazione prenotata nell'ultimo mese via API");
            }
            
            ctx.json(response);
            
        } catch (Exception e) {
            log.error("Errore nel trovare abitazione più gettonata via API: {}", e.getMessage());
            ctx.status(500).json(createError("Errore interno", e.getMessage()));
        }
    }
    
    public void getMediaPostiLetto(Context ctx) {
        try {
            double media = abitazioneService.getMediaPostiLetto();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("mediaPostiLetto", media);
            response.put("message", String.format("Media posti letto: %.2f", media));
            
            ctx.json(response);
            log.info("Media posti letto calcolata via API: {}", media);
            
        } catch (Exception e) {
            log.error("Errore nel calcolo media posti letto via API: {}", e.getMessage());
            ctx.status(500).json(createError("Errore interno", e.getMessage()));
        }
    }
    
    private Map<String, String> createError(String error, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        return errorResponse;
    }
}
package com.gestion.evenements.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gestion.evenements.exception.EvenementDejaExistantException;
import com.gestion.evenements.model.notification.EmailNotificationService;
import com.gestion.evenements.model.notification.NotificationService;

public class GestionEvenements {
    private static GestionEvenements instance;
    private final Map<String, Evenement> evenements;
    private NotificationService notificationService;

    private GestionEvenements() {
        this.evenements = new HashMap<>();
        this.notificationService = new EmailNotificationService();
    }

    public static synchronized GestionEvenements getInstance() {
        if (instance == null) {
            instance = new GestionEvenements();
        }
        return instance;
    }

    public void ajouterEvenement(Evenement evenement) throws EvenementDejaExistantException {
        if (evenements.containsKey(evenement.getId())) {
            throw new EvenementDejaExistantException("Événement avec l'ID " + evenement.getId() + " existe déjà");
        }
        evenements.put(evenement.getId(), evenement);
        notificationService.envoyerNotification("Nouvel événement créé: " + evenement.getNom());
    }

    public void supprimerEvenement(String id) {
        Evenement evenement = evenements.remove(id);
        if (evenement != null) {
            evenement.annuler();
        }
    }

    public Evenement rechercherEvenement(String id) {
        return evenements.get(id);
    }

    public List<Evenement> rechercherParLieu(String lieu) {
        return evenements.values().stream()
                .filter(e -> e.getLieu().toLowerCase().contains(lieu.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Evenement> getEvenementsFuturs() {
        return evenements.values().stream()
                .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Evenement::getDate))
                .collect(Collectors.toList());
    }

    public Map<String, Evenement> getEvenements() { return evenements; }
    public void setNotificationService(NotificationService service) { this.notificationService = service; }
}

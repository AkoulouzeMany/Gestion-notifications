package com.gestion.evenements.model.evenementparticulier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gestion.evenements.model.Evenement;
import com.gestion.evenements.model.Intervenant;

public class Conference extends Evenement {
    private String theme;
    private List<Intervenant> intervenants;

    public Conference() {
        super();
        this.intervenants = new ArrayList<>();
    }

    public Conference(String id, String nom, LocalDateTime date, String lieu, int capaciteMax, String theme) {
        super(id, nom, date, lieu, capaciteMax);
        this.theme = theme;
        this.intervenants = new ArrayList<>();
    }

    @Override
    public void annuler() {
        notifierObservateurs("Conférence annulée: " + nom + " - Thème: " + theme);
    }

    @Override
    public void afficherDetails() {
        System.out.println("=== CONFÉRENCE ===");
        System.out.println("ID: " + id);
        System.out.println("Nom: " + nom);
        System.out.println("Date: " + date);
        System.out.println("Lieu: " + lieu);
        System.out.println("Thème: " + theme);
        System.out.println("Capacité: " + participants.size() + "/" + capaciteMax);
        System.out.println("Intervenants: " + 
            intervenants.stream().map(Intervenant::getNom).collect(Collectors.joining(", ")));
    }

    // Getters et Setters
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public List<Intervenant> getIntervenants() { return intervenants; }
    public void setIntervenants(List<Intervenant> intervenants) { this.intervenants = intervenants; }
}

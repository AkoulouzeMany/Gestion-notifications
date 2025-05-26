package com.gestion.evenements.model.evenementparticulier;

import java.time.LocalDateTime;

import com.gestion.evenements.model.Evenement;

public class Concert extends Evenement{
      private String artiste;
    private String genreMusical;

    public Concert() {
        super();
    }

    public Concert(String id, String nom, LocalDateTime date, String lieu, int capaciteMax, 
                   String artiste, String genreMusical) {
        super(id, nom, date, lieu, capaciteMax);
        this.artiste = artiste;
        this.genreMusical = genreMusical;
    }

    @Override
    public void annuler() {
        notifierObservateurs("Concert annulé: " + nom + " - Artiste: " + artiste);
    }

    @Override
    public void afficherDetails() {
        System.out.println("=== CONCERT ===");
        System.out.println("ID: " + id);
        System.out.println("Nom: " + nom);
        System.out.println("Date: " + date);
        System.out.println("Lieu: " + lieu);
        System.out.println("Artiste: " + artiste);
        System.out.println("Genre: " + genreMusical);
        System.out.println("Capacité: " + participants.size() + "/" + capaciteMax);
    }

    // Getters et Setters
    public String getArtiste() { return artiste; }
    public void setArtiste(String artiste) { this.artiste = artiste; }
    public String getGenreMusical() { return genreMusical; }
    public void setGenreMusical(String genreMusical) { this.genreMusical = genreMusical; }

}

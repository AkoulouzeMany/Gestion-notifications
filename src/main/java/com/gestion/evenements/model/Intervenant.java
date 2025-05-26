package com.gestion.evenements.model;

public class Intervenant {
    private String nom;
    private String specialite;

    public Intervenant() {}

    public Intervenant(String nom, String specialite) {
        this.nom = nom;
        this.specialite = specialite;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
}

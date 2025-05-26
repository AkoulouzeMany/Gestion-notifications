package com.gestion.evenements.model;

import java.util.ArrayList;
import java.util.List;

public class Organisateur extends Participant {
     private List<Evenement> evenementsOrganises;

    public Organisateur() {
        super();
        this.evenementsOrganises = new ArrayList<>();
    }

    public Organisateur(String id, String nom, String email) {
        super(id, nom, email);
        this.evenementsOrganises = new ArrayList<>();
    }

    public void organiserEvenement(Evenement evenement) {
        evenementsOrganises.add(evenement);
    }

    public List<Evenement> getEvenementsOrganises() { return evenementsOrganises; }
    public void setEvenementsOrganises(List<Evenement> evenementsOrganises) { 
        this.evenementsOrganises = evenementsOrganises; 
    }

}

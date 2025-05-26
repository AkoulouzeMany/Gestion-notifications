package com.gestion.evenements.model;

import java.util.Objects;

import com.gestion.evenements.observer.ParticipantObserver;

public class Participant implements ParticipantObserver {
    protected String id;
    protected String nom;
    protected String email;

    public Participant() {}

    public Participant(String id, String nom, String email) {
        this.id = id;
        this.nom = nom;
        this.email = email;
    }

    @Override
    public void notifier(String message) {
        System.out.println("📧 Notification pour " + nom + " (" + email + "): " + message);
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Participant that = (Participant) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

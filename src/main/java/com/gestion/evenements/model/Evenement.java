package com.gestion.evenements.model;

import com.gestion.evenements.observer.EvenementObservable;
import com.gestion.evenements.observer.ParticipantObserver;
import com.gestion.evenements.exception.*;
import com.gestion.evenements.model.evenementparticulier.Conference;
import com.gestion.evenements.model.evenementparticulier.Concert;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Conference.class, name = "conference"),
    @JsonSubTypes.Type(value = Concert.class, name = "concert")
})
public abstract class Evenement implements EvenementObservable {
    protected String id;
    protected String nom;
    protected LocalDateTime date;
    protected String lieu;
    protected int capaciteMax;
    protected List<Participant> participants;
    public List<ParticipantObserver> observers;

    public Evenement() {
        this.participants = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public Evenement(String id, String nom, LocalDateTime date, String lieu, int capaciteMax) {
        this();
        this.id = id;
        this.nom = nom;
        this.date = date;
        this.lieu = lieu;
        this.capaciteMax = capaciteMax;
    }

    public void ajouterParticipant(Participant participant) throws CapaciteMaxAtteinteException {
        if (participants.size() >= capaciteMax) {
            throw new CapaciteMaxAtteinteException("Capacité maximale atteinte pour l'événement " + nom);
        }
        participants.add(participant);
        notifierObservateurs("Nouveau participant ajouté: " + participant.getNom());
    }

    public void retirerParticipant(Participant participant) {
        participants.remove(participant);
        notifierObservateurs("Participant retiré: " + participant.getNom());
    }

    public abstract void annuler();
    
    public abstract void afficherDetails();

    // Implémentation Observer Pattern
    @Override
    public void ajouterObservateur(ParticipantObserver observer) {
        observers.add(observer);
    }

    @Override
    public void retirerObservateur(ParticipantObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifierObservateurs(String message) {
        for (ParticipantObserver observer : observers) {
            observer.notifier(message);
        }
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }
    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) { this.participants = participants; }
}


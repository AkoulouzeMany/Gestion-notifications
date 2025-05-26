package com.gestion.evenements.observer;

public interface EvenementObservable {
    void ajouterObservateur(ParticipantObserver observer);
    void retirerObservateur(ParticipantObserver observer);
    void notifierObservateurs(String message);
}
package com.gestion.evenements.auth;

/**
 * Enumération des rôles d'utilisateur dans l'application
 */
public enum UserRole {
    PARTICIPANT("Participant", "Utilisateur qui participe aux événements"),
    ORGANISATEUR("Organisateur", "Utilisateur qui organise et gère des événements"),
    ADMINISTRATEUR("Administrateur", "Utilisateur avec accès complet au système");
    
    private final String displayName;
    private final String description;
    
    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}

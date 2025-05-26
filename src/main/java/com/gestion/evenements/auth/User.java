package com.gestion.evenements.auth;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestion.evenements.auth.UserRole;

/**
 * Modèle d'utilisateur pour l'authentification et la gestion des comptes
 * Contient les informations de base et les préférences utilisateur
 */
public class User {
    private String id;
    private String nom;
    private String email;
    private String motDePasse; // En production, ce serait haché
    private UserRole role;
    private String telephone;
    private String organisation;
    private LocalDateTime dateCreation;
    private LocalDateTime dernierAcces;
    private boolean actif;
    private String photoProfile;
    private String preferences;
    
    // Constructeur par défaut pour Jackson
    public User() {
        this.dateCreation = LocalDateTime.now();
        this.actif = true;
    }
    
    // Constructeur principal
    @JsonCreator
    public User(@JsonProperty("id") String id,
                @JsonProperty("nom") String nom,
                @JsonProperty("email") String email,
                @JsonProperty("motDePasse") String motDePasse,
                @JsonProperty("role") UserRole role,
                @JsonProperty("telephone") String telephone,
                @JsonProperty("organisation") String organisation) {
        this();
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.telephone = telephone;
        this.organisation = organisation;
    }
    
    // Constructeur simplifié
    public User(String id, String nom, String email, String motDePasse, UserRole role) {
        this(id, nom, email, motDePasse, role, null, null);
    }
    
    /**
     * Vérifie si le mot de passe fourni correspond à celui de l'utilisateur
     * En production, ceci utiliserait un hachage sécurisé
     */
    public boolean verifierMotDePasse(String motDePasse) {
        return this.motDePasse != null && this.motDePasse.equals(motDePasse);
    }
    
    /**
     * Met à jour la date de dernier accès
     */
    public void marquerAcces() {
        this.dernierAcces = LocalDateTime.now();
    }
    
    /**
     * Génère un nom d'affichage pour l'utilisateur
     */
    public String getNomAffichage() {
        if (organisation != null && !organisation.trim().isEmpty()) {
            return nom + " (" + organisation + ")";
        }
        return nom;
    }
    
    /**
     * Vérifie si l'utilisateur a le rôle spécifié
     */
    public boolean hasRole(UserRole role) {
        return this.role == role;
    }
    
    /**
     * Vérifie si l'utilisateur est un organisateur
     */
    public boolean isOrganisateur() {
        return role == UserRole.ORGANISATEUR;
    }
    
    /**
     * Vérifie si l'utilisateur est un participant
     */
    public boolean isParticipant() {
        return role == UserRole.PARTICIPANT;
    }
    
    /**
     * Vérifie si l'utilisateur est un administrateur
     */
    public boolean isAdministrateur() {
        return role == UserRole.ADMINISTRATEUR;
    }
    
    /**
     * Retourne une représentation sécurisée de l'utilisateur (sans mot de passe)
     */
    public User toSafeUser() {
        User safeUser = new User();
        safeUser.id = this.id;
        safeUser.nom = this.nom;
        safeUser.email = this.email;
        safeUser.role = this.role;
        safeUser.telephone = this.telephone;
        safeUser.organisation = this.organisation;
        safeUser.dateCreation = this.dateCreation;
        safeUser.dernierAcces = this.dernierAcces;
        safeUser.actif = this.actif;
        safeUser.photoProfile = this.photoProfile;
        safeUser.preferences = this.preferences;
        // Pas de motDePasse dans la version sécurisée
        return safeUser;
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getOrganisation() { return organisation; }
    public void setOrganisation(String organisation) { this.organisation = organisation; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDernierAcces() { return dernierAcces; }
    public void setDernierAcces(LocalDateTime dernierAcces) { this.dernierAcces = dernierAcces; }
    
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    
    public String getPhotoProfile() { return photoProfile; }
    public void setPhotoProfile(String photoProfile) { this.photoProfile = photoProfile; }
    
    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", organisation='" + organisation + '\'' +
                ", actif=" + actif +
                '}';
    }
}
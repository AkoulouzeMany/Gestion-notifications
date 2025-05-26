package com.gestion.evenements.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service d'authentification pour gérer les utilisateurs
 * Gère l'inscription, la connexion, et la persistance des données utilisateur
 */
public class AuthenticationService {
    private static final String USERS_FILE = "users.json";
    private static final String SESSIONS_FILE = "sessions.json";
    
    private Map<String, User> users; // Map: email -> User
    private Map<String, UserSession> activeSessions; // Map: sessionId -> UserSession
    private ObjectMapper objectMapper;
    
    // Singleton pattern
    private static AuthenticationService instance;
    
    public AuthenticationService() {
        this.users = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
        
        // Configuration Jackson pour JSON
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(
            com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false
        );
        
        // Charger les utilisateurs existants
        loadUsers();
        
        // Créer un utilisateur admin par défaut si aucun utilisateur n'existe
        createDefaultUsers();
    }
    
    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }
    
    /**
     * Enregistre un nouvel utilisateur
     */
    public boolean registerUser(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return false;
        }
        
        String email = user.getEmail().toLowerCase().trim();
        
        // Vérifier si l'email existe déjà
        if (users.containsKey(email)) {
            return false;
        }
        
        // Valider les données
        if (!validateUser(user)) {
            return false;
        }
        
        // Définir la date de création
        user.setDateCreation(LocalDateTime.now());
        user.setActif(true);
        
        // Ajouter l'utilisateur
        users.put(email, user);
        
        // Sauvegarder
        saveUsers();
        
        System.out.println("✅ Utilisateur enregistré: " + user.getNom() + " (" + user.getRole() + ")");
        return true;
    }
    
    /**
     * Authentifie un utilisateur
     */
    public UserSession authenticateUser(String email, String motDePasse) {
        if (email == null || motDePasse == null) {
            return null;
        }
        
        String emailKey = email.toLowerCase().trim();
        User user = users.get(emailKey);
        
        if (user == null || !user.isActif()) {
            return null;
        }
        
        if (user.verifierMotDePasse(motDePasse)) {
            // Créer une nouvelle session
            String sessionId = generateSessionId();
            UserSession session = new UserSession(sessionId, user.toSafeUser(), LocalDateTime.now());
            
            // Mettre à jour le dernier accès
            user.marquerAcces();
            
            // Stocker la session
            activeSessions.put(sessionId, session);
            
            // Sauvegarder les modifications
            saveUsers();
            
            System.out.println("✅ Connexion réussie pour: " + user.getNom());
            return session;
        }
        
        System.out.println("❌ Échec de connexion pour: " + email);
        return null;
    }
    
    /**
     * Déconnecte un utilisateur
     */
    public boolean logout(String sessionId) {
        UserSession session = activeSessions.remove(sessionId);
        if (session != null) {
            System.out.println("✅ Déconnexion réussie pour: " + session.getUser().getNom());
            return true;
        }
        return false;
    }
    
    /**
     * Vérifie si une session est valide
     */
    public boolean isValidSession(String sessionId) {
        UserSession session = activeSessions.get(sessionId);
        if (session == null) {
            return false;
        }
        
        // Vérifier l'expiration (session valide pendant 24h)
        LocalDateTime expiration = session.getDateConnexion().plusHours(24);
        if (LocalDateTime.now().isAfter(expiration)) {
            activeSessions.remove(sessionId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Récupère l'utilisateur d'une session
     */
    public User getUserFromSession(String sessionId) {
        if (!isValidSession(sessionId)) {
            return null;
        }
        
        UserSession session = activeSessions.get(sessionId);
        return session != null ? session.getUser() : null;
    }
    
    /**
     * Récupère un utilisateur par email
     */
    public User getUserByEmail(String email) {
        if (email == null) return null;
        return users.get(email.toLowerCase().trim());
    }
    
    /**
     * Met à jour un utilisateur
     */
    public boolean updateUser(User user) {
        if (user == null || user.getEmail() == null) {
            return false;
        }
        
        String email = user.getEmail().toLowerCase().trim();
        if (!users.containsKey(email)) {
            return false;
        }
        
        users.put(email, user);
        saveUsers();
        return true;
    }
    
    /**
     * Change le mot de passe d'un utilisateur
     */
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        User user = getUserByEmail(email);
        if (user == null || !user.verifierMotDePasse(oldPassword)) {
            return false;
        }
        
        user.setMotDePasse(newPassword);
        return updateUser(user);
    }
    
    /**
     * Désactive un utilisateur
     */
    public boolean deactivateUser(String email) {
        User user = getUserByEmail(email);
        if (user == null) {
            return false;
        }
        
        user.setActif(false);
        return updateUser(user);
    }
    
    /**
     * Récupère tous les utilisateurs (version sécurisée)
     */
    public List<User> getAllUsers() {
        return users.values().stream()
                .map(User::toSafeUser)
                .sorted((u1, u2) -> u1.getNom().compareToIgnoreCase(u2.getNom()))
                .toList();
    }
    
    /**
     * Récupère les utilisateurs par rôle
     */
    public List<User> getUsersByRole(UserRole role) {
        return users.values().stream()
                .filter(user -> user.getRole() == role)
                .map(User::toSafeUser)
                .sorted((u1, u2) -> u1.getNom().compareToIgnoreCase(u2.getNom()))
                .toList();
    }
    
    /**
     * Statistiques des utilisateurs
     */
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = users.size();
        long activeUsers = users.values().stream().filter(User::isActif).count();
        long participants = users.values().stream().filter(User::isParticipant).count();
        long organisateurs = users.values().stream().filter(User::isOrganisateur).count();
        long activeSessions = this.activeSessions.size();
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("participants", participants);
        stats.put("organisateurs", organisateurs);
        stats.put("activeSessions", activeSessions);
        
        return stats;
    }
    
    private boolean validateUser(User user) {
        if (user.getNom() == null || user.getNom().trim().isEmpty()) {
            return false;
        }
        
        if (user.getEmail() == null || !isValidEmail(user.getEmail())) {
            return false;
        }
        
        if (user.getMotDePasse() == null || user.getMotDePasse().length() < 6) {
            return false;
        }
        
        if (user.getRole() == null) {
            return false;
        }
        
        // Validation spécifique pour les organisateurs
        if (user.isOrganisateur() && 
            (user.getOrganisation() == null || user.getOrganisation().trim().isEmpty())) {
            return false;
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        return email != null && 
               email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
    
    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString() + "_" + System.currentTimeMillis();
    }
    
    private void createDefaultUsers() {
        if (users.isEmpty()) {
            // Créer un administrateur par défaut
            User admin = new User(
                "admin_default",
                "Administrateur",
                "admin@eventpro.com",
                "admin123",
                UserRole.ADMINISTRATEUR,
                "+33 1 23 45 67 89",
                "EventPro"
            );
            users.put(admin.getEmail().toLowerCase(), admin);
            
            // Créer un organisateur de démo
            User organisateur = new User(
                "org_demo",
                "Jean Dupont",
                "jean.dupont@eventpro.com",
                "demo123",
                UserRole.ORGANISATEUR,
                "+33 1 98 76 54 32",
                "EventPro"
            );
            users.put(organisateur.getEmail().toLowerCase(), organisateur);
            
            // Créer un participant de démo
            User participant = new User(
                "part_demo",
                "Marie Martin",
                "marie.martin@email.com",
                "demo123",
                UserRole.PARTICIPANT,
                "+33 6 12 34 56 78",
                null
            );
            users.put(participant.getEmail().toLowerCase(), participant);
            
            saveUsers();
            System.out.println("✅ Utilisateurs par défaut créés");
        }
    }
    
    private void loadUsers() {
        try {
            File file = new File(USERS_FILE);
            if (file.exists()) {
                Map<String, User> loadedUsers = objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, User.class));
                users.putAll(loadedUsers);
                System.out.println("✅ " + users.size() + " utilisateurs chargés");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Erreur lors du chargement des utilisateurs: " + e.getMessage());
            // Continuer avec une map vide
        }
    }
    
    private void saveUsers() {
        try {
            objectMapper.writeValue(new File(USERS_FILE), users);
            System.out.println("✅ Utilisateurs sauvegardés");
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la sauvegarde des utilisateurs: " + e.getMessage());
        }
    }
    
    /**
     * Classe interne pour gérer les sessions utilisateur
     */
    public static class UserSession {
        private String sessionId;
        private User user;
        private LocalDateTime dateConnexion;
        
        public UserSession() {}
        
        public UserSession(String sessionId, User user, LocalDateTime dateConnexion) {
            this.sessionId = sessionId;
            this.user = user;
            this.dateConnexion = dateConnexion;
        }
        
        // Getters et Setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
        
        public LocalDateTime getDateConnexion() { return dateConnexion; }
        public void setDateConnexion(LocalDateTime dateConnexion) { this.dateConnexion = dateConnexion; }
    }
}
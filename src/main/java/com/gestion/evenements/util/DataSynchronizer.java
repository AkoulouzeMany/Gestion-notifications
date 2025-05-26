package com.gestion.evenements.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.gestion.evenements.auth.AuthenticationService;
import com.gestion.evenements.auth.User;
import com.gestion.evenements.exception.EvenementDejaExistantException;
import com.gestion.evenements.model.Evenement;
import com.gestion.evenements.model.GestionEvenements;
import com.gestion.evenements.model.Participant;
import com.gestion.evenements.model.evenementparticulier.Concert;
import com.gestion.evenements.model.evenementparticulier.Conference;
import com.gestion.evenements.observer.ParticipantObserver;
import com.gestion.evenements.serialization.SerializationManager;

/**
 * Gestionnaire central de synchronisation des données avec sérialisation automatique
 * Utilise le pattern Singleton et Observer pour maintenir la cohérence des données
 * Intègre la sauvegarde automatique en JSON et XML
 */
public class DataSynchronizer {
    private static DataSynchronizer instance;
    
    // Services
    private GestionEvenements gestionEvenements;
    private AuthenticationService authService;
    
    // Observers globaux
    private List<ParticipantObserver> globalObservers;
    
    // Configuration de sauvegarde
    private static final String EVENTS_JSON_FILE = "evenements.json";
    private static final String EVENTS_XML_FILE = "evenements.xml";
    private static final String USERS_JSON_FILE = "users.json";
    private static final String BACKUP_DIR = "backups";
    
    // Sauvegarde automatique
    private ScheduledExecutorService scheduledExecutor;
    private boolean autoSaveEnabled = true;
    private int autoSaveIntervalMinutes = 5;
    
    // Statistiques système
    private SystemStats currentStats;
    private LocalDateTime lastSaveTime;
    private LocalDateTime lastLoadTime;
    
    private DataSynchronizer() {
        this.globalObservers = new ArrayList<>();
        this.gestionEvenements = GestionEvenements.getInstance();
        this.authService = AuthenticationService.getInstance();
        this.currentStats = new SystemStats();

        this.scheduledExecutor = Executors.newScheduledThreadPool(2);
        
        // Charger les données existantes
        loadAllData();
        
        // Initialiser des données de démonstration si nécessaire
        initializeDemoDataIfNeeded();
        
        // Démarrer la sauvegarde automatique
        startAutoSave();
        
        // Mettre à jour les statistiques
        updateSystemStats();
        
        System.out.println("✅ DataSynchronizer initialisé avec sérialisation automatique");
    }
    
    public static DataSynchronizer getInstance() {
        if (instance == null) {
            synchronized (DataSynchronizer.class) {
                if (instance == null) {
                    instance = new DataSynchronizer();
                }
            }
        }
        return instance;
    }
    
    // ================================
    // GESTION DES ÉVÉNEMENTS
    // ================================
    
    /**
     * Ajoute un événement avec synchronisation automatique
     */
    public void ajouterEvenementAvecSync(Evenement evenement) {
        if (evenement == null) return;
        
        try {

            // Vérifier si l'événement existe déjà
            if (gestionEvenements.getEvenements().containsKey(evenement.getId())) {
                throw new EvenementDejaExistantException("Un événement avec l'ID " + evenement.getId() + " existe déjà");
            }
            // Ajouter aux observers globaux
            for (ParticipantObserver observer : globalObservers) {
                evenement.ajouterObservateur(observer);
            }
            
            // Ajouter à la gestion centrale
            gestionEvenements.ajouterEvenement(evenement);
            
            // Notifier les observers
            notifierObserveursGlobaux("Nouvel événement ajouté: " + evenement.getNom());
            
            // Sauvegarder automatiquement
            /*if (autoSaveEnabled) {
                saveEventsAsync();
            }*/

            saveAllDataNow();
            
            // Mettre à jour les statistiques
            updateSystemStats();
            
            System.out.println("✅ Événement ajouté et synchronisé: " + evenement.getNom());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ajout de l'événement: " + e.getMessage());
            notifierObserveursGlobaux("Erreur lors de l'ajout de l'événement: " + e.getMessage());
        }
    }
    
    /**
     * Supprime un événement avec synchronisationmanyyymeg@gmailqaa
     */
    public void supprimerEvenementAvecSync(String evenementId) {
        try {
            Evenement evenement = gestionEvenements.rechercherEvenement(evenementId);
            if (evenement != null) {
                String nomEvenement = evenement.getNom();
                
                // Retirer les observers
                for (ParticipantObserver observer : globalObservers) {
                    evenement.retirerObservateur(observer);
                }
                
                // Supprimer de la gestion centrale
                gestionEvenements.supprimerEvenement(evenementId);
                
                // Notifier les observers
                notifierObserveursGlobaux("Événement supprimé: " + nomEvenement);
                
                // Sauvegarder automatiquement
                /*if (autoSaveEnabled) {
                    saveEventsAsync();
                }*/

                saveAllDataNow();
                
                // Mettre à jour les statistiques
                updateSystemStats();
                
                System.out.println("✅ Événement supprimé et synchronisé: " + nomEvenement);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression de l'événement: " + e.getMessage());
            notifierObserveursGlobaux("Erreur lors de la suppression: " + e.getMessage());
        }
    }
    
    /**
     * Met à jour un événement avec synchronisation
     */
    public void mettreAJourEvenementAvecSync(Evenement evenement) {
        if (evenement == null) return;
        
        try {
            // Vérifier que l'événement existe
            Evenement existant = gestionEvenements.rechercherEvenement(evenement.getId());
            if (existant != null) {
                // Mettre à jour dans la gestion centrale
                gestionEvenements.getEvenements().put(evenement.getId(), evenement);
                
                // Ajouter les observers si nécessaire
                for (ParticipantObserver observer : globalObservers) {
                    if (!evenement.observers.contains(observer)) {
                        evenement.ajouterObservateur(observer);
                    }
                }
                
                // Notifier les observers
                notifierObserveursGlobaux("Événement modifié: " + evenement.getNom());
                
                // Sauvegarder automatiquement
                /*if (autoSaveEnabled) {
                    saveEventsAsync();
                }*/

                saveAllDataNow();
                
                // Mettre à jour les statistiques
                updateSystemStats();
                
                System.out.println("✅ Événement mis à jour et synchronisé: " + evenement.getNom());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'événement: " + e.getMessage());
            notifierObserveursGlobaux("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

      /**
     * Sauvegarde immédiate et synchrone des données
     */
    public void saveAllDataNow() {
        try {
            System.out.println("💾 Sauvegarde immédiate des données...");
            
            // Sauvegarder les événements en JSON et XML
            Map<String, Evenement> evenements = gestionEvenements.getEvenements();
            
            // Créer les répertoires si nécessaire
            java.io.File jsonFile = new java.io.File(EVENTS_JSON_FILE);
            java.io.File xmlFile = new java.io.File(EVENTS_XML_FILE);
            
            if (jsonFile.getParentFile() != null) {
                jsonFile.getParentFile().mkdirs();
            }
            if (xmlFile.getParentFile() != null) {
                xmlFile.getParentFile().mkdirs();
            }
            
            // Sauvegarder en JSON
            SerializationManager.sauvegarderEvenementsJSON(evenements, EVENTS_JSON_FILE);
            System.out.println("✅ Fichier JSON créé: " + EVENTS_JSON_FILE);
            
            // Sauvegarder en XML
            SerializationManager.sauvegarderEvenementsXML(evenements, EVENTS_XML_FILE);
            System.out.println("✅ Fichier XML créé: " + EVENTS_XML_FILE);
            
            lastSaveTime = LocalDateTime.now();
            
            System.out.println("✅ " + evenements.size() + " événements sauvegardés immédiatement");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde immédiate: " + e.getMessage());
            e.printStackTrace();
            notifierObserveursGlobaux("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

       public GestionEvenements getGestionEvenements() {
        return gestionEvenements;
    }
    
    // ================================
    // GESTION DES OBSERVERS
    // ================================
    
    public void addGlobalObserver(ParticipantObserver observer) {
        if (observer != null && !globalObservers.contains(observer)) {
            globalObservers.add(observer);
            
            // Ajouter l'observer à tous les événements existants
            for (Evenement evenement : gestionEvenements.getEvenements().values()) {
                evenement.ajouterObservateur(observer);
            }
            
            System.out.println("✅ Observer global ajouté");
        }
    }
    
    public void removeGlobalObserver(ParticipantObserver observer) {
        if (observer != null && globalObservers.contains(observer)) {
            globalObservers.remove(observer);
            
            // Retirer l'observer de tous les événements
            for (Evenement evenement : gestionEvenements.getEvenements().values()) {
                evenement.retirerObservateur(observer);
            }
            
            System.out.println("✅ Observer global retiré");
        }
    }
    
    private void notifierObserveursGlobaux(String message) {
        for (ParticipantObserver observer : globalObservers) {
            try {
                observer.notifier(message);
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de la notification d'un observer: " + e.getMessage());
            }
        }
    }
    
    // ================================
    // SÉRIALISATION ET PERSISTANCE
    // ================================
    
    /**
     * Charge toutes les données depuis les fichiers
     */
    public void loadAllData() {
        try {
            System.out.println("🔄 Chargement des données...");
            
            // Charger les événements depuis JSON
            Map<String, Evenement> evenements = SerializationManager.chargerEvenementsJSON(EVENTS_JSON_FILE);
            
            // Si le fichier JSON n'existe pas, essayer XML
            if (evenements.isEmpty()) {
                try {
                    evenements = SerializationManager.chargerEvenementsXML(EVENTS_XML_FILE);
                } catch (Exception e) {
                    System.out.println("⚠️ Aucun fichier XML trouvé: " + e.getMessage());
                }
            }
            
            // Charger dans la gestion centrale
            for (Map.Entry<String, Evenement> entry : evenements.entrySet()) {
                gestionEvenements.getEvenements().put(entry.getKey(), entry.getValue());
                
                // Ajouter les observers globaux
                for (ParticipantObserver observer : globalObservers) {
                    entry.getValue().ajouterObservateur(observer);
                }
            }
            
            lastLoadTime = LocalDateTime.now();
            updateSystemStats();
            
            System.out.println("✅ " + evenements.size() + " événements chargés");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des données: " + e.getMessage());
        }
    }
    
    /**
     * Sauvegarde toutes les données
     */
    public void saveAllData() {
        saveAllDataNow();
    }
    
    /**
     * Sauvegarde asynchrone pour éviter de bloquer l'interface
     */
    private void saveEventsAsync() {
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.execute(this::saveAllData);
        }
    }
    
    /**
     * Export complet des données avec timestamp
     */
    public void exportCompleteBackup() {
        try {
            String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            // Export des événements
            SerializationManager.exporterEvenements(
                gestionEvenements.getEvenements(), 
                "evenements_backup_" + timestamp
            );
            
            // Export des utilisateurs
            Map<String, User> users = new HashMap<>();
            for (User user : authService.getAllUsers()) {
                users.put(user.getEmail(), user);
            }
            SerializationManager.exporterUtilisateurs(users, "utilisateurs_backup_" + timestamp);
            
            notifierObserveursGlobaux("Sauvegarde complète créée: " + timestamp);
            System.out.println("✅ Sauvegarde complète créée avec timestamp: " + timestamp);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'export complet: " + e.getMessage());
            notifierObserveursGlobaux("Erreur lors de la sauvegarde complète: " + e.getMessage());
        }
    }
    
    // ================================
    // SAUVEGARDE AUTOMATIQUE
    // ================================
    
    private void startAutoSave() {
        if (autoSaveEnabled && scheduledExecutor == null) {
            //scheduledExecutor = Executors.newScheduledThreadPool(2);
            
            // Sauvegarde automatique périodique
            scheduledExecutor.scheduleAtFixedRate(
                this::saveAllData,
                autoSaveIntervalMinutes,
                autoSaveIntervalMinutes,
                TimeUnit.MINUTES
            );
            
            // Sauvegarde de sécurité quotidienne
            scheduledExecutor.scheduleAtFixedRate(
                this::exportCompleteBackup,
                24, // Première exécution dans 24h
                24, // Puis toutes les 24h
                TimeUnit.HOURS
            );
            
            System.out.println("✅ Sauvegarde automatique activée (toutes les " + autoSaveIntervalMinutes + " minutes)");
        }
    }
    
    public void stopAutoSave() {
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {

            //Sauvegarder une derniere fois 

            saveAllDataNow();

            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("✅ Sauvegarde automatique arrêtée");
        }
    }
    
    // ================================
    // DONNÉES DE DÉMONSTRATION
    // ================================
    
    /**
     * Recharge les données de démonstration
     */
    public void reloadDemoData() {
        try {
            System.out.println("🔄 Rechargement des données de démonstration...");
            
            // Effacer les données existantes
            gestionEvenements.getEvenements().clear();
            
            // Créer de nouveaux événements de démonstration
            initializeDemoDataIfNeeded();
            
            // Sauvegarder immédiatement
            saveAllData();
            
            notifierObserveursGlobaux("Données de démonstration rechargées");
            System.out.println("✅ Données de démonstration rechargées");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du rechargement des données de démonstration: " + e.getMessage());
            notifierObserveursGlobaux("Erreur lors du rechargement: " + e.getMessage());
        }
    }
    
    private void initializeDemoDataIfNeeded() {
        if (gestionEvenements.getEvenements().isEmpty()) {
            System.out.println("🎯 Initialisation des données de démonstration...");
            
            try {
                // Créer des événements de démonstration
                Conference conf1 = new Conference(
                    "CONF_2025_001",
                    "Conférence Tech Innovation 2025",
                    LocalDateTime.now().plusDays(30),
                    "Centre de Conférences de Paris",
                    150,
                    "Intelligence Artificielle et Futur"
                );
                
                Concert concert1 = new Concert(
                    "CONC_2025_001",
                    "Festival Jazz Spring",
                    LocalDateTime.now().plusDays(45),
                    "Olympia, Paris",
                    800,
                    "Marcus Miller Trio",
                    "Jazz Fusion"
                );
                
                Conference conf2 = new Conference(
                    "CONF_2025_002",
                    "Séminaire Développement Durable",
                    LocalDateTime.now().plusDays(15),
                    "Palais des Congrès",
                    200,
                    "Écologie et Business"
                );
                
                Concert concert2 = new Concert(
                    "CONC_2025_002",
                    "Soirée Rock Classique",
                    LocalDateTime.now().plusDays(60),
                    "Zénith de Paris",
                    5000,
                    "The Legacy Band",
                    "Rock Classique"
                );
                
                // Créer des participants de démonstration
                Participant[] participants = {
                    new Participant("PART_001", "Alice Dubois", "alice.dubois@email.com"),
                    new Participant("PART_002", "Bob Martin", "bob.martin@email.com"),
                    new Participant("PART_003", "Claire Petit", "claire.petit@email.com"),
                    new Participant("PART_004", "David Moreau", "david.moreau@email.com"),
                    new Participant("PART_005", "Emma Bernard", "emma.bernard@email.com"),
                    new Participant("PART_006", "François Leroy", "francois.leroy@email.com")
                };
                
                // Ajouter les participants aux événements
                Random random = new Random();
                for (Participant participant : participants) {
                    // Inscrire aléatoirement les participants aux événements
                    if (random.nextBoolean()) {
                        try {
                            conf1.ajouterParticipant(participant);
                        } catch (Exception e) { /* Capacité peut être atteinte */ }
                    }
                    if (random.nextBoolean()) {
                        try {
                            concert1.ajouterParticipant(participant);
                        } catch (Exception e) { /* Capacité peut être atteinte */ }
                    }
                    if (random.nextBoolean()) {
                        try {
                            conf2.ajouterParticipant(participant);
                        } catch (Exception e) { /* Capacité peut être atteinte */ }
                    }
                    if (random.nextBoolean()) {
                        try {
                            concert2.ajouterParticipant(participant);
                        } catch (Exception e) { /* Capacité peut être atteinte */ }
                    }
                }
                
                // Ajouter les événements avec synchronisation
                ajouterEvenementAvecSync(conf1);
                ajouterEvenementAvecSync(concert1);
                ajouterEvenementAvecSync(conf2);
                ajouterEvenementAvecSync(concert2);
                
                System.out.println("✅ Données de démonstration initialisées avec succès");
                
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l'initialisation des données de démonstration: " + e.getMessage());
            }
        }
    }
    
    // ================================
    // STATISTIQUES SYSTÈME
    // ================================
    
    private void updateSystemStats() {
        try {
            Map<String, Evenement> evenements = gestionEvenements.getEvenements();
            
            currentStats.totalEvents = evenements.size();
            currentStats.totalParticipants = (int) evenements.values().stream()
                .flatMap(e -> e.getParticipants().stream())
                .distinct()
                .count();
            currentStats.totalInscriptions = evenements.values().stream()
                .mapToInt(e -> e.getParticipants().size())
                .sum();
            currentStats.activeEvents = (int) evenements.values().stream()
                .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
                .count();
                
            Map<String, Object> userStats = authService.getUserStatistics();
            currentStats.totalUsers = ((Long) userStats.get("totalUsers")).intValue();
            currentStats.activeUsers = ((Long) userStats.get("activeUsers")).intValue();
            currentStats.activeSessions = ((Long) userStats.get("activeSessions")).intValue();
            
            currentStats.lastUpdate = LocalDateTime.now();
            
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la mise à jour des statistiques: " + e.getMessage());
        }
    }
    
    public SystemStats getSystemStats() {
        updateSystemStats();
        return currentStats;
    }
    
    // ================================
    // CONFIGURATION
    // ================================
    
    public void setAutoSaveInterval(int minutes) {
        this.autoSaveIntervalMinutes = Math.max(1, minutes);
        
        // Redémarrer la sauvegarde automatique avec le nouvel intervalle
        if (autoSaveEnabled) {
            stopAutoSave();
            startAutoSave();
        }
        
        System.out.println("✅ Intervalle de sauvegarde automatique mis à jour: " + minutes + " minutes");
    }
    
    public void setAutoSaveEnabled(boolean enabled) {
        this.autoSaveEnabled = enabled;
        
        if (enabled) {
            startAutoSave();
        } else {
            stopAutoSave();
        }
        
        System.out.println("✅ Sauvegarde automatique " + (enabled ? "activée" : "désactivée"));
    }
    
    // ================================
    // NETTOYAGE
    // ================================
    
    public void shutdown() {
        System.out.println("🛑 Arrêt du DataSynchronizer...");
        
        // Sauvegarder une dernière fois
        saveAllData();
        
        // Arrêter la sauvegarde automatique
        stopAutoSave();
        
        // Nettoyer les observers
        globalObservers.clear();
        
        System.out.println("✅ DataSynchronizer arrêté proprement");
    }

    public void cleanup() {
    try {
        System.out.println("🛑 Nettoyage du DataSynchronizer...");
        
        // Nettoyer les observers
        if (globalObservers != null) {
            globalObservers.clear();
        }
        
        // Sauvegarder une dernière fois si nécessaire
        try {
            //saveSimpleBackup();
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la sauvegarde finale: " + e.getMessage());
        }
        
        System.out.println("✅ DataSynchronizer nettoyé");
        
    } catch (Exception e) {
        System.err.println("❌ Erreur lors du nettoyage: " + e.getMessage());
    }
}
    
    // ================================
    // CLASSE INTERNE POUR LES STATISTIQUES
    // ================================
    
    public static class SystemStats {
        private int totalEvents = 0;
        private int activeEvents = 0;
        private int totalParticipants = 0;
        private int totalInscriptions = 0;
        private int totalUsers = 0;
        private int activeUsers = 0;
        private int activeSessions = 0;
        private LocalDateTime lastUpdate;
        
        // Getters
        public int getTotalEvents() { return totalEvents; }
        public int getActiveEvents() { return activeEvents; }
        public int getTotalParticipants() { return totalParticipants; }
        public int getTotalInscriptions() { return totalInscriptions; }
        public int getTotalUsers() { return totalUsers; }
        public int getActiveUsers() { return activeUsers; }
        public int getActiveSessions() { return activeSessions; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        
        @Override
        public String toString() {
            return String.format(
                "SystemStats{events=%d, activeEvents=%d, participants=%d, inscriptions=%d, users=%d, sessions=%d}",
                totalEvents, activeEvents, totalParticipants, totalInscriptions, totalUsers, activeSessions
            );
        }
    }
}
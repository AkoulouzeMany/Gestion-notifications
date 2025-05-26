package com.gestion.evenements;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.gestion.evenements.ui.GestionEvenementsApp;
import com.gestion.evenements.ui.ParticipantApp;
import com.gestion.evenements.ui.WelcomeView;
import com.gestion.evenements.ui.OrganisateurApp;
import com.gestion.evenements.util.DataSynchronizer;

/**
 * Lanceur principal pour l'application de gestion d'événements
 * Permet de choisir entre les différentes interfaces utilisateur
 */
public class ApplicationLauncher extends Application {
    
    private Stage primaryStage;
    private DataSynchronizer dataSynchronizer;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialiser le système de données
        dataSynchronizer = DataSynchronizer.getInstance();
        
        // Configuration de la fenêtre de sélection
        primaryStage.setTitle("Gestion d'Événements - Sélection de l'interface");
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(500);
        //primaryStage.setResizable(false);
        
        
        // Création de l'interface de sélection
        Scene scene = createSelectionScene();
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Afficher les informations système
        showSystemInfo();
    }
    
    private void loadFonts() {
        Font font = Font.loadFont(
            getClass().getResourceAsStream("/com/gestion/evenements/ui/fonts/Poppins-Regular.ttf"), 12
        );
        
        if (font == null) {
            System.out.println("⚠️ Police Poppins non trouvée, utilisation de la police par défaut");
        } else {
            System.out.println("✅ Police Poppins chargée avec succès");
        }
    }
    
    private Scene createSelectionScene() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(30);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("main-container");
        
        // Header avec logo et titre
        VBox header = createHeader();
        
        // Statistiques système
        VBox systemStats = createSystemStats();
        
        // Sélection des interfaces
        VBox interfaceSelection = createInterfaceSelection();
        
        // Footer avec informations
        VBox footer = createFooter();
        
        root.getChildren().addAll(header, systemStats, interfaceSelection, footer);
        
        Scene scene = new Scene(root, 1080, 720);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        
        return scene;
    }
    
    private VBox createHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(15);
        
        // Icône principale
        Label mainIcon = new Label("🎯");
        mainIcon.setStyle("-fx-font-size: 64px;");
        
        // Titre principal
        Label mainTitle = new Label("Gestion d'Événements");
        mainTitle.getStyleClass().add("content-title");
        mainTitle.setStyle("-fx-font-size: 32px;");
        
        // Sous-titre
        Label subtitle = new Label("Système connecté avec Pattern Observer");
        subtitle.getStyleClass().add("content-subtitle");
        subtitle.setStyle("-fx-font-size: 16px; -fx-opacity: 0.8;");
        
        // Version
        Label version = new Label("Version 2.0 - Temps Réel");
        version.getStyleClass().add("text-secondary");
        version.setStyle("-fx-font-size: 12px;");
        
        header.getChildren().addAll(mainIcon, mainTitle, subtitle, version);
        return header;
    }
    
    private VBox createSystemStats() {
        VBox statsContainer = new VBox();
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.setSpacing(15);
        
        Label statsTitle = new Label("État du système");
        statsTitle.getStyleClass().add("section-title");
        statsTitle.setStyle("-fx-font-size: 18px;");
        
        HBox statsBar = new HBox();
        statsBar.setAlignment(Pos.CENTER);
        statsBar.setSpacing(20);
        
        // Récupérer les statistiques en temps réel
        DataSynchronizer.SystemStats stats = dataSynchronizer.getSystemStats();
        
        VBox eventsCard = createStatCard(String.valueOf(stats.getTotalEvents()), "Événements", "total-stat");
        VBox participantsCard = createStatCard(String.valueOf(stats.getTotalParticipants()), "Participants", "info-stat");
        VBox inscriptionsCard = createStatCard(String.valueOf(stats.getTotalInscriptions()), "Inscriptions", "positive-stat");
        //VBox futureCard = createStatCard(String.valueOf(stats.getFutureEvents()), "À venir", "success-stat");
        
        statsBar.getChildren().addAll(eventsCard, participantsCard, inscriptionsCard/* ,futureCard*/);
        
        statsContainer.getChildren().addAll(statsTitle, statsBar);
        return statsContainer;
    }
    
    private VBox createStatCard(String value, String label, String styleClass) {
        VBox card = new VBox();
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setAlignment(Pos.CENTER);
        card.setSpacing(8);
        card.setPrefWidth(120);
        card.setPrefHeight(80);
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setStyle("-fx-font-size: 20px;");
        
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("stat-label");
        labelLabel.setStyle("-fx-font-size: 10px;");
        
        card.getChildren().addAll(valueLabel, labelLabel);
        return card;
    }
    
    private VBox createInterfaceSelection() {
        VBox selection = new VBox();
        selection.setAlignment(Pos.CENTER);
        selection.setSpacing(20);
        
        Label selectionTitle = new Label("Choisir votre interface");
        selectionTitle.getStyleClass().add("section-title");
        selectionTitle.setStyle("-fx-font-size: 20px;");
        
        HBox interfacesRow = new HBox();
        interfacesRow.setAlignment(Pos.CENTER);
        interfacesRow.setSpacing(30);
        
        // Interface Administrateur
        VBox adminInterface = createInterfaceCard(
            "👨‍💼", 
            "Administrateur", 
            "Interface complète\nGestion globale\nTous les droits",
            "Interface administrative avec accès complet aux événements, participants et organisateurs",
            () -> launchAdminInterface()
        );
        
        // Interface Organisateur
        VBox organizerInterface = createInterfaceCard(
            "📋", 
            "Organisateur", 
            "Créer événements\nGérer participants\nVoir rapports",
            "Interface dédiée aux organisateurs pour créer et gérer leurs événements",
            () -> launchOrganizerInterface()
        );
        
        // Interface Participant
        VBox participantInterface = createInterfaceCard(
            "🎫", 
            "Participant", 
            "S'inscrire\nVoir événements\nSuivre inscriptions",
            "Interface pour les participants pour découvrir et s'inscrire aux événements",
            () -> launchParticipantInterface()
        );
        
        interfacesRow.getChildren().addAll(adminInterface, organizerInterface, participantInterface);
        
        selection.getChildren().addAll(selectionTitle, interfacesRow);
        return selection;
    }
    
    private VBox createInterfaceCard(String icon, String title, String features, String description, Runnable action) {
        VBox card = new VBox();
        card.getStyleClass().add("organizer-card");
        card.setAlignment(Pos.CENTER);
        card.setSpacing(12);
        card.setPrefWidth(160);
        card.setPrefHeight(220);
        card.setCursor(javafx.scene.Cursor.HAND);
        
        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 40px;");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("organizer-name");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600;");
        
        Label featuresLabel = new Label(features);
        featuresLabel.getStyleClass().add("organizer-role");
        featuresLabel.setStyle("-fx-font-size: 11px; -fx-text-alignment: center;");
        featuresLabel.setWrapText(true);
        
        Button launchBtn = new Button("Lancer");
        launchBtn.getStyleClass().add("primary-button-small");
        launchBtn.setOnAction(e -> action.run());
        
        card.getChildren().addAll(iconLabel, titleLabel, featuresLabel, launchBtn);
        
        // Tooltip avec description
        Tooltip tooltip = new Tooltip(description);
        Tooltip.install(card, tooltip);
        
        return card;
    }
    
    private VBox createFooter() {
        VBox footer = new VBox();
        footer.setAlignment(Pos.CENTER);
        footer.setSpacing(10);
        
        Label techInfo = new Label("Technologies: JavaFX • Pattern Observer • Material Design");
        techInfo.getStyleClass().add("text-secondary");
        techInfo.setStyle("-fx-font-size: 11px;");
        
        HBox buttonsRow = new HBox();
        buttonsRow.setAlignment(Pos.CENTER);
        buttonsRow.setSpacing(15);
        
        Button reloadBtn = new Button("🔄 Recharger données");
        reloadBtn.getStyleClass().add("secondary-button");
        reloadBtn.setStyle("-fx-font-size: 11px; -fx-padding: 6px 12px;");
        reloadBtn.setOnAction(e -> reloadSystemData());
        
        Button aboutBtn = new Button("ℹ️ À propos");
        aboutBtn.getStyleClass().add("secondary-button");
        aboutBtn.setStyle("-fx-font-size: 11px; -fx-padding: 6px 12px;");
        aboutBtn.setOnAction(e -> showAbout());
        
        Button exitBtn = new Button("❌ Quitter");
        exitBtn.getStyleClass().add("icon-button-danger");
        exitBtn.setStyle("-fx-font-size: 11px; -fx-padding: 6px 12px;");
        exitBtn.setOnAction(e -> exitApplication());
        
        buttonsRow.getChildren().addAll(reloadBtn, aboutBtn, exitBtn);
        
        footer.getChildren().addAll(techInfo, buttonsRow);
        return footer;
    }
    
    private void launchAdminInterface() {
        try {
            // Fermer la fenêtre de sélection
            primaryStage.close();
            
            // Lancer l'interface administrateur
            GestionEvenementsApp adminApp = new GestionEvenementsApp();
            Stage adminStage = new Stage();
            adminApp.start(adminStage);
            
            System.out.println("🚀 Interface Administrateur lancée");
            
        } catch (Exception e) {
            showError("Erreur lors du lancement de l'interface administrateur", e);
        }
    }
    
    private void launchOrganizerInterface() {
        try {
            // Fermer la fenêtre de sélection
            primaryStage.close();
            
            // Lancer l'interface organisateur
            OrganisateurApp organizerApp = new OrganisateurApp();
            Stage organizerStage = new Stage();
            organizerApp.start(organizerStage);
            
            System.out.println("🚀 Interface Organisateur lancée");
            
        } catch (Exception e) {
            showError("Erreur lors du lancement de l'interface organisateur", e);
        }
    }
    
    private void launchParticipantInterface() {
        try {
            // Fermer la fenêtre de sélection
            primaryStage.close();
            
            // Lancer l'interface participant
            ParticipantApp participantApp = new ParticipantApp();
            Stage participantStage = new Stage();
            participantApp.start(participantStage);
            
            System.out.println("🚀 Interface Participant lancée");
            
        } catch (Exception e) {
            showError("Erreur lors du lancement de l'interface participant", e);
        }
    }
    
    private void reloadSystemData() {
        try {
            dataSynchronizer.reloadDemoData();
            
            // Rafraîchir la scène
            Scene newScene = createSelectionScene();
            primaryStage.setScene(newScene);
            
            showSuccess("Données système rechargées avec succès");
            
        } catch (Exception e) {
            showError("Erreur lors du rechargement des données", e);
        }
    }
    
    private void showAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("À propos");
        about.setHeaderText("Système de Gestion d'Événements");
        
        StringBuilder content = new StringBuilder();
        content.append("Version 2.0 - Connecté en Temps Réel\n\n");
        content.append("ARCHITECTURE:\n");
        content.append("• Pattern Observer pour la synchronisation\n");
        content.append("• JavaFX pour l'interface utilisateur\n");
        content.append("• Material Design pour le style\n");
        content.append("• Gestion en temps réel des données\n\n");
        
        content.append("INTERFACES DISPONIBLES:\n");
        content.append("• Administrateur: Gestion complète du système\n");
        content.append("• Organisateur: Création et gestion d'événements\n");
        content.append("• Participant: Inscription et suivi des événements\n\n");
        
        content.append("FONCTIONNALITÉS:\n");
        content.append("• Synchronisation en temps réel\n");
        content.append("• Notifications automatiques\n");
        content.append("• Interface responsive et moderne\n");
        content.append("• Gestion des participants et organisateurs\n");
        content.append("• Rapports et statistiques\n\n");
        
        DataSynchronizer.SystemStats stats = dataSynchronizer.getSystemStats();
        content.append("ÉTAT ACTUEL DU SYSTÈME:\n");
        content.append("• Événements: ").append(stats.getTotalEvents()).append("\n");
        content.append("• Participants: ").append(stats.getTotalParticipants()).append("\n");
        content.append("• Inscriptions: ").append(stats.getTotalInscriptions()).append("\n");
       // content.append("• Événements à venir: ").append(stats.getFutureEvents()).append("\n");
        
        about.setContentText(content.toString());
        about.showAndWait();
    }
    
    private void exitApplication() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Quitter l'application");
        confirmation.setHeaderText("Confirmer la fermeture");
        confirmation.setContentText("Êtes-vous sûr de vouloir quitter l'application ?\n" +
                                   "Toutes les données en cours seront conservées.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("👋 Fermeture de l'application");
                javafx.application.Platform.exit();
                System.exit(0);
            }
        });
    }
    
    private void showSystemInfo() {
        DataSynchronizer.SystemStats stats = dataSynchronizer.getSystemStats();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🎯 SYSTÈME DE GESTION D'ÉVÉNEMENTS - DÉMARRAGE");
        System.out.println("=".repeat(50));
        System.out.println("📊 État initial du système:");
        System.out.println("   • Événements chargés: " + stats.getTotalEvents());
        System.out.println("   • Participants: " + stats.getTotalParticipants());
        System.out.println("   • Inscriptions totales: " + stats.getTotalInscriptions());
       //System.out.println("   • Événements à venir: " + stats.getFutureEvents());
        System.out.println("\n🔧 Pattern Observer: Actif ✅");
        System.out.println("🎨 Interface: Material Design ✅");
        System.out.println("⚡ Synchronisation: Temps réel ✅");
        System.out.println("\n💡 Interfaces disponibles:");
        System.out.println("   • Administrateur: Gestion complète");
        System.out.println("   • Organisateur: Création d'événements");
        System.out.println("   • Participant: Inscription aux événements");
        System.out.println("=".repeat(50) + "\n");
    }
    
    private void showError(String message, Exception e) {
        System.err.println("❌ Erreur: " + message);
        if (e != null) {
            System.err.println("   Détails: " + e.getMessage());
        }
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);
        alert.setContentText(e != null ? e.getMessage() : "Une erreur inattendue s'est produite");
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        System.out.println("✅ " + message);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Lancement du système de gestion d'événements...");
        //launch(args);
        Application.launch(WelcomeView.class, args);
    }
}
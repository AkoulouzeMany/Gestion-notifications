package com.gestion.evenements.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

/**
 * Interface de bienvenue et présentation de l'application
 * Page d'accueil avec présentation des fonctionnalités et boutons de connexion/inscription
 * Point d'entrée principal de l'application
 */
public class WelcomeView extends Application {

    private BorderPane mainLayout;
    private VBox centerContent;
    private Label titleLabel;
    private Label subtitleLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gestion d'Événements Professionnelle");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        createWelcomeInterface();
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void createWelcomeInterface() {
        mainLayout = new BorderPane();
        
        // Header avec navigation
        HBox header = createHeader();
        
        // Contenu central
        centerContent = createCenterContent();
        
        // Assemblage avec la section fonctionnalités intégrée
        VBox fullCenter = new VBox();
        fullCenter.getChildren().addAll(centerContent);
        
        mainLayout.setTop(header);
        mainLayout.setCenter(fullCenter);
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 40, 20, 40));
        header.setSpacing(20);
        
        // Logo et nom de l'application
        HBox logo = new HBox();
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setSpacing(15);
        
        Label logoIcon = new Label("🎯");
        
        VBox logoText = new VBox();
        logoText.setSpacing(2);
        
        Label appName = new Label("Gestion d'Événements ");
        
        logoText.getChildren().addAll(appName);
        logo.getChildren().addAll(logoIcon, logoText);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Boutons de navigation
        HBox navButtons = new HBox();
        navButtons.setSpacing(15);
        navButtons.setAlignment(Pos.CENTER_RIGHT);
        
        Button loginBtn = new Button("Se connecter");
        loginBtn.setOnAction(e -> openLoginView());
        
        Button registerBtn = new Button("S'inscrire");
        registerBtn.setOnAction(e -> openRegisterView());
        
        navButtons.getChildren().addAll(loginBtn, registerBtn);
        
        header.getChildren().addAll(logo, spacer, navButtons);
        return header;
    }
    
    private VBox createCenterContent() {
        VBox center = new VBox();
        center.setAlignment(Pos.CENTER);
        center.setSpacing(40);
        center.setPadding(new Insets(60, 40, 40, 40));
        
        // Titre principal
        titleLabel = new Label("Gérez vos événements en temps réel");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(800);
        
        // Boutons d'action principaux
        HBox actionButtons = createMainActionButtons();
        
        center.getChildren().addAll(titleLabel, actionButtons);
        return center;
    }
    
    private HBox createMainActionButtons() {
        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(20);
        
        Button startBtn = new Button("Commencer");
        startBtn.setOnAction(e -> openLoginView());
        
        Button organizerBtn = new Button("Espace Organisateur");
        organizerBtn.setOnAction(e -> {
            Alert info = createInfoAlert("Espace Organisateur", 
                "Connectez-vous avec un compte organisateur pour :\n\n" +
                "• Créer et gérer vos événements\n" +
                "• Suivre vos participants en temps réel\n" +
                "• Générer des rapports détaillés\n" +
                "• Synchronisation automatique des données");
            info.showAndWait();
            openLoginView();
        });
        
        Button participantBtn = new Button("👤 Espace Participant");
        participantBtn.setOnAction(e -> {
            Alert info = createInfoAlert("Espace Participant",
                "Connectez-vous avec un compte participant pour :\n\n" +
                "• Découvrir les événements disponibles\n" +
                "• S'inscrire en temps réel\n" +
                "• Suivre vos inscriptions\n" +
                "• Recevoir des notifications automatiques");
            info.showAndWait();
            openLoginView();
        });
        
        buttons.getChildren().addAll(startBtn, organizerBtn, participantBtn);
        return buttons;
    }
    
    // Actions des boutons - Connexion au système
    private void openLoginView() {
        try {
            // Fermer la fenêtre de bienvenue
            Stage currentStage = (Stage) mainLayout.getScene().getWindow();
            currentStage.close();
            
            // Ouvrir la vue de connexion
            Stage loginStage = new Stage();
            LoginView loginView = new LoginView();
            loginView.start(loginStage);
            
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la connexion", e.getMessage());
        }
    }
    
    private void openRegisterView() {
        try {
            // Fermer la fenêtre de bienvenue
            Stage currentStage = (Stage) mainLayout.getScene().getWindow();
            currentStage.close();
            
            // Ouvrir la vue d'inscription
            Stage registerStage = new Stage();
            RegisterView registerView = new RegisterView();
            registerView.start(registerStage);
            
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de l'inscription", e.getMessage());
        }
    }
    
    private Alert createInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setPrefWidth(500);
        return alert;
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
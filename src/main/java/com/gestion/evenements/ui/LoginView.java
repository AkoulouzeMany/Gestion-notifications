package com.gestion.evenements.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import com.gestion.evenements.auth.AuthenticationService;
import com.gestion.evenements.auth.User;
import com.gestion.evenements.auth.UserRole;
import com.gestion.evenements.auth.AuthenticationService.UserSession;
import com.gestion.evenements.util.DataSynchronizer;

/**
 * Interface de connexion moderne avec authentification réelle
 * Design épuré et intuitif pour l'authentification connectée au système
 */
public class LoginView extends Application {

    private VBox mainContainer;
    private VBox loginCard;
    private TextField emailField;
    private PasswordField passwordField;
    private CheckBox rememberCheckBox;
    private Label statusLabel;
    private Button loginButton;
    private Button forgotPasswordButton;
    private VBox demoSection;
    
    private AuthenticationService authService;
    private DataSynchronizer dataSynchronizer;

    @Override
    public void start(Stage primaryStage) {
        // Initialiser les services
        authService = AuthenticationService.getInstance();
        dataSynchronizer = DataSynchronizer.getInstance();
        
        primaryStage.setTitle("Se connecter - EventPro");
        primaryStage.setMinWidth(480);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(false);
        
        createGoogleStyleInterface();
        addModernAnimations();
        
        Scene scene = new Scene(mainContainer, 480, 640);
        
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    private void createGoogleStyleInterface() {
        mainContainer = new VBox();
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40, 20, 40, 20));
        mainContainer.setSpacing(32);
        
        // Carte de connexion principale
        loginCard = new VBox();
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(400);
        loginCard.setSpacing(0);
        loginCard.setPadding(new Insets(48, 40, 36, 40));
        
        // Header avec logo et titre
        VBox header = createGoogleHeader();
        
        // Formulaire de connexion
        VBox loginForm = createLoginForm();
        
        // Actions et liens
        VBox actions = createLoginActions();
        
        loginCard.getChildren().addAll(header, loginForm, actions);
        
        // Section démo en bas avec comptes réels
        demoSection = createDemoSection();
        
        mainContainer.getChildren().addAll(loginCard, demoSection);
    }
    
    private VBox createGoogleHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(8);
        header.setPadding(new Insets(0, 0, 32, 0));
        
        // Logo EventPro (style Google)
        Label logoLabel = new Label("EventPro");
        
        // Titre principal
        Label titleLabel = new Label("Se connecter");
        
        // Sous-titre
        Label subtitleLabel = new Label("Accédez à votre compte EventPro");
        
        header.getChildren().addAll(logoLabel, titleLabel, subtitleLabel);
        return header;
    }
    
    private VBox createLoginForm() {
        VBox form = new VBox();
        form.setSpacing(24);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(0, 0, 32, 0));
        
        // Champ Email
        VBox emailSection = new VBox(8);
        Label emailLabel = new Label("Email");
        
        emailField = new TextField();
        emailField.setPromptText("votre.email@exemple.com");
        styleGoogleField(emailField);
        emailField.setOnAction(e -> passwordField.requestFocus());
        
        emailSection.getChildren().addAll(emailLabel, emailField);
        
        // Champ Mot de passe
        VBox passwordSection = new VBox(8);
        Label passwordLabel = new Label("Mot de passe");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Entrez votre mot de passe");
        styleGoogleField(passwordField);
        passwordField.setOnAction(e -> handleLogin());
        
        passwordSection.getChildren().addAll(passwordLabel, passwordField);
        
        // Options ligne (Se souvenir + Mot de passe oublié)
        HBox optionsRow = new HBox();
        optionsRow.setAlignment(Pos.CENTER_LEFT);
        optionsRow.setSpacing(8);
        optionsRow.setPadding(new Insets(8, 0, 0, 0));
        
        rememberCheckBox = new CheckBox("Rester connecté");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        forgotPasswordButton = new Button("Mot de passe oublié ?");
        forgotPasswordButton.setOnAction(e -> showForgotPassword());
        
        optionsRow.getChildren().addAll(rememberCheckBox, spacer, forgotPasswordButton);
        
        // Message de statut
        statusLabel = new Label();
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        
        form.getChildren().addAll(emailSection, passwordSection, optionsRow, statusLabel);
        return form;
    }
    
    private VBox createLoginActions() {
        VBox actions = new VBox();
        actions.setAlignment(Pos.CENTER);
        actions.setSpacing(16);
        
        // Bouton de connexion principal
        loginButton = new Button("Se connecter");
        
        // Effet hover
        loginButton.setOnMouseEntered(e -> {
        });
        
        loginButton.setOnMouseExited(e -> {
            if (!loginButton.isDisabled()) {
            }
        });
        
        loginButton.setOnAction(e -> handleLogin());
        
        // Séparateur et lien vers inscription
        VBox signupSection = new VBox(12);
        signupSection.setAlignment(Pos.CENTER);
        signupSection.setPadding(new Insets(16, 0, 0, 0));
        
        HBox signupRow = new HBox(8);
        signupRow.setAlignment(Pos.CENTER);
        
        Label noAccountLabel = new Label("Vous n'avez pas de compte ?");
        
        Button createAccountButton = new Button("Créer un compte");
        createAccountButton.setOnAction(e -> navigateToRegister());
        
        signupRow.getChildren().addAll(noAccountLabel, createAccountButton);
        
        // Bouton accès invité
        Button guestButton = new Button("Continuer en tant qu'invité");
        guestButton.setOnAction(e -> accessAsGuest());
        
        signupSection.getChildren().addAll(signupRow, guestButton);
        
        actions.getChildren().addAll(loginButton, signupSection);
        return actions;
    }
    
    private VBox createDemoSection() {
        VBox demo = new VBox();
        demo.setAlignment(Pos.CENTER);
        demo.setMaxWidth(400);
        demo.setSpacing(12);
        demo.setPadding(new Insets(24, 24, 24, 24));
        
        Label demoTitle = new Label("🚀 Comptes de démonstration");
        
        VBox accountsList = new VBox(6);
        accountsList.setAlignment(Pos.CENTER_LEFT);
        
        Label adminDemo = createDemoAccountLabel("👨‍💼 Admin", "admin@eventpro.com", "admin123");
        Label orgDemo = createDemoAccountLabel("🎯 Organisateur", "jean.dupont@eventpro.com", "demo123");
        Label partDemo = createDemoAccountLabel("👤 Participant", "marie.martin@email.com", "demo123");
        
        accountsList.getChildren().addAll(adminDemo, orgDemo, partDemo);
        
        // Boutons de connexion rapide
        HBox quickLoginButtons = new HBox(8);
        quickLoginButtons.setAlignment(Pos.CENTER);
        
        Button quickAdminButton = createQuickLoginButton("Admin", () -> quickLogin("admin@eventpro.com", "admin123"));
        Button quickOrgButton = createQuickLoginButton("Organisateur", () -> quickLogin("jean.dupont@eventpro.com", "demo123"));
        Button quickPartButton = createQuickLoginButton("Participant", () -> quickLogin("marie.martin@email.com", "demo123"));
        
        quickLoginButtons.getChildren().addAll(quickAdminButton, quickOrgButton, quickPartButton);
        
        demo.getChildren().addAll(demoTitle, accountsList, quickLoginButtons);
        return demo;
    }
    
    private Label createDemoAccountLabel(String role, String email, String password) {
        Label label = new Label(role + ": " + email + " / " + password);
        return label;
    }
    
    private Button createQuickLoginButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(e -> action.run());
        return button;
    }
    
    private void styleGoogleField(Control field) {
        field.setMaxWidth(Double.MAX_VALUE);
    }
    
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez saisir votre email et mot de passe");
            return;
        }
        
        // Désactiver et animer le bouton
        loginButton.setDisable(true);
        loginButton.setText("Connexion...");
        
        try {
            // Authentification réelle avec le service
            UserSession session = authService.authenticateUser(email, password);
            
            if (session != null) {
                User user = session.getUser();
                showSuccess("Connexion réussie ! Bienvenue " + user.getNom());
                
                System.out.println("✅ Utilisateur connecté: " + user.getNom() + " (" + user.getRole() + ")");
                
                // Redirection avec délai selon le rôle réel
                new Thread(() -> {
                    try {
                        Thread.sleep(1200);
                        javafx.application.Platform.runLater(() -> {
                            redirectUserBasedOnRole(user);
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                
            } else {
                showError("Email ou mot de passe incorrect");
                resetLoginButton();
            }
            
        } catch (Exception e) {
            showError("Erreur lors de la connexion: " + e.getMessage());
            resetLoginButton();
        }
    }
    
    private void quickLogin(String email, String password) {
        emailField.setText(email);
        passwordField.setText(password);
        handleLogin();
    }
    
    private void resetLoginButton() {
        loginButton.setDisable(false);
        loginButton.setText("Se connecter");
    }
    
    private void redirectUserBasedOnRole(User user) {
        try {
            Stage currentStage = (Stage) mainContainer.getScene().getWindow();
            currentStage.close();
            
            Stage newStage = new Stage();
            
            // Redirection selon le rôle réel de l'utilisateur
            switch (user.getRole()) {
                case ADMINISTRATEUR:
                    System.out.println("🚀 Ouverture interface ADMINISTRATEUR");
                    GestionEvenementsApp adminApp = new GestionEvenementsApp();
                    adminApp.setCurrentUser(user); // Passer l'utilisateur connecté
                    adminApp.start(newStage);
                    break;
                    
                case ORGANISATEUR:
                    System.out.println("🚀 Ouverture interface ORGANISATEUR");
                    OrganisateurApp orgApp = new OrganisateurApp();
                    orgApp.setCurrentUser(user); // Passer l'utilisateur connecté
                    orgApp.start(newStage);
                    break;
                    
                case PARTICIPANT:
                    System.out.println("🚀 Ouverture interface PARTICIPANT");
                    ParticipantApp partApp = new ParticipantApp();
                    partApp.setCurrentUser(user); // Passer l'utilisateur connecté
                    partApp.start(newStage);
                    break;
                    
                default:
                    showError("Rôle utilisateur non reconnu: " + user.getRole());
                    resetLoginButton();
                    break;
            }
            
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de l'interface: " + e.getMessage());
            resetLoginButton();
            e.printStackTrace();
        }
    }
    
    private void navigateToRegister() {
        Stage currentStage = (Stage) loginCard.getScene().getWindow();
        currentStage.close();
        
        try {
            Stage registerStage = new Stage();
            RegisterView registerView = new RegisterView();
            registerView.start(registerStage);
        } catch (Exception ex) {
            showError("Erreur lors de l'ouverture de l'inscription: " + ex.getMessage());
        }
    }
    
    private void accessAsGuest() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Accès invité");
        confirm.setHeaderText("Continuer en tant qu'invité");
        confirm.setContentText("En tant qu'invité, vous aurez un accès limité.\n\nVoulez-vous continuer ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Stage currentStage = (Stage) loginCard.getScene().getWindow();
                    currentStage.close();
                    
                    Stage participantStage = new Stage();
                    ParticipantApp participantApp = new ParticipantApp();
                    participantApp.setCurrentUser(null); // Utilisateur invité
                    participantApp.start(participantStage);
                    
                } catch (Exception e) {
                    showError("Erreur lors de l'accès invité: " + e.getMessage());
                }
            }
        });
    }
    
    private void showForgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe oublié");
        dialog.setHeaderText("Récupération du mot de passe");
        dialog.setContentText("Entrez votre adresse email:");
        
        dialog.showAndWait().ifPresent(email -> {
            if (email.trim().isEmpty()) {
                showError("Veuillez entrer une adresse email valide");
                return;
            }
            
            User user = authService.getUserByEmail(email);
            if (user != null) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Récupération envoyée");
                info.setHeaderText("Email de récupération");
                info.setContentText("Un email de récupération a été envoyé à " + email + 
                                   "\n\nPour cette démo, voici votre mot de passe:\n" + 
                                   user.getMotDePasse());
                info.showAndWait();
            } else {
                showError("Aucun compte trouvé avec cette adresse email");
            }
        });
    }
    
    private void addModernAnimations() {
        // Animation d'entrée de la carte
        loginCard.setScaleX(0.95);
        loginCard.setScaleY(0.95);
        loginCard.setOpacity(0);
        
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(400), loginCard);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), loginCard);
        fadeTransition.setToValue(1.0);
        
        // Animation de la section démo avec délai
        demoSection.setOpacity(0);
        demoSection.setTranslateY(20);
        
        FadeTransition demoFade = new FadeTransition(Duration.millis(400), demoSection);
        demoFade.setToValue(1.0);
        demoFade.setDelay(Duration.millis(200));
        
        TranslateTransition demoSlide = new TranslateTransition(Duration.millis(400), demoSection);
        demoSlide.setToY(0);
        demoSlide.setDelay(Duration.millis(200));
        
        scaleTransition.play();
        fadeTransition.play();
        demoFade.play();
        demoSlide.play();
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        
        // Animation d'apparition fluide
        statusLabel.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(300), statusLabel);
        fade.setToValue(1.0);
        fade.play();
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        
        // Animation d'apparition fluide
        statusLabel.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(300), statusLabel);
        fade.setToValue(1.0);
        fade.play();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
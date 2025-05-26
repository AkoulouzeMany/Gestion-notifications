package com.gestion.evenements.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import com.gestion.evenements.controller.*;
import com.gestion.evenements.observer.UIObserver;
import com.gestion.evenements.util.DataSynchronizer;
import com.gestion.evenements.auth.User;
import com.gestion.evenements.auth.UserRole;

/**
 * Application principale pour l'interface d'administration
 * Connectée avec utilisateur réel et synchronisation des données
 */
public class GestionEvenementsApp extends Application {

    private BorderPane mainLayout;
    private ScrollPane sideBarScrollPane;
    private VBox sideBar;
    private ScrollPane contentScrollPane;
    private BorderPane contentArea;
    private Button selectedButton;
    private Label globalStatusLabel;
    
    private DataSynchronizer dataSynchronizer;
    private UIObserver globalUIObserver;
    private User currentUser;
    private EvenementController evenementController;
    private ParticipantController participantController;
    private OrganisateurController organisateurController;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.getRole() != UserRole.ADMINISTRATEUR) {
            throw new IllegalArgumentException("L'utilisateur doit être un administrateur");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        initializeServices();
        initializeControllers();
        
        String titleSuffix = currentUser != null ? " (" + currentUser.getNom() + ")" : " (Mode Démo)";
        primaryStage.setTitle("Gestion d'Événements - Interface Administrative" + titleSuffix);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        createResponsiveUI();
        Scene scene = new Scene(mainLayout, 1008, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        showEvenements();
    }
    
    private void initializeServices() {
        dataSynchronizer = DataSynchronizer.getInstance();
        globalUIObserver = new UIObserver(() -> updateGlobalStatus());
        dataSynchronizer.addGlobalObserver(globalUIObserver);
    }
    
    private void initializeControllers() {
        evenementController = new EvenementController();
        participantController = new ParticipantController();
        organisateurController = new OrganisateurController();
    }
    
    private void createResponsiveUI() {
        mainLayout = new BorderPane();
        createResponsiveSideBar();
        createResponsiveContentArea();
        mainLayout.setLeft(sideBarScrollPane);
        mainLayout.setCenter(contentScrollPane);
    }
    
    private void createResponsiveSideBar() {
        sideBar = new VBox();
        sideBar.setPrefWidth(280);
        sideBar.setSpacing(8);
        sideBar.setPadding(new Insets(24, 0, 24, 0));
        
        VBox header = createHeader();
        VBox systemStatus = createSystemStatus();
        
        Button btnEvenements = createNavButton("📅", "Événements");
        Button btnParticipants = createNavButton("👥", "Participants");
        Button btnOrganisateurs = createNavButton("👨‍💼", "Organisateurs");
        
        btnEvenements.setOnAction(e -> {
            setSelectedButton(btnEvenements);
            showEvenements();
        });
        
        btnParticipants.setOnAction(e -> {
            setSelectedButton(btnParticipants);
            showParticipants();
        });
        
        btnOrganisateurs.setOnAction(e -> {
            setSelectedButton(btnOrganisateurs);
            showOrganisateurs();
        });
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button btnReload = createNavButton("🔄", "Recharger données");
        Button btnLogout = createNavButton("🚪", currentUser != null ? "Déconnexion" : "Connexion");
        
        btnReload.setOnAction(e -> reloadData());
        btnLogout.setOnAction(e -> logout());
        
        sideBar.getChildren().addAll(
            header, new Separator(), systemStatus, new Separator(),
            btnEvenements, btnParticipants, btnOrganisateurs,
            spacer, new Separator(), btnReload, btnLogout
        );
        
        selectedButton = btnEvenements;
        
        sideBarScrollPane = new ScrollPane(sideBar);
        sideBarScrollPane.setFitToWidth(true);
        sideBarScrollPane.setFitToHeight(true);
        sideBarScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sideBarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sideBarScrollPane.setPrefWidth(280);
        sideBarScrollPane.setMaxWidth(280);
        sideBarScrollPane.setMinWidth(280);
    }
    
    private void createResponsiveContentArea() {
        contentArea = new BorderPane();
        updateContentHeader("Gestion des Événements", "Interface administrative");
        contentScrollPane = new ScrollPane(contentArea);
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setFitToHeight(false);
        contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(contentScrollPane, Priority.ALWAYS);
        HBox.setHgrow(contentScrollPane, Priority.ALWAYS);
    }
    
    private VBox createHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(12);
        header.setPadding(new Insets(0, 24, 24, 24));
        
        Label appTitle = new Label("Administration");
        Label appSubtitle = new Label("Événements");
        Label appVersion = new Label(currentUser != null ? "v2.0 - Connecté" : "v2.0 - Démo");
        
        header.getChildren().addAll(appTitle, appSubtitle, appVersion);
        return header;
    }
    
    private VBox createSystemStatus() {
        VBox systemStatus = new VBox();
        systemStatus.setAlignment(Pos.CENTER);
        systemStatus.setSpacing(8);
        systemStatus.setPadding(new Insets(16, 24, 16, 24));
        
        Label userLabel = new Label(currentUser != null ? currentUser.getNom() : "Mode Démo");
        globalStatusLabel = new Label(currentUser != null ? "🔐 Administrateur" : "🔓 Accès libre");
        
        DataSynchronizer.SystemStats stats = dataSynchronizer.getSystemStats();
        Label statsLabel = new Label(String.format("%d événements • %d participants", 
                                                  stats.getTotalEvents(), 
                                                  stats.getTotalParticipants()));
        
        systemStatus.getChildren().addAll(userLabel, globalStatusLabel, statsLabel);
        return systemStatus;
    }
    
    private Button createNavButton(String icon, String text) {
        Button button = new Button();
        button.setPrefWidth(240);
        button.setMaxWidth(Double.MAX_VALUE);
        
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(16);
        
        Label iconLabel = new Label(icon);
        Label textLabel = new Label(text);
        
        content.getChildren().addAll(iconLabel, textLabel);
        button.setGraphic(content);
        return button;
    }
    
    private void updateContentHeader(String title, String subtitle) {
        VBox header = new VBox();
        header.setSpacing(8);
        header.setPadding(new Insets(32, 32, 24, 32));
        
        Label titleLabel = new Label(title);
        Label subtitleLabel = new Label(subtitle);
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        contentArea.setTop(header);
    }
    
    private void setSelectedButton(Button button) {
        selectedButton = button;
    }
    
    private void showEvenements() {
        updateContentHeader("Gestion des Événements", "Gérez les événements");
        contentArea.setCenter(evenementController.getView());
        scrollToTop();
        updateGlobalStatus();
    }
    
    private void showParticipants() {
        updateContentHeader("Gestion des Participants", "Gérez les participants");
        contentArea.setCenter(participantController.getView());
        scrollToTop();
        updateGlobalStatus();
    }
    
    private void showOrganisateurs() {
        updateContentHeader("Gestion des Organisateurs", "Gérez les organisateurs");
        contentArea.setCenter(organisateurController.getView());
        scrollToTop();
        updateGlobalStatus();
    }
    
    private void scrollToTop() {
        Platform.runLater(() -> {
            contentScrollPane.setVvalue(0);
            contentScrollPane.setHvalue(0);
        });
    }
    
    private void updateGlobalStatus() {
        try {
            DataSynchronizer.SystemStats stats = dataSynchronizer.getSystemStats();
            globalStatusLabel.setText(String.format("%d événements • %d participants actifs", 
                                                   stats.getTotalEvents(), 
                                                   stats.getTotalParticipants()));
        } catch (Exception e) {
            globalStatusLabel.setText("Erreur de connexion");
        }
    }
    
    private void reloadData() {
        try {
            dataSynchronizer.reloadDemoData();
            if (selectedButton != null) {
                selectedButton.fire();
            }
            System.out.println("Données rechargées par " + 
                              (currentUser != null ? currentUser.getNom() : "Mode Démo"));
        } catch (Exception e) {
            System.err.println("Erreur lors du rechargement: " + e.getMessage());
        }
    }
    
    private void logout() {
        try {
            dataSynchronizer.removeGlobalObserver(globalUIObserver);
            Stage currentStage = (Stage) mainLayout.getScene().getWindow();
            currentStage.close();
            Stage loginStage = new Stage();
            LoginView loginView = new LoginView();
            loginView.start(loginStage);
            System.out.println("Déconnexion réussie");
        } catch (Exception e) {
            System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() throws Exception {
        if (dataSynchronizer != null && globalUIObserver != null) {
            dataSynchronizer.removeGlobalObserver(globalUIObserver);
        }
        super.stop();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
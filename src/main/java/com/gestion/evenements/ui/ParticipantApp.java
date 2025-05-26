package com.gestion.evenements.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import com.gestion.evenements.model.*;
import com.gestion.evenements.model.evenementparticulier.*;
import com.gestion.evenements.observer.UIObserver;
import com.gestion.evenements.util.DataSynchronizer;
import com.gestion.evenements.auth.User;
import com.gestion.evenements.auth.UserRole;
import com.gestion.evenements.exception.CapaciteMaxAtteinteException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParticipantApp extends Application {

    private BorderPane mainLayout;
    private ScrollPane sideBarScrollPane;
    private VBox sideBar;
    private ScrollPane contentScrollPane;
    private BorderPane contentArea;
    private Button selectedButton;
    private Label globalStatusLabel;
    
    private DataSynchronizer dataSynchronizer;
    private UIObserver participantUIObserver;
    private GestionEvenements gestionEvenements;
    
    private User currentUser;
    private Participant currentParticipant;
    private boolean isGuest = false;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user == null) {
            this.isGuest = true;
            this.currentParticipant = new Participant(
                "GUEST_" + System.currentTimeMillis(), 
                "Invité", 
                "invite@guest.com"
            );
        } else if (user.getRole() == UserRole.PARTICIPANT) {
            this.currentParticipant = new Participant(
                "PART_" + user.getId(), 
                user.getNom(), 
                user.getEmail()
            );
        } else {
            throw new IllegalArgumentException("L'utilisateur doit être un participant ou null (invité)");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        if (currentUser == null && currentParticipant == null) {
            setCurrentUser(null);
        }

        initializeServices();
        synchronizeWithExistingEvents();
        
        primaryStage.setTitle("Gestion d'Événements - Espace Participant (" + currentParticipant.getNom() + ")");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        createResponsiveUI();
        Scene scene = new Scene(mainLayout, 1008, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        showDashboard();
    }
    
    private void initializeServices() {
        dataSynchronizer = DataSynchronizer.getInstance();
        gestionEvenements = GestionEvenements.getInstance();
        participantUIObserver = new UIObserver(() -> refreshCurrentView());
        dataSynchronizer.addGlobalObserver(participantUIObserver);
    }
    
    private void synchronizeWithExistingEvents() {
        if (!isGuest && currentUser != null) {
            List<Evenement> allEvents = gestionEvenements.getEvenements().values().stream()
                .collect(Collectors.toList());
            
            for (Evenement event : allEvents) {
                Optional<Participant> existingParticipant = event.getParticipants().stream()
                    .filter(p -> p.getEmail().equals(currentParticipant.getEmail()))
                    .findFirst();
                
                if (existingParticipant.isPresent()) {
                    event.retirerParticipant(existingParticipant.get());
                    try {
                        event.ajouterParticipant(currentParticipant);
                    } catch (CapaciteMaxAtteinteException e) {
                        globalStatusLabel.setText("Erreur de synchronisation: " + e.getMessage());
                    }
                }
            }
        }
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
        VBox userProfile = createUserProfile();
        
        Button btnDashboard = createNavButton("📊", "Tableau de bord");
        Button btnEvents = createNavButton("📅", "Événements disponibles");
        Button btnMyEvents = createNavButton("🎫", "Mes inscriptions");
        
        btnDashboard.setOnAction(e -> {
            setSelectedButton(btnDashboard);
            showDashboard();
        });
        
        btnEvents.setOnAction(e -> {
            setSelectedButton(btnEvents);
            showAvailableEvents();
        });
        
        btnMyEvents.setOnAction(e -> {
            setSelectedButton(btnMyEvents);
            showMyEvents();
        });
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button btnLogout = createNavButton("🚪", "Déconnexion");
        btnLogout.setOnAction(e -> logout());
        
        sideBar.getChildren().addAll(
            header, new Separator(), userProfile, new Separator(),
            btnDashboard, btnEvents, btnMyEvents,
            spacer, new Separator(), btnLogout
        );
        
        selectedButton = btnDashboard;
        
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
        updateContentHeader("Tableau de bord", "Vue d'ensemble de vos événements");
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
        
        Label appTitle = new Label("Espace");
        Label appSubtitle = new Label("Participant");
        Label appStatus = new Label("v2.0");
        
        header.getChildren().addAll(appTitle, appSubtitle, appStatus);
        return header;
    }
    
    private VBox createUserProfile() {
        VBox profile = new VBox();
        profile.setAlignment(Pos.CENTER);
        profile.setSpacing(8);
        profile.setPadding(new Insets(16, 24, 16, 24));
        
        Label name = new Label(currentParticipant.getNom());
        Label email = new Label(currentParticipant.getEmail());
        globalStatusLabel = new Label("En ligne");
        
        profile.getChildren().addAll(name, email, globalStatusLabel);
        return profile;
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
    
    private void showDashboard() {
        updateContentHeader("Tableau de bord", "Vue d'ensemble de vos événements");
        contentArea.setCenter(createDashboardView());
        scrollToTop();
    }
    
    private void showAvailableEvents() {
        updateContentHeader("Événements disponibles", "Découvrez les nouveaux événements");
        contentArea.setCenter(createAvailableEventsView());
        scrollToTop();
    }
    
    private void showMyEvents() {
        updateContentHeader("Mes inscriptions", "Gérez vos événements");
        contentArea.setCenter(createMyEventsView());
        scrollToTop();
    }
    
    private void scrollToTop() {
        Platform.runLater(() -> {
            contentScrollPane.setVvalue(0);
            contentScrollPane.setHvalue(0);
        });
    }
    
    private VBox createDashboardView() {
        VBox view = new VBox();
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        VBox upcomingEvents = createUpcomingEventsSection();
        view.getChildren().add(upcomingEvents);
        return view;
    }
    
    private VBox createUpcomingEventsSection() {
        VBox section = new VBox();
        section.setSpacing(16);
        
        Label title = new Label("Mes prochains événements");
        VBox eventsContainer = new VBox();
        eventsContainer.setSpacing(8);
        
        List<Evenement> upcomingEvents = getMyUpcomingEvents();
        if (upcomingEvents.isEmpty()) {
            Label noEvents = new Label("Aucun événement à venir.");
            eventsContainer.getChildren().add(noEvents);
        } else {
            for (Evenement event : upcomingEvents) {
                HBox eventCard = createEventCard(event);
                eventsContainer.getChildren().add(eventCard);
            }
        }
        
        section.getChildren().addAll(title, eventsContainer);
        return section;
    }
    
    private VBox createAvailableEventsView() {
        VBox view = new VBox();
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        VBox eventsList = new VBox();
        eventsList.setSpacing(12);
        
        List<Evenement> availableEvents = gestionEvenements.getEvenements().values().stream()
            .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
            .sorted((e1, e2) -> e1.getDate().compareTo(e2.getDate()))
            .collect(Collectors.toList());
        
        if (availableEvents.isEmpty()) {
            Label noEvents = new Label("Aucun événement disponible.");
            eventsList.getChildren().add(noEvents);
        } else {
            for (Evenement event : availableEvents) {
                HBox eventCard = createAvailableEventCard(event);
                eventsList.getChildren().add(eventCard);
            }
        }
        
        view.getChildren().add(eventsList);
        return view;
    }
    
    private VBox createMyEventsView() {
        VBox view = new VBox();
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        VBox eventsList = new VBox();
        eventsList.setSpacing(12);
        
        List<Evenement> myEvents = getMyRegisteredEvents();
        if (myEvents.isEmpty()) {
            Label noEvents = new Label("Vous n'êtes inscrit à aucun événement.");
            eventsList.getChildren().add(noEvents);
        } else {
            for (Evenement event : myEvents) {
                HBox eventCard = createMyEventCard(event);
                eventsList.getChildren().add(eventCard);
            }
        }
        
        view.getChildren().add(eventsList);
        return view;
    }
    
    private HBox createEventCard(Evenement event) {
        HBox card = new HBox();
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        VBox info = new VBox();
        info.setSpacing(4);
        
        Label titleLabel = new Label(event.getNom());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(event.getDate().format(formatter) + " • " + event.getLieu());
        
        info.getChildren().addAll(titleLabel, dateLabel);
        card.getChildren().add(info);
        return card;
    }
    
    private HBox createAvailableEventCard(Evenement event) {
        HBox card = new HBox();
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        VBox info = new VBox();
        info.setSpacing(4);
        
        Label titleLabel = new Label(event.getNom());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(event.getDate().format(formatter) + " • " + event.getLieu());
        int placesRestantes = event.getCapaciteMax() - event.getParticipants().size();
        Label placesLabel = new Label(placesRestantes + " places restantes");
        
        info.getChildren().addAll(titleLabel, dateLabel, placesLabel);
        
        Button registerBtn = new Button(event.getParticipants().contains(currentParticipant) ? "Inscrit" : "S'inscrire");
        registerBtn.setDisable(event.getParticipants().contains(currentParticipant));
        registerBtn.setOnAction(e -> registerToEvent(event));
        
        card.getChildren().addAll(info, registerBtn);
        return card;
    }
    
    private HBox createMyEventCard(Evenement event) {
        HBox card = new HBox();
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        VBox info = new VBox();
        info.setSpacing(4);
        
        Label titleLabel = new Label(event.getNom());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(event.getDate().format(formatter) + " • " + event.getLieu());
        String status = event.getDate().isAfter(LocalDateTime.now()) ? "À venir" : "Terminé";
        Label statusLabel = new Label("Statut: " + status);
        
        info.getChildren().addAll(titleLabel, dateLabel, statusLabel);
        
        Button unregisterBtn = new Button("Se désinscrire");
        unregisterBtn.setOnAction(e -> unregisterFromEvent(event));
        
        card.getChildren().addAll(info, unregisterBtn);
        return card;
    }
    
    private List<Evenement> getMyRegisteredEvents() {
        return gestionEvenements.getEvenements().values().stream()
            .filter(e -> e.getParticipants().contains(currentParticipant))
            .collect(Collectors.toList());
    }
    
    private List<Evenement> getMyUpcomingEvents() {
        return getMyRegisteredEvents().stream()
            .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
            .sorted((e1, e2) -> e1.getDate().compareTo(e2.getDate()))
            .collect(Collectors.toList());
    }
    
    private void registerToEvent(Evenement event) {
        try {
            event.ajouterParticipant(currentParticipant);
            globalStatusLabel.setText("Inscription réussie à " + event.getNom());
            refreshCurrentView();
        } catch (CapaciteMaxAtteinteException e) {
            globalStatusLabel.setText("Inscription impossible: " + e.getMessage());
        }
    }
    
    private void unregisterFromEvent(Evenement event) {
        event.retirerParticipant(currentParticipant);
        globalStatusLabel.setText("Désinscription réussie de " + event.getNom());
        refreshCurrentView();
    }
    
    private void logout() {
        dataSynchronizer.removeGlobalObserver(participantUIObserver);
        Platform.exit();
    }
    
    private void refreshCurrentView() {
        if (selectedButton != null) {
            selectedButton.fire();
        }
    }
    
    @Override
    public void stop() throws Exception {
        if (dataSynchronizer != null && participantUIObserver != null) {
            dataSynchronizer.removeGlobalObserver(participantUIObserver);
        }
        super.stop();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
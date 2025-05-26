package com.gestion.evenements.ui;

import javafx.application.Application;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application pour l'interface Organisateur
 * Interface dédiée aux organisateurs pour gérer leurs événements
 */
public class OrganisateurApp extends Application {

    private BorderPane mainLayout;
    private ScrollPane sideBarScrollPane;
    private VBox sideBar;
    private ScrollPane contentScrollPane;
    private BorderPane contentArea;
    private Button selectedButton;
    private Label globalStatusLabel;
    
    private DataSynchronizer dataSynchronizer;
    private UIObserver organizerUIObserver;
    private GestionEvenements gestionEvenements;
    private User currentUser;
    private Organisateur currentOrganizer;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.getRole() == UserRole.ORGANISATEUR) {
            this.currentOrganizer = new Organisateur(
                "ORG_" + user.getId(), 
                user.getNom(), 
                user.getEmail()
            );
        } else {
            throw new IllegalArgumentException("L'utilisateur doit être un organisateur");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur connecté défini");
        }

        initializeServices();
        associateExistingEvents();
        
        primaryStage.setTitle("Gestion d'Événements - Espace Organisateur (" + currentUser.getNom() + ")");
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
        organizerUIObserver = new UIObserver(() -> refreshCurrentView());
        dataSynchronizer.addGlobalObserver(organizerUIObserver);
    }
    
    private void associateExistingEvents() {
        List<Evenement> allEvents = gestionEvenements.getEvenements().values().stream()
            .collect(Collectors.toList());
        
        for (Evenement event : allEvents) {
            if (event.getNom().contains("Tech") || event.getNom().contains("Innovation")) {
                currentOrganizer.organiserEvenement(event);
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
        VBox organizerProfile = createOrganizerProfile();
        
        Button btnDashboard = createNavButton("📊", "Tableau de bord");
        Button btnMyEvents = createNavButton("📅", "Mes événements");
        Button btnCreateEvent = createNavButton("➕", "Créer un événement");
        Button btnParticipants = createNavButton("👥", "Participants");
        
        btnDashboard.setOnAction(e -> {
            setSelectedButton(btnDashboard);
            showDashboard();
        });
        
        btnMyEvents.setOnAction(e -> {
            setSelectedButton(btnMyEvents);
            showMyEvents();
        });
        
        btnCreateEvent.setOnAction(e -> {
            setSelectedButton(btnCreateEvent);
            showCreateEvent();
        });
        
        btnParticipants.setOnAction(e -> {
            setSelectedButton(btnParticipants);
            showParticipants();
        });
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button btnLogout = createNavButton("🚪", "Déconnexion");
        btnLogout.setOnAction(e -> logout());
        
        sideBar.getChildren().addAll(
            header, new Separator(), organizerProfile, new Separator(),
            btnDashboard, btnMyEvents, btnCreateEvent, btnParticipants,
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
        Label appSubtitle = new Label("Organisateur");
        Label appStatus = new Label("v2.0");
        
        header.getChildren().addAll(appTitle, appSubtitle, appStatus);
        return header;
    }
    
    private VBox createOrganizerProfile() {
        VBox profile = new VBox();
        profile.setAlignment(Pos.CENTER);
        profile.setSpacing(8);
        profile.setPadding(new Insets(16, 24, 16, 24));
        
        Label name = new Label(currentUser.getNom());
        Label role = new Label(currentUser.getRole().toString());
        Label company = new Label(currentUser.getOrganisation() != null ? 
                                 currentUser.getOrganisation() : "EventPro");
        globalStatusLabel = new Label("En ligne");
        
        profile.getChildren().addAll(name, role, company, globalStatusLabel);
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
    
    private void showMyEvents() {
        updateContentHeader("Mes événements", "Gérez vos événements");
        contentArea.setCenter(createMyEventsView());
        scrollToTop();
    }
    
    private void showCreateEvent() {
        updateContentHeader("Créer un événement", "Organisez votre prochain événement");
        contentArea.setCenter(createEventFormView());
        scrollToTop();
    }
    
    private void showParticipants() {
        updateContentHeader("Participants", "Gérez les participants");
        contentArea.setCenter(createParticipantsView());
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
        
        VBox recentEvents = createRecentEventsSection();
        view.getChildren().add(recentEvents);
        return view;
    }
    
    private VBox createRecentEventsSection() {
        VBox section = new VBox();
        section.setSpacing(16);
        
        Label title = new Label("Événements récents");
        VBox eventsContainer = new VBox();
        eventsContainer.setSpacing(8);
        
        List<Evenement> recentEvents = currentOrganizer.getEvenementsOrganises().stream()
            .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
            .limit(5)
            .collect(Collectors.toList());
        
        if (recentEvents.isEmpty()) {
            Label noEvents = new Label("Aucun événement créé.");
            eventsContainer.getChildren().add(noEvents);
        } else {
            for (Evenement event : recentEvents) {
                HBox eventCard = createEventCard(event);
                eventsContainer.getChildren().add(eventCard);
            }
        }
        
        section.getChildren().addAll(title, eventsContainer);
        return section;
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
        Label participantsLabel = new Label(event.getParticipants().size() + "/" + event.getCapaciteMax() + " participants");
        
        info.getChildren().addAll(titleLabel, dateLabel, participantsLabel);
        card.getChildren().add(info);
        return card;
    }
    
    private VBox createMyEventsView() {
        VBox view = new VBox();
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        VBox eventsList = new VBox();
        eventsList.setSpacing(12);
        
        List<Evenement> myEvents = currentOrganizer.getEvenementsOrganises();
        if (myEvents.isEmpty()) {
            Label emptyLabel = new Label("Aucun événement créé.");
            eventsList.getChildren().add(emptyLabel);
        } else {
            for (Evenement event : myEvents) {
                HBox eventCard = createEventCard(event);
                eventsList.getChildren().add(eventCard);
            }
        }
        
        view.getChildren().add(eventsList);
        return view;
    }
    
    private VBox createEventFormView() {
        VBox view = new VBox();
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        VBox form = new VBox();
        form.setSpacing(20);
        
        Label formTitle = new Label("Créer un nouvel événement");
        VBox formFields = createEventFormFields();
        
        form.getChildren().addAll(formTitle, formFields);
        view.getChildren().add(form);
        return view;
    }
    
    private VBox createEventFormFields() {
        VBox formFields = new VBox();
        formFields.setSpacing(16);
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Conférence", "Concert");
        typeCombo.setValue("Conférence");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Nom de l'événement");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description de l'événement");
        descriptionArea.setPrefRowCount(3);
        
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");
        
        TextField timeField = new TextField();
        timeField.setPromptText("Heure (HH:mm)");
        
        TextField locationField = new TextField();
        locationField.setPromptText("Lieu");
        
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacité maximale");
        
        TextField themeField = new TextField();
        themeField.setPromptText("Thème (pour conférence)");
        
        TextField artisteField = new TextField();
        artisteField.setPromptText("Artiste (pour concert)");
        artisteField.setVisible(false);
        
        TextField genreField = new TextField();
        genreField.setPromptText("Genre musical (pour concert)");
        genreField.setVisible(false);
        
        typeCombo.setOnAction(e -> {
            boolean isConference = "Conférence".equals(typeCombo.getValue());
            themeField.setVisible(isConference);
            artisteField.setVisible(!isConference);
            genreField.setVisible(!isConference);
        });
        
        Button publishBtn = new Button("Publier l'événement");
        publishBtn.setOnAction(e -> publishEvent(typeCombo, nameField, descriptionArea, datePicker, timeField, 
                                                locationField, capacityField, themeField, artisteField, genreField));
        
        formFields.getChildren().addAll(
            new Label("Type d'événement:"), typeCombo,
            new Label("Nom:"), nameField,
            new Label("Description:"), descriptionArea,
            new Label("Date et heure:"), datePicker, timeField,
            new Label("Lieu et capacité:"), locationField, capacityField,
            new Label("Détails spécifiques:"), themeField, artisteField, genreField,
            publishBtn
        );
        
        return formFields;
    }
    
    private VBox createParticipantsView() {
        VBox view = new VBox();
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        HBox eventSelector = createEventSelector();
        VBox participantsTable = createParticipantsTable();
        
        view.getChildren().addAll(eventSelector, participantsTable);
        return view;
    }
    
    private HBox createEventSelector() {
        HBox eventSelector = new HBox();
        eventSelector.setSpacing(16);
        eventSelector.setAlignment(Pos.CENTER_LEFT);
        
        Label eventLabel = new Label("Événement:");
        ComboBox<Evenement> eventCombo = new ComboBox<>();
        eventCombo.getItems().addAll(currentOrganizer.getEvenementsOrganises());
        if (!currentOrganizer.getEvenementsOrganises().isEmpty()) {
            eventCombo.setValue(currentOrganizer.getEvenementsOrganises().get(0));
        }
        
        eventCombo.setConverter(new javafx.util.StringConverter<Evenement>() {
            @Override
            public String toString(Evenement event) {
                return event != null ? event.getNom() : "";
            }
            
            @Override
            public Evenement fromString(String string) {
                return null;
            }
        });
        
        eventSelector.getChildren().addAll(eventLabel, eventCombo);
        return eventSelector;
    }
    
    private VBox createParticipantsTable() {
        VBox table = new VBox();
        table.setSpacing(0);
        
        HBox header = new HBox();
        header.setSpacing(16);
        header.setPadding(new Insets(16));
        
        Label nameHeader = new Label("Nom");
        nameHeader.setPrefWidth(200);
        Label emailHeader = new Label("Email");
        emailHeader.setPrefWidth(250);
        Label eventsHeader = new Label("Événements inscrits");
        eventsHeader.setPrefWidth(150);
        
        header.getChildren().addAll(nameHeader, emailHeader, eventsHeader);
        
        VBox rows = new VBox();
        rows.setSpacing(0);
        
        List<Participant> allParticipants = currentOrganizer.getEvenementsOrganises().stream()
            .flatMap(e -> e.getParticipants().stream())
            .distinct()
            .collect(Collectors.toList());
        
        if (allParticipants.isEmpty()) {
            Label noParticipants = new Label("Aucun participant inscrit.");
            noParticipants.setPadding(new Insets(20));
            rows.getChildren().add(noParticipants);
        } else {
            for (Participant participant : allParticipants) {
                HBox row = new HBox();
                row.setSpacing(16);
                row.setPadding(new Insets(12, 16, 12, 16));
                
                Label nameLabel = new Label(participant.getNom());
                nameLabel.setPrefWidth(200);
                
                Label emailLabel = new Label(participant.getEmail());
                emailLabel.setPrefWidth(250);
                
                long eventCount = currentOrganizer.getEvenementsOrganises().stream()
                    .filter(e -> e.getParticipants().contains(participant))
                    .count();
                
                Label eventsLabel = new Label(String.valueOf(eventCount));
                eventsLabel.setPrefWidth(150);
                
                row.getChildren().addAll(nameLabel, emailLabel, eventsLabel);
                rows.getChildren().add(row);
            }
        }
        
        table.getChildren().addAll(header, rows);
        return table;
    }
    
    private void publishEvent(ComboBox<String> typeCombo, TextField nameField, TextArea descriptionArea, 
                             DatePicker datePicker, TextField timeField, TextField locationField, 
                             TextField capacityField, TextField themeField, TextField artisteField, TextField genreField) {
        try {
            if (nameField.getText().trim().isEmpty() || locationField.getText().trim().isEmpty() ||
                capacityField.getText().trim().isEmpty() || datePicker.getValue() == null || timeField.getText().trim().isEmpty()) {
                globalStatusLabel.setText("Veuillez remplir tous les champs obligatoires");
                return;
            }
            
            String type = typeCombo.getValue();
            String nom = nameField.getText();
            String lieu = locationField.getText();
            String capacite = capacityField.getText();
            String dateHeure = datePicker.getValue().toString() + " " + timeField.getText();
            
            String id = type.toUpperCase() + "_" + currentUser.getId() + "_" + System.currentTimeMillis();
            int cap = Integer.parseInt(capacite);
            LocalDateTime date = LocalDateTime.parse(dateHeure, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            if (date.isBefore(LocalDateTime.now())) {
                globalStatusLabel.setText("La date doit être dans le futur");
                return;
            }
            
            Evenement evenement;
            if ("Conférence".equals(type)) {
                if (themeField.getText().trim().isEmpty()) {
                    globalStatusLabel.setText("Le thème est obligatoire pour une conférence");
                    return;
                }
                evenement = new Conference(id, nom, date, lieu, cap, themeField.getText());
            } else {
                if (artisteField.getText().trim().isEmpty() || genreField.getText().trim().isEmpty()) {
                    globalStatusLabel.setText("L'artiste et le genre sont obligatoires pour un concert");
                    return;
                }
                evenement = new Concert(id, nom, date, lieu, cap, artisteField.getText(), genreField.getText());
            }
            
            dataSynchronizer.ajouterEvenementAvecSync(evenement);
            currentOrganizer.organiserEvenement(evenement);
            globalStatusLabel.setText("Événement créé avec succès");
            
            clearForm(nameField, descriptionArea, datePicker, timeField, locationField, capacityField, 
                     themeField, artisteField, genreField);
                     
        } catch (Exception e) {
            globalStatusLabel.setText("Erreur: " + e.getMessage());
        }
    }
    
    private void clearForm(TextField nameField, TextArea descriptionArea, DatePicker datePicker, 
                          TextField timeField, TextField locationField, TextField capacityField, 
                          TextField themeField, TextField artisteField, TextField genreField) {
        nameField.clear();
        descriptionArea.clear();
        datePicker.setValue(null);
        timeField.clear();
        locationField.clear();
        capacityField.clear();
        themeField.clear();
        artisteField.clear();
        genreField.clear();
    }
    
    private void logout() {
        dataSynchronizer.removeGlobalObserver(organizerUIObserver);
        Platform.exit();
    }
    
    private void refreshCurrentView() {
        if (selectedButton != null) {
            selectedButton.fire();
        }
    }
    
    @Override
    public void stop() throws Exception {
        if (dataSynchronizer != null && organizerUIObserver != null) {
            dataSynchronizer.removeGlobalObserver(organizerUIObserver);
        }
        super.stop();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
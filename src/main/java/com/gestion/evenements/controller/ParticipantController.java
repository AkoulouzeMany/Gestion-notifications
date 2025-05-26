package com.gestion.evenements.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;

import com.gestion.evenements.model.*;
import com.gestion.evenements.observer.UIObserver;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParticipantController {
    private VBox view;
    private VBox participantsContainer;
    private Label statusLabel;
    private UIObserver uiObserver;
    private GestionEvenements gestionEvenements;
    
    // Statistiques dynamiques
    private Label totalParticipantsLabel;
    private Label activeParticipantsLabel;
    private Label eventParticipationsLabel;
    
    public ParticipantController() {
        this.gestionEvenements = GestionEvenements.getInstance();
        createView();
        setupObserver();
        refreshParticipantsList();
        updateStatistics();
    }
    
    private void setupObserver() {
        this.uiObserver = new UIObserver(statusLabel, this::refreshParticipantsList);
        
        // Ajouter l'observer à tous les événements pour être notifié des changements de participants
        for (Evenement evenement : gestionEvenements.getEvenements().values()) {
            evenement.ajouterObservateur(uiObserver);
        }
    }
    
    private void createView() {
        view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Label de statut pour les notifications
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-active");
        statusLabel.setVisible(false);
        statusLabel.managedProperty().bind(statusLabel.visibleProperty());
        
        // Barre d'actions
        HBox actionBar = createActionBar();
        
        // Statistiques rapides
        HBox statsBar = createStatsBar();
        
        // Liste des participants
        VBox participantsList = createParticipantsList();
        
        view.getChildren().addAll(statusLabel, actionBar, statsBar, participantsList);
    }
    
    private HBox createActionBar() {
        HBox actionBar = new HBox();
        actionBar.getStyleClass().add("action-bar");
        actionBar.setSpacing(16);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        Button btnAdd = new Button("👤+ Nouveau Participant");
        btnAdd.getStyleClass().add("primary-button");
        btnAdd.setOnAction(e -> showCreateParticipantDialog());
        
        Button btnImport = new Button("📥 Importer");
        btnImport.getStyleClass().add("secondary-button");
        btnImport.setOnAction(e -> importParticipants());
        
        Button btnExport = new Button("📤 Exporter");
        btnExport.getStyleClass().add("secondary-button");
        btnExport.setOnAction(e -> exportParticipants());
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Barre de recherche
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher un participant...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldText, newText) -> filterParticipants(newText));
        
        actionBar.getChildren().addAll(btnAdd, btnImport, btnExport, spacer, searchField);
        return actionBar;
    }
    
    private HBox createStatsBar() {
    HBox statsBar = new HBox();
    statsBar.getStyleClass().add("stats-bar");
    statsBar.setSpacing(24);
    
    VBox totalParticipants = createStatCard("0", "Participants totaux", "total-stat");
    totalParticipantsLabel = (Label) totalParticipants.getChildren().get(0); // Directement le Label à l'index 0
    
    VBox activeParticipants = createStatCard("0", "Participants actifs", "positive-stat");
    activeParticipantsLabel = (Label) activeParticipants.getChildren().get(0); // Directement le Label à l'index 0
    
    VBox eventParticipations = createStatCard("0", "Inscriptions totales", "info-stat");
    eventParticipationsLabel = (Label) eventParticipations.getChildren().get(0); // Directement le Label à l'index 0
    
    VBox avgEvents = createStatCard("0", "Événements/participant", "success-stat");
    
    statsBar.getChildren().addAll(totalParticipants, activeParticipants, eventParticipations, avgEvents);
    return statsBar;
}
    
    private VBox createStatCard(String value, String label, String styleClass) {
        VBox card = new VBox();
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setAlignment(Pos.CENTER);
        card.setSpacing(8);
        card.setPrefWidth(180);
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("stat-label");
        
        card.getChildren().addAll(valueLabel, labelLabel);
        return card;
    }
    
    private VBox createParticipantsList() {
        VBox participantsList = new VBox();
        participantsList.getStyleClass().add("events-list");
        participantsList.setSpacing(12);
        
        Label title = new Label("Participants");
        title.getStyleClass().add("section-title");
        
        // Container pour les participants
        participantsContainer = new VBox();
        participantsContainer.setSpacing(8);
        
        participantsList.getChildren().addAll(title, participantsContainer);
        return participantsList;
    }
    
    private void refreshParticipantsList() {
        participantsContainer.getChildren().clear();
        
        // Récupérer tous les participants uniques de tous les événements
        List<Participant> allParticipants = gestionEvenements.getEvenements().values().stream()
            .flatMap(e -> e.getParticipants().stream())
            .distinct()
            .collect(Collectors.toList());
        
        for (Participant participant : allParticipants) {
            HBox participantCard = createParticipantCard(participant);
            participantsContainer.getChildren().add(participantCard);
        }
        
        updateStatistics();
        
        // Afficher le statut
        if (!statusLabel.getText().isEmpty()) {
            statusLabel.setVisible(true);
        }
    }
    
    private HBox createParticipantCard(Participant participant) {
        HBox card = new HBox();
        card.getStyleClass().add("event-card");
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        VBox info = new VBox();
        info.setSpacing(4);
        VBox.setVgrow(info, Priority.ALWAYS);
        
        Label nameLabel = new Label("👤 " + participant.getNom());
        nameLabel.getStyleClass().add("event-title");
        
        Label emailLabel = new Label(participant.getEmail());
        emailLabel.getStyleClass().add("event-date");
        
        // Compter dans combien d'événements ce participant est inscrit
        long eventCount = gestionEvenements.getEvenements().values().stream()
            .filter(e -> e.getParticipants().contains(participant))
            .count();
        
        Label eventsLabel = new Label("Inscrit à " + eventCount + " événement(s)");
        eventsLabel.getStyleClass().add("event-participants");
        
        info.getChildren().addAll(nameLabel, emailLabel, eventsLabel);
        
        // Boutons d'action
        HBox actions = new HBox();
        actions.setSpacing(8);
        
        Button btnView = new Button("👁️");
        btnView.getStyleClass().add("icon-button");
        btnView.setOnAction(e -> viewParticipantDetails(participant));
        
        Button btnEdit = new Button("✏️");
        btnEdit.getStyleClass().add("icon-button");
        btnEdit.setOnAction(e -> editParticipant(participant));
        
        Button btnAddToEvent = new Button("📅+");
        btnAddToEvent.getStyleClass().add("icon-button");
        btnAddToEvent.setOnAction(e -> addParticipantToEvent(participant));
        
        Button btnRemove = new Button("🗑️");
        btnRemove.getStyleClass().add("icon-button-danger");
        btnRemove.setOnAction(e -> removeParticipantFromAllEvents(participant));
        
        actions.getChildren().addAll(btnView, btnEdit, btnAddToEvent, btnRemove);
        
        card.getChildren().addAll(info, actions);
        return card;
    }
    
    private void showCreateParticipantDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Créer un nouveau participant");
        
        VBox content = new VBox();
        content.setSpacing(16);
        content.setPadding(new Insets(20));
        
        TextField nomField = new TextField();
        nomField.setPromptText("Nom du participant");
        nomField.getStyleClass().add("search-field");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("search-field");
        
        ComboBox<String> eventCombo = new ComboBox<>();
        eventCombo.setPromptText("Sélectionner un événement (optionnel)");
        eventCombo.getStyleClass().add("search-field");
        
        // Remplir la liste des événements
        for (Evenement evenement : gestionEvenements.getEvenements().values()) {
            eventCombo.getItems().add(evenement.getNom() + " - " + evenement.getId());
        }
        
        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnCreate = new Button("Créer");
        btnCreate.getStyleClass().add("primary-button");
        btnCreate.setOnAction(e -> {
            try {
                createParticipant(nomField.getText(), emailField.getText(), eventCombo.getValue());
                dialog.close();
            } catch (Exception ex) {
                showError("Erreur lors de la création: " + ex.getMessage());
            }
        });
        
        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().add("secondary-button");
        btnCancel.setOnAction(e -> dialog.close());
        
        buttons.getChildren().addAll(btnCancel, btnCreate);
        
        content.getChildren().addAll(
            new Label("Nom:"), nomField,
            new Label("Email:"), emailField,
            new Label("Ajouter à l'événement:"), eventCombo,
            buttons
        );
        
        Scene scene = new Scene(content, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void createParticipant(String nom, String email, String selectedEvent) {
        if (nom.trim().isEmpty() || email.trim().isEmpty()) {
            throw new RuntimeException("Le nom et l'email sont obligatoires");
        }
        
        String id = "PART_" + System.currentTimeMillis();
        Participant participant = new Participant(id, nom.trim(), email.trim());
        
        // Si un événement est sélectionné, ajouter le participant à cet événement
        if (selectedEvent != null && !selectedEvent.isEmpty()) {
            String eventId = selectedEvent.split(" - ")[1];
            Evenement evenement = gestionEvenements.rechercherEvenement(eventId);
            if (evenement != null) {
                try {
                    evenement.ajouterParticipant(participant);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur lors de l'ajout à l'événement: " + e.getMessage());
                }
            }
        }
        
        statusLabel.setText("Participant " + nom + " créé avec succès");
    }
    
    private void viewParticipantDetails(Participant participant) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du participant");
        alert.setHeaderText(participant.getNom());
        
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(participant.getId()).append("\n");
        details.append("Email: ").append(participant.getEmail()).append("\n\n");
        
        details.append("Événements inscrits:\n");
        for (Evenement evenement : gestionEvenements.getEvenements().values()) {
            if (evenement.getParticipants().contains(participant)) {
                details.append("- ").append(evenement.getNom())
                       .append(" (").append(evenement.getDate().toLocalDate()).append(")\n");
            }
        }
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
    
    private void editParticipant(Participant participant) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modifier le participant");
        
        VBox content = new VBox();
        content.setSpacing(16);
        content.setPadding(new Insets(20));
        
        TextField nomField = new TextField(participant.getNom());
        nomField.getStyleClass().add("search-field");
        
        TextField emailField = new TextField(participant.getEmail());
        emailField.getStyleClass().add("search-field");
        
        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnSave = new Button("Sauvegarder");
        btnSave.getStyleClass().add("primary-button");
        btnSave.setOnAction(e -> {
            participant.setNom(nomField.getText());
            participant.setEmail(emailField.getText());
            refreshParticipantsList();
            statusLabel.setText("Participant " + participant.getNom() + " modifié");
            dialog.close();
        });
        
        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().add("secondary-button");
        btnCancel.setOnAction(e -> dialog.close());
        
        buttons.getChildren().addAll(btnCancel, btnSave);
        
        content.getChildren().addAll(
            new Label("Nom:"), nomField,
            new Label("Email:"), emailField,
            buttons
        );
        
        Scene scene = new Scene(content, 300, 200);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void addParticipantToEvent(Participant participant) {
        // Afficher la liste des événements où le participant n'est pas encore inscrit
        List<Evenement> availableEvents = gestionEvenements.getEvenements().values().stream()
            .filter(e -> !e.getParticipants().contains(participant))
            .collect(Collectors.toList());
        
        if (availableEvents.isEmpty()) {
            showInfo("Ce participant est déjà inscrit à tous les événements disponibles");
            return;
        }
        
        ChoiceDialog<Evenement> dialog = new ChoiceDialog<>(availableEvents.get(0), availableEvents);
        dialog.setTitle("Ajouter à un événement");
        dialog.setHeaderText("Inscrire " + participant.getNom() + " à un événement");
        dialog.setContentText("Choisir l'événement:");
        
        // Personnaliser l'affichage des événements
        dialog.getItems().forEach(evenement -> {
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
        });
        
        Optional<Evenement> result = dialog.showAndWait();
        result.ifPresent(evenement -> {
            try {
                evenement.ajouterParticipant(participant);
            } catch (Exception e) {
                showError("Erreur lors de l'inscription: " + e.getMessage());
            }
        });
    }
    
    private void removeParticipantFromAllEvents(Participant participant) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Retirer le participant");
        confirmation.setContentText("Êtes-vous sûr de vouloir retirer \"" + participant.getNom() + 
                                   "\" de tous les événements ?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Retirer le participant de tous les événements
            for (Evenement evenement : gestionEvenements.getEvenements().values()) {
                if (evenement.getParticipants().contains(participant)) {
                    evenement.retirerParticipant(participant);
                }
            }
        }
    }
    
    private void filterParticipants(String searchText) {
        // Implémentation basique du filtrage
        refreshParticipantsList(); // Pour l'instant, on rafraîchit tout
    }
    
    private void updateStatistics() {
        // Récupérer tous les participants uniques
        List<Participant> allParticipants = gestionEvenements.getEvenements().values().stream()
            .flatMap(e -> e.getParticipants().stream())
            .distinct()
            .collect(Collectors.toList());
        
        int totalParticipants = allParticipants.size();
        int activeParticipants = totalParticipants; // Tous sont considérés actifs pour l'instant
        
        int totalInscriptions = gestionEvenements.getEvenements().values().stream()
            .mapToInt(e -> e.getParticipants().size())
            .sum();
        
        totalParticipantsLabel.setText(String.valueOf(totalParticipants));
        activeParticipantsLabel.setText(String.valueOf(activeParticipants));
        eventParticipationsLabel.setText(String.valueOf(totalInscriptions));
    }
    
    private void importParticipants() {
        showInfo("Fonctionnalité d'import à implémenter");
    }
    
    private void exportParticipants() {
        showInfo("Fonctionnalité d'export à implémenter");
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public VBox getView() {
        return view;
    }
}
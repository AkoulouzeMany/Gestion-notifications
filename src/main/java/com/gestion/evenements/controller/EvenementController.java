package com.gestion.evenements.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class EvenementController {
    private VBox view;
    
    public EvenementController() {
        createView();
    }
    
    private void createView() {
        view = new VBox();
        view.getStyleClass().add("content-view");
        view.setSpacing(24);
        view.setPadding(new Insets(0, 32, 32, 32));
        
        // Barre d'actions
        HBox actionBar = createActionBar();
        
        // Statistiques rapides
        HBox statsBar = createStatsBar();
        
        // Liste des événements
        VBox eventsList = createEventsList();
        
        view.getChildren().addAll(actionBar, statsBar, eventsList);
    }
    
    private HBox createActionBar() {
        HBox actionBar = new HBox();
        actionBar.getStyleClass().add("action-bar");
        actionBar.setSpacing(16);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        Button btnAdd = new Button("➕ Nouvel Événement");
        btnAdd.getStyleClass().add("primary-button");
        
        Button btnImport = new Button("📥 Importer");
        btnImport.getStyleClass().add("secondary-button");
        
        Button btnExport = new Button("📤 Exporter");
        btnExport.getStyleClass().add("secondary-button");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Barre de recherche
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher un événement...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);
        
        actionBar.getChildren().addAll(btnAdd, btnImport, btnExport, spacer, searchField);
        return actionBar;
    }
    
    private HBox createStatsBar() {
        HBox statsBar = new HBox();
        statsBar.getStyleClass().add("stats-bar");
        statsBar.setSpacing(24);
        
        VBox totalEvents = createStatCard("12", "Événements", "total-stat");
        VBox thisMonth = createStatCard("4", "Ce mois", "positive-stat");
        VBox participants = createStatCard("248", "Participants", "info-stat");
        VBox revenue = createStatCard("15,200€", "Revenus", "success-stat");
        
        statsBar.getChildren().addAll(totalEvents, thisMonth, participants, revenue);
        return statsBar;
    }
    
    private VBox createStatCard(String value, String label, String styleClass) {
        VBox card = new VBox();
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setAlignment(Pos.CENTER);
        card.setSpacing(8);
        card.setPrefWidth(150);
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("stat-label");
        
        card.getChildren().addAll(valueLabel, labelLabel);
        return card;
    }
    
    private VBox createEventsList() {
        VBox eventsList = new VBox();
        eventsList.getStyleClass().add("events-list");
        eventsList.setSpacing(12);
        
        Label title = new Label("Événements Récents");
        title.getStyleClass().add("section-title");

        // Événements exemple
        VBox eventsContainer = new VBox();
        eventsContainer.setSpacing(8);
        
        eventsContainer.getChildren().addAll(
            createEventCard("🎤 Conférence Tech 2025", "15 Juin 2025 • Paris", "124 participants", "conference"),
            createEventCard("🎵 Concert Jazz Night", "20 Mai 2025 • Lyon", "85 participants", "concert"),
            createEventCard("💼 Séminaire Business", "2 Juillet 2025 • Marseille", "67 participants", "seminar")
        );

        
        eventsList.getChildren().addAll(title, eventsContainer);
        return eventsList;
    }
    
    private HBox createEventCard(String title, String date, String participants, String type) {
        HBox card = new HBox();
        card.getStyleClass().addAll("event-card", type + "-card");
        card.setSpacing(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        VBox info = new VBox();
        info.setSpacing(4);
        VBox.setVgrow(info, Priority.ALWAYS);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("event-title");
        
        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("event-date");
        
        Label participantsLabel = new Label(participants);
        participantsLabel.getStyleClass().add("event-participants");
        
        info.getChildren().addAll(titleLabel, dateLabel, participantsLabel);
        
        // Boutons d'action
        HBox actions = new HBox();
        actions.setSpacing(8);
        
        Button btnEdit = new Button("✏️");
        btnEdit.getStyleClass().add("icon-button");
        
        Button btnDelete = new Button("🗑️");
        btnDelete.getStyleClass().add("icon-button-danger");
        
        actions.getChildren().addAll(btnEdit, btnDelete);
        
        card.getChildren().addAll(info, actions);
        return card;
    }
    
    public VBox getView() {
        return view;
    }
}

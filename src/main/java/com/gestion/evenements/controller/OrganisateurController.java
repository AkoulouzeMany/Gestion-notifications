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

public class OrganisateurController {
    private VBox view;
    private HBox organizersContainer;
    private Label statusLabel;
    private UIObserver uiObserver;
    private GestionEvenements gestionEvenements;
    
    // Liste des organisateurs (simulation - dans une vraie app, ce serait dans une base de données)
    private List<Organisateur> organisateurs;
    
    // Statistiques dynamiques
    private Label totalOrganizersLabel;
    private Label activeOrganizersLabel;
    private Label totalEventsLabel;
    
    public OrganisateurController() {
        this.gestionEvenements = GestionEvenements.getInstance();
        initializeOrganizers();
        createView();
        setupObserver();
        refreshOrganizersList();
        updateStatistics();
    }
    
    private void initializeOrganizers() {
        // Initialiser quelques organisateurs par défaut
        organisateurs = List.of(
            new Organisateur("ORG_1", "Jean Dupont", "jean.dupont@eventpro.com"),
            new Organisateur("ORG_2", "Marie Claire", "marie.claire@eventpro.com"),
            new Organisateur("ORG_3", "Paul Martin", "paul.martin@eventpro.com")
        );
        
        // Associer quelques événements existants aux organisateurs (simulation)
        if (!gestionEvenements.getEvenements().isEmpty()) {
            List<Evenement> events = gestionEvenements.getEvenements().values().stream().collect(Collectors.toList());
            for (int i = 0; i < Math.min(events.size(), organisateurs.size()); i++) {
                organisateurs.get(i % organisateurs.size()).organiserEvenement(events.get(i));
            }
        }
    }
    
    private void setupObserver() {
        this.uiObserver = new UIObserver(statusLabel, this::refreshOrganizersList);
        
        // Ajouter l'observer à tous les événements pour être notifié des changements
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
        
        // Liste des organisateurs
        VBox organizersList = createOrganizersList();
        
        view.getChildren().addAll(statusLabel, actionBar, statsBar, organizersList);
    }
    
    private HBox createActionBar() {
        HBox actionBar = new HBox();
        actionBar.getStyleClass().add("action-bar");
        actionBar.setSpacing(16);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        Button btnAdd = new Button("👨‍💼+ Nouvel Organisateur");
        btnAdd.getStyleClass().add("primary-button");
        btnAdd.setOnAction(e -> showCreateOrganizerDialog());
        
        Button btnAssignEvent = new Button("📅 Assigner Événement");
        btnAssignEvent.getStyleClass().add("secondary-button");
        btnAssignEvent.setOnAction(e -> showAssignEventDialog());
        
        Button btnReports = new Button("📊 Rapports");
        btnReports.getStyleClass().add("secondary-button");
        btnReports.setOnAction(e -> showReports());
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Barre de recherche
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher un organisateur...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldText, newText) -> filterOrganizers(newText));
        
        actionBar.getChildren().addAll(btnAdd, btnAssignEvent, btnReports, spacer, searchField);
        return actionBar;
    }
    
    private HBox createStatsBar() {
    HBox statsBar = new HBox();
    statsBar.getStyleClass().add("stats-bar");
    statsBar.setSpacing(24);
    
    VBox totalOrganizers = createStatCard("0", "Organisateurs", "total-stat");
    totalOrganizersLabel = (Label) totalOrganizers.getChildren().get(0); // Supprimé le .getChildren().get(0) final
    
    VBox activeOrganizers = createStatCard("0", "Organisateurs actifs", "positive-stat");
    activeOrganizersLabel = (Label) activeOrganizers.getChildren().get(0); // Supprimé le .getChildren().get(0) final
    
    VBox totalEvents = createStatCard("0", "Événements organisés", "info-stat");
    totalEventsLabel = (Label) totalEvents.getChildren().get(0); // Supprimé le .getChildren().get(0) final
    
    VBox avgEvents = createStatCard("0", "Événements/organisateur", "success-stat");
    
    statsBar.getChildren().addAll(totalOrganizers, activeOrganizers, totalEvents, avgEvents);
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
    
    private VBox createOrganizersList() {
        VBox organizersList = new VBox();
        organizersList.getStyleClass().add("events-list");
        organizersList.setSpacing(12);
        
        Label title = new Label("Équipe d'organisateurs");
        title.getStyleClass().add("section-title");
        
        Label description = new Label("Gérez les organisateurs et leurs événements assignés");
        description.getStyleClass().add("section-description");
        
        // Container pour les organisateurs
        organizersContainer = new HBox();
        organizersContainer.setSpacing(24);
        
        organizersList.getChildren().addAll(title, description, organizersContainer);
        return organizersList;
    }
    
    private void refreshOrganizersList() {
        organizersContainer.getChildren().clear();
        
        for (Organisateur organisateur : organisateurs) {
            VBox organizerCard = createOrganizerCard(organisateur);
            organizersContainer.getChildren().add(organizerCard);
        }
        
        updateStatistics();
        
        // Afficher le statut
        if (!statusLabel.getText().isEmpty()) {
            statusLabel.setVisible(true);
        }
    }
    
    private VBox createOrganizerCard(Organisateur organisateur) {
        VBox card = new VBox();
        card.getStyleClass().add("organizer-card");
        card.setAlignment(Pos.CENTER);
        card.setSpacing(12);
        card.setPadding(new Insets(24));
        card.setPrefWidth(250);
        
        Label iconLabel = new Label("👨‍💼");
        iconLabel.getStyleClass().add("organizer-icon");
        
        Label nameLabel = new Label(organisateur.getNom());
        nameLabel.getStyleClass().add("organizer-name");
        
        Label emailLabel = new Label(organisateur.getEmail());
        emailLabel.getStyleClass().add("organizer-role");
        emailLabel.setStyle("-fx-font-size: 11px;");
        
        Label eventsLabel = new Label(organisateur.getEvenementsOrganises().size() + " événement(s)");
        eventsLabel.getStyleClass().add("organizer-events");
        
        // Boutons d'action
        HBox actions = new HBox();
        actions.setSpacing(8);
        actions.setAlignment(Pos.CENTER);
        
        Button btnView = new Button("👁️");
        btnView.getStyleClass().add("icon-button");
        btnView.setOnAction(e -> viewOrganizerDetails(organisateur));
        
        Button btnEdit = new Button("✏️");
        btnEdit.getStyleClass().add("icon-button");
        btnEdit.setOnAction(e -> editOrganizer(organisateur));
        
        Button btnAssign = new Button("📅+");
        btnAssign.getStyleClass().add("icon-button");
        btnAssign.setOnAction(e -> assignEventToOrganizer(organisateur));
        
        actions.getChildren().addAll(btnView, btnEdit, btnAssign);
        
        Button btnManage = new Button("Gérer");
        btnManage.getStyleClass().add("primary-button-small");
        btnManage.setOnAction(e -> manageOrganizer(organisateur));
        
        card.getChildren().addAll(iconLabel, nameLabel, emailLabel, eventsLabel, actions, btnManage);
        return card;
    }
    
    private void showCreateOrganizerDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Créer un nouvel organisateur");
        
        VBox content = new VBox();
        content.setSpacing(16);
        content.setPadding(new Insets(20));
        
        TextField nomField = new TextField();
        nomField.setPromptText("Nom de l'organisateur");
        nomField.getStyleClass().add("search-field");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("search-field");
        
        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnCreate = new Button("Créer");
        btnCreate.getStyleClass().add("primary-button");
        btnCreate.setOnAction(e -> {
            try {
                createOrganizer(nomField.getText(), emailField.getText());
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
            buttons
        );
        
        Scene scene = new Scene(content, 400, 250);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void createOrganizer(String nom, String email) {
        if (nom.trim().isEmpty() || email.trim().isEmpty()) {
            throw new RuntimeException("Le nom et l'email sont obligatoires");
        }
        
        // Vérifier que l'email n'existe pas déjà
        boolean emailExists = organisateurs.stream()
            .anyMatch(org -> org.getEmail().equalsIgnoreCase(email.trim()));
        
        if (emailExists) {
            throw new RuntimeException("Un organisateur avec cet email existe déjà");
        }
        
        String id = "ORG_" + System.currentTimeMillis();
        Organisateur nouveauOrganisateur = new Organisateur(id, nom.trim(), email.trim());
        
        // Ajouter à la liste (dans une vraie app, ce serait dans la base de données)
        organisateurs.add(nouveauOrganisateur);
        
        refreshOrganizersList();
        statusLabel.setText("Organisateur " + nom + " créé avec succès");
    }
    
    private void viewOrganizerDetails(Organisateur organisateur) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'organisateur");
        alert.setHeaderText(organisateur.getNom());
        
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(organisateur.getId()).append("\n");
        details.append("Email: ").append(organisateur.getEmail()).append("\n");
        details.append("Événements organisés: ").append(organisateur.getEvenementsOrganises().size()).append("\n\n");
        
        if (!organisateur.getEvenementsOrganises().isEmpty()) {
            details.append("Liste des événements:\n");
            for (Evenement evenement : organisateur.getEvenementsOrganises()) {
                details.append("- ").append(evenement.getNom())
                       .append(" (").append(evenement.getDate().toLocalDate())
                       .append(", ").append(evenement.getParticipants().size()).append(" participants)\n");
            }
        } else {
            details.append("Aucun événement assigné pour le moment.");
        }
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
    
    private void editOrganizer(Organisateur organisateur) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modifier l'organisateur");
        
        VBox content = new VBox();
        content.setSpacing(16);
        content.setPadding(new Insets(20));
        
        TextField nomField = new TextField(organisateur.getNom());
        nomField.getStyleClass().add("search-field");
        
        TextField emailField = new TextField(organisateur.getEmail());
        emailField.getStyleClass().add("search-field");
        
        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnSave = new Button("Sauvegarder");
        btnSave.getStyleClass().add("primary-button");
        btnSave.setOnAction(e -> {
            organisateur.setNom(nomField.getText());
            organisateur.setEmail(emailField.getText());
            refreshOrganizersList();
            statusLabel.setText("Organisateur " + organisateur.getNom() + " modifié");
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
    
    private void assignEventToOrganizer(Organisateur organisateur) {
        // Afficher la liste des événements non encore assignés à cet organisateur
        List<Evenement> availableEvents = gestionEvenements.getEvenements().values().stream()
            .filter(e -> !organisateur.getEvenementsOrganises().contains(e))
            .collect(Collectors.toList());
        
        if (availableEvents.isEmpty()) {
            showInfo("Tous les événements sont déjà assignés à cet organisateur");
            return;
        }
        
        ChoiceDialog<Evenement> dialog = new ChoiceDialog<>(availableEvents.get(0), availableEvents);
        dialog.setTitle("Assigner un événement");
        dialog.setHeaderText("Assigner un événement à " + organisateur.getNom());
        dialog.setContentText("Choisir l'événement:");
        
        Optional<Evenement> result = dialog.showAndWait();
        result.ifPresent(evenement -> {
            organisateur.organiserEvenement(evenement);
            refreshOrganizersList();
            statusLabel.setText("Événement " + evenement.getNom() + " assigné à " + organisateur.getNom());
        });
    }
    
    private void manageOrganizer(Organisateur organisateur) {
        // Afficher un menu contextuel avec les options de gestion
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem viewEvents = new MenuItem("Voir les événements");
        viewEvents.setOnAction(e -> viewOrganizerEvents(organisateur));
        
        MenuItem removeEvents = new MenuItem("Retirer tous les événements");
        removeEvents.setOnAction(e -> removeAllEventsFromOrganizer(organisateur));
        
        MenuItem deleteOrganizer = new MenuItem("Supprimer l'organisateur");
        deleteOrganizer.setOnAction(e -> deleteOrganizer(organisateur));
        
        contextMenu.getItems().addAll(viewEvents, removeEvents, new SeparatorMenuItem(), deleteOrganizer);
        contextMenu.show(view, 0, 0);
    }
    
    private void viewOrganizerEvents(Organisateur organisateur) {
        if (organisateur.getEvenementsOrganises().isEmpty()) {
            showInfo("Cet organisateur n'a aucun événement assigné");
            return;
        }
        
        StringBuilder eventsList = new StringBuilder();
        eventsList.append("Événements de ").append(organisateur.getNom()).append(":\n\n");
        
        for (Evenement evenement : organisateur.getEvenementsOrganises()) {
            eventsList.append("• ").append(evenement.getNom()).append("\n");
            eventsList.append("  Date: ").append(evenement.getDate().toLocalDate()).append("\n");
            eventsList.append("  Lieu: ").append(evenement.getLieu()).append("\n");
            eventsList.append("  Participants: ").append(evenement.getParticipants().size())
                      .append("/").append(evenement.getCapaciteMax()).append("\n\n");
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Événements de l'organisateur");
        alert.setHeaderText(null);
        alert.setContentText(eventsList.toString());
        alert.showAndWait();
    }
    
    private void removeAllEventsFromOrganizer(Organisateur organisateur) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Retirer tous les événements");
        confirmation.setContentText("Êtes-vous sûr de vouloir retirer tous les événements de " + 
                                   organisateur.getNom() + " ?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            organisateur.getEvenementsOrganises().clear();
            refreshOrganizersList();
            statusLabel.setText("Tous les événements retirés de " + organisateur.getNom());
        }
    }
    
    private void deleteOrganizer(Organisateur organisateur) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer l'organisateur");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer \"" + organisateur.getNom() + "\" ?\n" +
                                   "Cette action supprimera également tous ses événements assignés.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            organisateurs.remove(organisateur);
            refreshOrganizersList();
            statusLabel.setText("Organisateur " + organisateur.getNom() + " supprimé");
        }
    }
    
    private void showAssignEventDialog() {
        if (organisateurs.isEmpty()) {
            showInfo("Aucun organisateur disponible");
            return;
        }
        
        if (gestionEvenements.getEvenements().isEmpty()) {
            showInfo("Aucun événement disponible");
            return;
        }
        
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Assigner un événement");
        
        VBox content = new VBox();
        content.setSpacing(16);
        content.setPadding(new Insets(20));
        
        ComboBox<Organisateur> organizerCombo = new ComboBox<>();
        organizerCombo.getItems().addAll(organisateurs);
        organizerCombo.getStyleClass().add("search-field");
        organizerCombo.setConverter(new javafx.util.StringConverter<Organisateur>() {
            @Override
            public String toString(Organisateur org) {
                return org != null ? org.getNom() : "";
            }
            
            @Override
            public Organisateur fromString(String string) {
                return null;
            }
        });
        
        ComboBox<Evenement> eventCombo = new ComboBox<>();
        eventCombo.getItems().addAll(gestionEvenements.getEvenements().values());
        eventCombo.getStyleClass().add("search-field");
        eventCombo.setConverter(new javafx.util.StringConverter<Evenement>() {
            @Override
            public String toString(Evenement evt) {
                return evt != null ? evt.getNom() : "";
            }
            
            @Override
            public Evenement fromString(String string) {
                return null;
            }
        });
        
        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnAssign = new Button("Assigner");
        btnAssign.getStyleClass().add("primary-button");
        btnAssign.setOnAction(e -> {
            Organisateur selectedOrganizer = organizerCombo.getValue();
            Evenement selectedEvent = eventCombo.getValue();
            
            if (selectedOrganizer != null && selectedEvent != null) {
                selectedOrganizer.organiserEvenement(selectedEvent);
                refreshOrganizersList();
                statusLabel.setText("Événement assigné avec succès");
                dialog.close();
            } else {
                showError("Veuillez sélectionner un organisateur et un événement");
            }
        });
        
        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().add("secondary-button");
        btnCancel.setOnAction(e -> dialog.close());
        
        buttons.getChildren().addAll(btnCancel, btnAssign);
        
        content.getChildren().addAll(
            new Label("Organisateur:"), organizerCombo,
            new Label("Événement:"), eventCombo,
            buttons
        );
        
        Scene scene = new Scene(content, 400, 250);
        scene.getStylesheets().add(getClass().getResource("/com/gestion/evenements/ui/styles/modernStyle.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void filterOrganizers(String searchText) {
        // Implémentation basique du filtrage
        refreshOrganizersList(); // Pour l'instant, on rafraîchit tout
    }
    
    private void updateStatistics() {
        int totalOrganizers = organisateurs.size();
        int activeOrganizers = (int) organisateurs.stream()
            .filter(org -> !org.getEvenementsOrganises().isEmpty())
            .count();
        
        int totalEvents = organisateurs.stream()
            .mapToInt(org -> org.getEvenementsOrganises().size())
            .sum();
        
        totalOrganizersLabel.setText(String.valueOf(totalOrganizers));
        activeOrganizersLabel.setText(String.valueOf(activeOrganizers));
        totalEventsLabel.setText(String.valueOf(totalEvents));
    }
    
    private void showReports() {
        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT DES ORGANISATEURS ===\n\n");
        
        for (Organisateur org : organisateurs) {
            report.append("Organisateur: ").append(org.getNom()).append("\n");
            report.append("Email: ").append(org.getEmail()).append("\n");
            report.append("Événements: ").append(org.getEvenementsOrganises().size()).append("\n");
            
            if (!org.getEvenementsOrganises().isEmpty()) {
                int totalParticipants = org.getEvenementsOrganises().stream()
                    .mapToInt(e -> e.getParticipants().size())
                    .sum();
                report.append("Participants totaux: ").append(totalParticipants).append("\n");
            }
            report.append("\n");
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rapport des organisateurs");
        alert.setHeaderText("Statistiques détaillées");
        alert.setContentText(report.toString());
        alert.showAndWait();
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
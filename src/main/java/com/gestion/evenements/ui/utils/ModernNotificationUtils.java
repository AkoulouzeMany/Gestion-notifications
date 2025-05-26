package com.gestion.evenements.ui.utils;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Classe utilitaire pour créer des notifications et alertes modernes
 * avec les styles CSS personnalisés
 */
public class ModernNotificationUtils {
    
    private static final String STYLESHEET_PATH = "/com/gestion/evenements/ui/styles/modernStyle.css";
    
    // ======================== ALERTES STYLISÉES ========================
    
    /**
     * Affiche une alerte d'information moderne
     */
    public static void showInfo(String title, String message) {
        showStyledAlert(Alert.AlertType.INFORMATION, title, message, "ℹ️");
    }
    
    /**
     * Affiche une alerte d'erreur moderne
     */
    public static void showError(String title, String message) {
        showStyledAlert(Alert.AlertType.ERROR, title, message, "❌");
    }
    
    /**
     * Affiche une alerte d'avertissement moderne
     */
    public static void showWarning(String title, String message) {
        showStyledAlert(Alert.AlertType.WARNING, title, message, "⚠️");
    }
    
    /**
     * Affiche une alerte de confirmation moderne
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = createStyledAlert(Alert.AlertType.CONFIRMATION, title, message, "❓");
        
        // Personnaliser les boutons
        ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
        
        alert.getButtonTypes().setAll(yesButton, noButton);
        
        // Styliser les boutons
        alert.getDialogPane().lookupButton(yesButton).getStyleClass().add("default");
        alert.getDialogPane().lookupButton(noButton).getStyleClass().add("cancel-button");
        
        return alert.showAndWait()
                   .filter(response -> response == yesButton)
                   .isPresent();
    }
    
    /**
     * Créer une alerte stylisée de base
     */
    private static void showStyledAlert(Alert.AlertType type, String title, String message, String icon) {
        Alert alert = createStyledAlert(type, title, message, icon);
        alert.showAndWait();
    }
    
    /**
     * Créer une alerte avec style moderne
     */
    private static Alert createStyledAlert(Alert.AlertType type, String title, String message, String icon) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.setGraphic(new Label(icon));
        
        // Appliquer le style CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(ModernNotificationUtils.class.getResource(STYLESHEET_PATH).toExternalForm());
        
        // Ajouter la classe CSS selon le type
        switch (type) {
            case INFORMATION:
                dialogPane.getStyleClass().add("information");
                break;
            case ERROR:
                dialogPane.getStyleClass().add("error");
                break;
            case WARNING:
                dialogPane.getStyleClass().add("warning");
                break;
            case CONFIRMATION:
                dialogPane.getStyleClass().add("confirmation");
                break;
        }
        
        // Animation d'apparition
        addDialogAnimation(dialogPane);
        
        return alert;
    }
    
    // ======================== NOTIFICATIONS TOAST ========================
    
    /**
     * Affiche une notification toast de succès
     */
    public static void showSuccessToast(String message) {
        showToast(message, ToastType.SUCCESS, "✅");
    }
    
    /**
     * Affiche une notification toast d'erreur
     */
    public static void showErrorToast(String message) {
        showToast(message, ToastType.ERROR, "❌");
    }
    
    /**
     * Affiche une notification toast d'avertissement
     */
    public static void showWarningToast(String message) {
        showToast(message, ToastType.WARNING, "⚠️");
    }
    
    /**
     * Affiche une notification toast d'information
     */
    public static void showInfoToast(String message) {
        showToast(message, ToastType.INFO, "ℹ️");
    }
    
    /**
     * Types de toast notifications
     */
    public enum ToastType {
        SUCCESS, ERROR, WARNING, INFO
    }
    
    /**
     * Affiche une notification toast moderne
     */
    private static void showToast(String message, ToastType type, String icon) {
        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);
            
            HBox toastContainer = new HBox();
            toastContainer.getStyleClass().addAll("toast-notification", type.name().toLowerCase());
            toastContainer.setAlignment(Pos.CENTER_LEFT);
            toastContainer.setSpacing(12);
            toastContainer.setPadding(new Insets(16, 20, 16, 20));
            
            // Icône
            Label iconLabel = new Label(icon);
            iconLabel.getStyleClass().add("icon-label");
            
            // Message
            Label messageLabel = new Label(message);
            messageLabel.getStyleClass().add("content-label");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(400);
            
            // Bouton fermer
            Button closeButton = new Button("✕");
            closeButton.getStyleClass().add("close-button");
            closeButton.setOnAction(e -> closeToast(toastStage));
            
            toastContainer.getChildren().addAll(iconLabel, messageLabel, closeButton);
            HBox.setHgrow(messageLabel, Priority.ALWAYS);
            
            Scene scene = new Scene(toastContainer);
            scene.setFill(null);
            scene.getStylesheets().add(ModernNotificationUtils.class.getResource(STYLESHEET_PATH).toExternalForm());
            
            toastStage.setScene(scene);
            
            // Positionner en haut à droite
            toastStage.setX(javafx.stage.Screen.getPrimary().getVisualBounds().getMaxX() - 350);
            toastStage.setY(50);
            
            // Animations
            addToastAnimations(toastContainer, toastStage);
            
            toastStage.show();
            
            // Auto-fermeture après 4 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(4000);
                    Platform.runLater(() -> closeToast(toastStage));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }
    
    /**
     * Fermer une notification toast avec animation
     */
    private static void closeToast(Stage stage) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> stage.close());
        fadeOut.play();
    }
    
    // ======================== DIALOG PERSONNALISÉ ========================
    
    /**
     * Créer un dialog personnalisé moderne
     */
    public static Stage createCustomDialog(String title, Node content, boolean modal) {
        Stage dialog = new Stage();
        if (modal) {
            dialog.initModality(Modality.APPLICATION_MODAL);
        }
        dialog.setTitle(title);
        
        VBox dialogContainer = new VBox();
        dialogContainer.getStyleClass().add("custom-dialog");
        
        // Header
        VBox header = new VBox();
        header.getStyleClass().add("header-panel");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(32));
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title");
        header.getChildren().add(titleLabel);
        
        // Content
        VBox contentContainer = new VBox();
        contentContainer.getStyleClass().add("content");
        contentContainer.getChildren().add(content);
        
        dialogContainer.getChildren().addAll(header, contentContainer);
        
        Scene scene = new Scene(dialogContainer);
        scene.getStylesheets().add(ModernNotificationUtils.class.getResource(STYLESHEET_PATH).toExternalForm());
        dialog.setScene(scene);
        
        // Animation
        addDialogAnimation(dialogContainer);
        
        return dialog;
    }
    
    // ======================== SNACKBAR ========================
    
    /**
     * Affiche une snackbar moderne avec action
     */
    public static void showSnackbar(String message, String actionText, Runnable action) {
        Platform.runLater(() -> {
            Stage snackbarStage = new Stage();
            snackbarStage.initStyle(StageStyle.TRANSPARENT);
            snackbarStage.setAlwaysOnTop(true);
            
            HBox snackbarContainer = new HBox();
            snackbarContainer.getStyleClass().add("snackbar");
            snackbarContainer.setAlignment(Pos.CENTER_LEFT);
            snackbarContainer.setSpacing(16);
            
            // Message
            Label messageLabel = new Label(message);
            messageLabel.getStyleClass().add("message-label");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(400);
            
            HBox.setHgrow(messageLabel, Priority.ALWAYS);
            
            snackbarContainer.getChildren().add(messageLabel);
            
            // Bouton d'action si fourni
            if (actionText != null && action != null) {
                Button actionButton = new Button(actionText);
                actionButton.getStyleClass().add("action-button");
                actionButton.setOnAction(e -> {
                    action.run();
                    snackbarStage.close();
                });
                snackbarContainer.getChildren().add(actionButton);
            }
            
            Scene scene = new Scene(snackbarContainer);
            scene.setFill(null);
            scene.getStylesheets().add(ModernNotificationUtils.class.getResource(STYLESHEET_PATH).toExternalForm());
            
            snackbarStage.setScene(scene);
            
            // Positionner en bas au centre
            double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
            
            snackbarStage.setX((screenWidth - 400) / 2);
            snackbarStage.setY(screenHeight - 120);
            
            // Animation d'entrée
            addSnackbarAnimation(snackbarContainer);
            
            snackbarStage.show();
            
            // Auto-fermeture après 5 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Platform.runLater(snackbarStage::close);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }
    
    // ======================== ANIMATIONS ========================
    
    /**
     * Ajouter animation d'apparition pour les dialogs
     */
    private static void addDialogAnimation(Node node) {
        // Animation de scale et fade
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), node);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        
        scaleTransition.play();
        fadeTransition.play();
    }
    
    /**
     * Ajouter animations pour les toasts
     */
    private static void addToastAnimations(Node node, Stage stage) {
        // Animation d'entrée depuis la droite
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), node);
        slideIn.setFromX(100);
        slideIn.setToX(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        slideIn.play();
        fadeIn.play();
    }
    
    /**
     * Ajouter animation pour les snackbars
     */
    private static void addSnackbarAnimation(Node node) {
        // Animation d'entrée depuis le bas
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(350), node);
        slideUp.setFromY(50);
        slideUp.setToY(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        slideUp.play();
        fadeIn.play();
    }
    
    // ======================== PROGRESS DIALOG ========================
    
    /**
     * Créer un dialog de progression moderne
     */
    public static Stage createProgressDialog(String title, String message) {
        Stage progressStage = new Stage();
        progressStage.initModality(Modality.APPLICATION_MODAL);
        progressStage.initStyle(StageStyle.UNDECORATED);
        progressStage.setTitle(title);
        
        VBox container = new VBox();
        container.getStyleClass().add("progress-dialog");
        container.setAlignment(Pos.CENTER);
        container.setSpacing(20);
        container.setPadding(new Insets(40));
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");
        
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5f6368;");
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.getStyleClass().add("progress-bar");
        progressBar.setPrefWidth(300);
        
        // Rendre la barre indéterminée pour une animation continue
        progressBar.setProgress(-1);
        
        container.getChildren().addAll(titleLabel, messageLabel, progressBar);
        
        Scene scene = new Scene(container);
        scene.getStylesheets().add(ModernNotificationUtils.class.getResource(STYLESHEET_PATH).toExternalForm());
        progressStage.setScene(scene);
        
        addDialogAnimation(container);
        
        return progressStage;
    }
    
    // ======================== MÉTHODES UTILITAIRES ========================
    
    /**
     * Créer un separator stylisé
     */
    public static Separator createStyledSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("modern-separator");
        return separator;
    }
    
    /**
     * Appliquer un tooltip moderne à un nœud
     */
    public static void addModernTooltip(Node node, String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.getStyleClass().add("modern-tooltip");
        Tooltip.install(node, tooltip);
    }

    /*public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        System.out.println("✅ PrimaryStage configuré pour les notifications");
}*/
}
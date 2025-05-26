package com.gestion.evenements.observer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

/**
 * Observer pour mettre à jour l'interface utilisateur
 * Implémente ParticipantObserver pour recevoir les notifications du modèle
 */
public class UIObserver implements ParticipantObserver {
    private Label statusLabel;
    private Runnable refreshCallback;
    
    public UIObserver(Label statusLabel, Runnable refreshCallback) {
        this.statusLabel = statusLabel;
        this.refreshCallback = refreshCallback;
    }
    
    public UIObserver(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }
    
    @Override
    public void notifier(String message) {
        // Utiliser Platform.runLater pour mettre à jour l'UI depuis un thread différent
        Platform.runLater(() -> {
            // Mettre à jour le label de statut si disponible
            if (statusLabel != null) {
                statusLabel.setText(message);
                statusLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: 600;");
                
                // Effacer le message après 3 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        Platform.runLater(() -> statusLabel.setText(""));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
            
            // Afficher une notification popup pour les actions importantes
            if (message.contains("ajouté") || message.contains("retiré") || message.contains("annulé")) {
                showNotification(message);
            }
            
            // Rafraîchir l'interface
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        });
    }
    
    private void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
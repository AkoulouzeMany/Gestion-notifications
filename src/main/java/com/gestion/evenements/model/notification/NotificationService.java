package com.gestion.evenements.model.notification;

import java.util.concurrent.CompletableFuture;

public interface NotificationService {
    void envoyerNotification(String message);
    CompletableFuture<Void> envoyerNotificationAsync(String message);
}


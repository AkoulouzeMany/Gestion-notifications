module com.gestion.evenements {
    // Modules JavaFX requis
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    
    // Modules Jackson pour JSON
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.annotation;
    
    // Modules Java standard
    requires java.xml;
    requires java.desktop;
    
    // Exporter les packages principaux
    exports com.gestion.evenements;
    exports com.gestion.evenements.ui;
    exports com.gestion.evenements.model;
    exports com.gestion.evenements.controller;
    exports com.gestion.evenements.observer;
    exports com.gestion.evenements.util;
    exports com.gestion.evenements.serialization;
    
    // IMPORTANT: Exporter les packages pour Jackson
    exports com.gestion.evenements.auth to com.fasterxml.jackson.databind;
    exports com.gestion.evenements.model.evenementparticulier to com.fasterxml.jackson.databind;
    
    // Ouvrir les packages pour la réflexion JavaFX
    opens com.gestion.evenements to javafx.fxml, javafx.base;
    opens com.gestion.evenements.ui to javafx.fxml, javafx.base;
    opens com.gestion.evenements.model to javafx.fxml, javafx.base;
    opens com.gestion.evenements.controller to javafx.fxml, javafx.base;
    
    // CRUCIAL: Ouvrir les packages pour Jackson (sérialisation/désérialisation)
    opens com.gestion.evenements.auth to com.fasterxml.jackson.databind, com.fasterxml.jackson.core;
    //opens com.gestion.evenements.model to com.fasterxml.jackson.databind, com.fasterxml.jackson.core;
    opens com.gestion.evenements.model.evenementparticulier to com.fasterxml.jackson.databind, com.fasterxml.jackson.core;
}
package com.gestion.evenements.serialization;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gestion.evenements.model.Evenement;
import com.gestion.evenements.model.Participant;
import com.gestion.evenements.model.evenementparticulier.Conference;
import com.gestion.evenements.model.evenementparticulier.Concert;
import com.gestion.evenements.auth.User;

/**
 * Gestionnaire de sérialisation pour les événements et utilisateurs
 * Supporte les formats JSON et XML avec sauvegarde automatique
 */
public class SerializationManager {
    private static final ObjectMapper objectMapper;
    private static final String DEFAULT_BACKUP_DIR = "backups";
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(
            com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false
        );
        objectMapper.configure(
            com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT, true
        );
    }
    
    // ================================
    // MÉTHODES JSON
    // ================================
    
    /**
     * Sauvegarde les événements au format JSON
     */
    public static void sauvegarderEvenementsJSON(Map<String, Evenement> evenements, String fichier) 
            throws IOException {
        createBackupIfExists(fichier);
        objectMapper.writeValue(new File(fichier), evenements);
        System.out.println("✅ Événements sauvegardés en JSON: " + fichier);
    }
    
    /**
     * Charge les événements depuis un fichier JSON
     */
    public static Map<String, Evenement> chargerEvenementsJSON(String fichier) throws IOException {
        File file = new File(fichier);
        if (!file.exists()) {
            System.out.println("⚠️ Fichier non trouvé: " + fichier + " - Création d'une map vide");
            return new HashMap<>();
        }
        
        Map<String, Evenement> evenements = objectMapper.readValue(file, 
            objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Evenement.class));
        
        System.out.println("✅ " + evenements.size() + " événements chargés depuis JSON: " + fichier);
        return evenements;
    }
    
    /**
     * Sauvegarde les utilisateurs au format JSON
     */
    public static void sauvegarderUtilisateursJSON(Map<String, User> utilisateurs, String fichier) 
            throws IOException {
        createBackupIfExists(fichier);
        objectMapper.writeValue(new File(fichier), utilisateurs);
        System.out.println("✅ Utilisateurs sauvegardés en JSON: " + fichier);
    }
    
    /**
     * Charge les utilisateurs depuis un fichier JSON
     */
    public static Map<String, User> chargerUtilisateursJSON(String fichier) throws IOException {
        File file = new File(fichier);
        if (!file.exists()) {
            return new HashMap<>();
        }
        
        Map<String, User> utilisateurs = objectMapper.readValue(file, 
            objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, User.class));
        
        System.out.println("✅ " + utilisateurs.size() + " utilisateurs chargés depuis JSON: " + fichier);
        return utilisateurs;
    }
    
    // ================================
    // MÉTHODES XML
    // ================================
    
    /**
     * Sauvegarde les événements au format XML
     */
    public static void sauvegarderEvenementsXML(Map<String, Evenement> evenements, String fichier) 
            throws Exception {
        createBackupIfExists(fichier);
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        
        // Élément racine
        Element rootElement = doc.createElement("evenements");
        rootElement.setAttribute("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        rootElement.setAttribute("count", String.valueOf(evenements.size()));
        doc.appendChild(rootElement);
        
        // Ajouter chaque événement
        for (Map.Entry<String, Evenement> entry : evenements.entrySet()) {
            Element evenementElement = createEvenementElement(doc, entry.getValue());
            rootElement.appendChild(evenementElement);
        }
        
        // Écrire le document XML
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fichier));
        transformer.transform(source, result);
        
        System.out.println("✅ Événements sauvegardés en XML: " + fichier);
    }
    
    /**
     * Charge les événements depuis un fichier XML
     */
    public static Map<String, Evenement> chargerEvenementsXML(String fichier) throws Exception {
        File file = new File(fichier);
        if (!file.exists()) {
            return new HashMap<>();
        }
        
        Map<String, Evenement> evenements = new HashMap<>();
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        
        NodeList evenementNodes = doc.getElementsByTagName("evenement");
        
        for (int i = 0; i < evenementNodes.getLength(); i++) {
            Node evenementNode = evenementNodes.item(i);
            if (evenementNode.getNodeType() == Node.ELEMENT_NODE) {
                Evenement evenement = parseEvenementFromXML((Element) evenementNode);
                if (evenement != null) {
                    evenements.put(evenement.getId(), evenement);
                }
            }
        }
        
        System.out.println("✅ " + evenements.size() + " événements chargés depuis XML: " + fichier);
        return evenements;
    }
    
    /**
     * Sauvegarde les utilisateurs au format XML
     */
    public static void sauvegarderUtilisateursXML(Map<String, User> utilisateurs, String fichier) 
            throws Exception {
        createBackupIfExists(fichier);
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        
        // Élément racine
        Element rootElement = doc.createElement("utilisateurs");
        rootElement.setAttribute("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        rootElement.setAttribute("count", String.valueOf(utilisateurs.size()));
        doc.appendChild(rootElement);
        
        // Ajouter chaque utilisateur
        for (Map.Entry<String, User> entry : utilisateurs.entrySet()) {
            Element userElement = createUtilisateurElement(doc, entry.getValue());
            rootElement.appendChild(userElement);
        }
        
        // Écrire le document XML
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fichier));
        transformer.transform(source, result);
        
        System.out.println("✅ Utilisateurs sauvegardés en XML: " + fichier);
    }
    
    // ================================
    // MÉTHODES D'EXPORT COMBINÉ
    // ================================
    
    /**
     * Exporte tous les événements dans les deux formats
     */
    public static void exporterEvenements(Map<String, Evenement> evenements, String nomBase) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            // Export JSON
            String fichierJSON = nomBase + "_" + timestamp + ".json";
            sauvegarderEvenementsJSON(evenements, fichierJSON);
            
            // Export XML
            String fichierXML = nomBase + "_" + timestamp + ".xml";
            sauvegarderEvenementsXML(evenements, fichierXML);
            
            System.out.println("✅ Export complet terminé: " + evenements.size() + " événements");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'export: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Exporte tous les utilisateurs dans les deux formats
     */
    public static void exporterUtilisateurs(Map<String, User> utilisateurs, String nomBase) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            // Export JSON
            String fichierJSON = nomBase + "_" + timestamp + ".json";
            sauvegarderUtilisateursJSON(utilisateurs, fichierJSON);
            
            // Export XML
            String fichierXML = nomBase + "_" + timestamp + ".xml";
            sauvegarderUtilisateursXML(utilisateurs, fichierXML);
            
            System.out.println("✅ Export utilisateurs complet: " + utilisateurs.size() + " utilisateurs");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'export utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ================================
    
    private static Element createEvenementElement(Document doc, Evenement evenement) {
        Element evenementElement = doc.createElement("evenement");
        evenementElement.setAttribute("id", evenement.getId());
        evenementElement.setAttribute("type", getEvenementType(evenement));
        
        // Informations de base
        addElementWithText(doc, evenementElement, "nom", evenement.getNom());
        addElementWithText(doc, evenementElement, "date", evenement.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        addElementWithText(doc, evenementElement, "lieu", evenement.getLieu());
        addElementWithText(doc, evenementElement, "capaciteMax", String.valueOf(evenement.getCapaciteMax()));
        
        // Participants
        Element participantsElement = doc.createElement("participants");
        participantsElement.setAttribute("count", String.valueOf(evenement.getParticipants().size()));
        
        for (Participant participant : evenement.getParticipants()) {
            Element participantElement = doc.createElement("participant");
            participantElement.setAttribute("id", participant.getId());
            addElementWithText(doc, participantElement, "nom", participant.getNom());
            addElementWithText(doc, participantElement, "email", participant.getEmail());
            participantsElement.appendChild(participantElement);
        }
        evenementElement.appendChild(participantsElement);
        
        // Détails spécifiques selon le type
        if (evenement instanceof Conference) {
            Conference conf = (Conference) evenement;
            addElementWithText(doc, evenementElement, "theme", conf.getTheme());
        } else if (evenement instanceof Concert) {
            Concert concert = (Concert) evenement;
            addElementWithText(doc, evenementElement, "artiste", concert.getArtiste());
            addElementWithText(doc, evenementElement, "genreMusical", concert.getGenreMusical());
        }
        
        return evenementElement;
    }
    
    private static Element createUtilisateurElement(Document doc, User user) {
        Element userElement = doc.createElement("utilisateur");
        userElement.setAttribute("id", user.getId());
        userElement.setAttribute("role", user.getRole().toString());
        
        addElementWithText(doc, userElement, "nom", user.getNom());
        addElementWithText(doc, userElement, "email", user.getEmail());
        
        if (user.getTelephone() != null) {
            addElementWithText(doc, userElement, "telephone", user.getTelephone());
        }
        
        if (user.getOrganisation() != null) {
            addElementWithText(doc, userElement, "organisation", user.getOrganisation());
        }
        
        if (user.getDateCreation() != null) {
            addElementWithText(doc, userElement, "dateCreation", 
                user.getDateCreation().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        addElementWithText(doc, userElement, "actif", String.valueOf(user.isActif()));
        
        return userElement;
    }
    
    private static void addElementWithText(Document doc, Element parent, String tagName, String textContent) {
        if (textContent != null) {
            Element element = doc.createElement(tagName);
            element.setTextContent(textContent);
            parent.appendChild(element);
        }
    }
    
    private static String getEvenementType(Evenement evenement) {
        if (evenement instanceof Conference) {
            return "conference";
        } else if (evenement instanceof Concert) {
            return "concert";
        }
        return "unknown";
    }
    
    private static Evenement parseEvenementFromXML(Element evenementElement) {
        try {
            String id = evenementElement.getAttribute("id");
            String type = evenementElement.getAttribute("type");
            
            String nom = getElementText(evenementElement, "nom");
            String dateStr = getElementText(evenementElement, "date");
            String lieu = getElementText(evenementElement, "lieu");
            int capaciteMax = Integer.parseInt(getElementText(evenementElement, "capaciteMax"));
            
            LocalDateTime date = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            Evenement evenement;
            
            if ("conference".equals(type)) {
                String theme = getElementText(evenementElement, "theme");
                evenement = new Conference(id, nom, date, lieu, capaciteMax, theme != null ? theme : "");
            } else if ("concert".equals(type)) {
                String artiste = getElementText(evenementElement, "artiste");
                String genre = getElementText(evenementElement, "genreMusical");
                evenement = new Concert(id, nom, date, lieu, capaciteMax, 
                    artiste != null ? artiste : "", genre != null ? genre : "");
            } else {
                return null; // Type non reconnu
            }
            
            // Charger les participants
            NodeList participantsNodes = evenementElement.getElementsByTagName("participants");
            if (participantsNodes.getLength() > 0) {
                Element participantsElement = (Element) participantsNodes.item(0);
                NodeList participantNodes = participantsElement.getElementsByTagName("participant");
                
                for (int i = 0; i < participantNodes.getLength(); i++) {
                    Element participantElement = (Element) participantNodes.item(i);
                    String participantId = participantElement.getAttribute("id");
                    String participantNom = getElementText(participantElement, "nom");
                    String participantEmail = getElementText(participantElement, "email");
                    
                    Participant participant = new Participant(participantId, participantNom, participantEmail);
                    try {
                        evenement.ajouterParticipant(participant);
                    } catch (Exception e) {
                        System.err.println("⚠️ Impossible d'ajouter le participant: " + e.getMessage());
                    }
                }
            }
            
            return evenement;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du parsing XML d'un événement: " + e.getMessage());
            return null;
        }
    }
    
    private static String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }
    
    private static void createBackupIfExists(String fichier) {
        File file = new File(fichier);
        if (file.exists()) {
            try {
                // Créer le répertoire de sauvegarde s'il n'existe pas
                File backupDir = new File(DEFAULT_BACKUP_DIR);
                if (!backupDir.exists()) {
                    backupDir.mkdirs();
                }
                
                // Créer le nom de sauvegarde avec timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String backupName = file.getName().replaceFirst("\\.", "_backup_" + timestamp + ".");
                File backupFile = new File(backupDir, backupName);
                
                // Copier le fichier
                java.nio.file.Files.copy(file.toPath(), backupFile.toPath());
                System.out.println("📁 Sauvegarde créée: " + backupFile.getPath());
                
            } catch (Exception e) {
                System.err.println("⚠️ Impossible de créer la sauvegarde: " + e.getMessage());
            }
        }
    }
}
package com.monopoly.Monopoly.models.plateau;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Carte {
    private static int compteur = 0;
    private int id;
    @JsonProperty
    private String typeCarte, nom;

    @JsonProperty("action")
    private String aFaire;

    @JsonProperty("texte")
    private String description;

    @JsonProperty("value")
    private Object valeur;

    Carte(int id, String typeCarte, String description, String aFaire) {
        this.id = id;
        this.typeCarte = typeCarte;
        this.description = description;
        this.aFaire = aFaire;
    }

    Carte(String typeCarte, String description, String aFaire) {
        this(compteur++, typeCarte, description, aFaire);
    }

    Carte(){
        // Pour Jackson
    }

    // -------------------------------
    // 🔧 Getters / Setters
    // -------------------------------

    public int getId() {
        return id;
    }

    public String getTypeCarte() {
        return typeCarte;
    }

    public String getDescription() {
        return description;
    }

    public String getAFaire() {
        return aFaire;
    }

    public Object getValeur() {
        return valeur;
    }

    public String getNom() {
        return nom;
    }



    @Override
    public String toString() {
        return "Carte{" +
                "id=" + id +
                ", typeCarte=" + typeCarte +
                ", description='" + description + '\'' +
                ", aFaire='" + aFaire + '\'' +
                '}';
    }
}

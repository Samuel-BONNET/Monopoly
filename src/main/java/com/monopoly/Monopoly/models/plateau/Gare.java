package com.monopoly.Monopoly.models.plateau;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Gare implements IPossession {

    @JsonProperty
    private int id, numero;
    @JsonProperty
    private String nom;
    private static int compteur = 0;
    private boolean estHypotheque = false;
    private int prix;

    Gare(int id, String nom) {
        this.id = id;
        this.nom = nom;
        this.prix = 200;
    }

    Gare(String nom) {
        this(compteur++, nom);
    }

    // -------------------------------
    // 🔧 Getters / Setters
    // -------------------------------

    public int getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public String getNom() {
        return nom;
    }

    public int getPrixAchat() {
        return prix;
    }

    public boolean isEstHypotheque() {
        return estHypotheque;
    }

    public void setEstHypotheque(boolean estHypotheque) {
        this.estHypotheque = estHypotheque;
    }



    public int calculerLoyer() {
        return 25; // Le loyer de base pour une gare est de 25
    }
}

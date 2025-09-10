package com.monopoly.Monopoly.models.plateau;

public class Gare implements IPossession {
    private static int compteur = 0;
    private int id, prix;
    private String nom;
    private boolean estHypotheque = false;

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

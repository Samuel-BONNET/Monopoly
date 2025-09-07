package com.monopoly.Monopoly.models.plateau;

public class ServicePublic implements IPossession {

    private static int compteur = 0;
    private int id, prix;
    private String nom;

    ServicePublic(int id, String nom) {
        this.id = id;
        this.nom = nom;
        this.prix = 150;
    }

    ServicePublic(String nom) {
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
}

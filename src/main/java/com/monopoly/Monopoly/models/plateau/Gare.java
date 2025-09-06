package com.monopoly.Monopoly.models.plateau;

public class Gare implements Possession {
    private static int compteur = 0;
    private int id, prix;
    private String nom;

    Gare(int id, String nom) {
        this.id = id;
        this.nom = nom;
        this.prix = 200;
    }

    Gare(String nom) {
        this(compteur++, nom);
    }

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

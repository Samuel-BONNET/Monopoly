package com.monopoly.Monopoly.models.plateau;

public class ServicePublic implements IPossession {

    private static int compteur = 0;
    private int id, numero, prix;
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

    public int getNumero() {
        return numero;
    }

    public String getNom() {
        return nom;
    }

    public int getPrixAchat() {
        return prix;
    }

    public boolean getEstHypotheque() {
        return false;
    }

    public void setEstHypotheque(boolean estHypotheque) {
        // Ne fait rien, un service public ne peut pas être hypothéqué
    }

    public int calculerLoyer() {
        // ne rien faire
        return 0;
    }
}

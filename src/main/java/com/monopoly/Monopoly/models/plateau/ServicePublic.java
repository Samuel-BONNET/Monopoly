package com.monopoly.Monopoly.models.plateau;

import com.monopoly.Monopoly.models.Joueur;

public class ServicePublic implements IPossession {

    private static int compteur = 0;
    private int id, numero, prix;
    private String nom;
    private Joueur proprietaire;

    ServicePublic(int id, String nom) {
        this.id = id;
        this.nom = nom;
        this.prix = 150;
        this.proprietaire = null;
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

    public Joueur getProprietaire() {
        return this.proprietaire;
    }

    public void setProprietaire(Joueur joueur){
        this.proprietaire = joueur;
    }
}

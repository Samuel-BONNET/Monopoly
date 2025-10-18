package com.monopoly.Monopoly.models.plateau;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopoly.Monopoly.models.Joueur;

public class Gare implements IPossession {

    @JsonProperty
    private int id, numero;
    @JsonProperty
    private String nom;
    private static int compteur = 0;
    private boolean estHypotheque = false;
    private int prix;
    private Joueur proprietaire;

    Gare(int id, String nom) {
        this.id = id;
        this.nom = nom;
        this.prix = 200;
        this.proprietaire = null;
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

    public boolean getEstHypothequee() {
        return estHypotheque;
    }

    public void setEstHypotheque(boolean estHypotheque) {
        this.estHypotheque = estHypotheque;
    }

    public Joueur getProprietaire() {
        return this.proprietaire;
    }

    public void setProprietaire(Joueur proprietaire) {
        this.proprietaire = proprietaire;
    }

    public int getLoyerAPayer() {
        if(proprietaire != null) {
            int nbGare = proprietaire.getNbGare();
            if(nbGare == 0) return 0;
            return (int) (Math.pow(2,nbGare-1)*50);
        }
        return 0;
    }

    public void setEstHypothequee(boolean valeur){
        this.estHypotheque = valeur;
    }
}

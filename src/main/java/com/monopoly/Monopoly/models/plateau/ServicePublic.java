package com.monopoly.Monopoly.models.plateau;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.monopoly.Monopoly.models.Joueur;

public class ServicePublic implements IPossession {

    private static int compteur = 0;
    private int id, numero, prix;
    private String nom;
    @JsonIgnore
    private Joueur proprietaire;
    private boolean estHypothequee = false;

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

    public boolean getEstHypothequee() {
        return false;
    }

    public int getLoyerAPayer() {
        return 0; //evenement custom
    }

    public Joueur getProprietaire() {
        return this.proprietaire;
    }

    public void setProprietaire(Joueur joueur){
        this.proprietaire = joueur;
    }

    public void setEstHypothequee(boolean valeur){
        this.estHypothequee = valeur;
    }
}

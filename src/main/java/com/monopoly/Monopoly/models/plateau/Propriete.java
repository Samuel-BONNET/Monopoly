package com.monopoly.Monopoly.models.plateau;

import com.monopoly.Monopoly.models.Joueur;

public class Propriete implements IPossession {

    private static int compteur_id = 0;
    private int id, prix_achat, loyer, loyerGroupeComplet, loyer_maison1, loyer_maison2, loyer_maison3, loyer_maison4, loyer_hotel, prix_maison, prix_hotel, nb_maisons, nb_hotel;
    private String nom, quartier;
    private Joueur proprietaire;
    private boolean estHypothequee,estGroupeComplet,estHotel;

    public Propriete(int id, int prix_achat, int loyer, int loyerGroupeComplet, int loyer_maison1, int loyer_maison2, int loyer_maison3, int loyer_maison4, int loyer_hotel, int prix_maison, int prix_hotel, String nom, String quartier){
        this.id = id;
        this.prix_achat = prix_achat;
        this.loyer = loyer;
        this.loyer_maison1 = loyer_maison1;
        this.loyer_maison2 = loyer_maison2;
        this.loyer_maison3 = loyer_maison3;
        this.loyer_maison4 = loyer_maison4;
        this.loyer_hotel = loyer_hotel;
        this.prix_maison = prix_maison;
        this.prix_hotel = prix_hotel;
        this.nom = nom;
        this.quartier = quartier;
        this.nb_maisons = 0;
        this.loyerGroupeComplet = loyerGroupeComplet;
        this.estGroupeComplet = false;
        this.estHotel = false;
    }

    public Propriete( int prix_achat, int loyer, int loyerGroupeComplet, int maison1, int maison2, int maison3, int maison4, int hotel, int prix_maison, int prix_hotel, String nom, String quartier){
        this(compteur_id++, prix_achat, loyer, loyerGroupeComplet, maison1, maison2, maison3, maison4, hotel, prix_maison, prix_hotel, nom, quartier);
    }

    // -------------------------------
    // 🔧 Getters / Setters
    // -------------------------------

    public int getId() {
        return id;
    }

    public int getPrixAchat() {
        return prix_achat;
    }

    public int getLoyer() {
        return loyer;
    }

    public int getLoyer_maison1() {
        return loyer_maison1;
    }

    public int getLoyer_maison2() {
        return loyer_maison2;
    }

    public int getLoyer_maison3() {
        return loyer_maison3;
    }

    public int getLoyer_maison4() {
        return loyer_maison4;
    }

    public int getLoyer_hotel() {
        return loyer_hotel;
    }

    public int getPrix_maison() {
        return prix_maison;
    }

    public int getPrix_hotel() {
        return prix_hotel;
    }

    public int getNb_maisons() {
        return nb_maisons;
    }

    public void setNb_maisons(int nb_maisons) {
        this.nb_maisons = nb_maisons;
    }

    public int getNb_hotel() {
        return nb_hotel;
    }

    public void setNb_hotel(int nb_hotel) {
        this.nb_hotel = nb_hotel;
    }

    public String getNom() {
        return nom;
    }

    public String getQuartier() {
        return quartier;
    }

    public boolean getEstHypothequee() {
        return estHypothequee;
    }

    public void setEstHypothequee(boolean estHypothequee) {
        this.estHypothequee = estHypothequee;
    }

    public Joueur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Joueur proprietaire) {
        this.proprietaire = proprietaire;
    }

    public boolean getEstGroupeComplet() {
        return estGroupeComplet;
    }

    public void setGroupeComplet(boolean groupe_complet) {
        this.estGroupeComplet = groupe_complet;
    }

    public boolean getHasHotel() {
        return estHotel;
    }

    public void setHotel(boolean hotel) {
        this.estHotel = hotel;
    }


    // others

    public int calculerLoyer(){
        // Si hotel
        if(estHotel){
            return loyer_hotel;
        }
        else{
            // Si maison
            if(nb_maisons > 0){
                switch (nb_maisons) {
                    case 1:
                        return loyer_maison1;
                    case 2:
                        return loyer_maison2;
                    case 3:
                        return loyer_maison3;
                    case 4:
                        return loyer_maison4;
                    default:
                        return loyer; // au cas où
                }
            }
            else{
                // Si groupe complet
                if(estGroupeComplet){
                    return loyerGroupeComplet;
                }
                else{
                    // Sinon loyer de base
                    return loyer;
                }
            }
        }
    }
}

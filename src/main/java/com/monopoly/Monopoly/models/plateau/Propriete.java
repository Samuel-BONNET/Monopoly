package com.monopoly.Monopoly.models.plateau;

public class Propriete implements Possession{
    private static int compteur_id = 0;
    private int id, prix_achat, loyer, groupe_complet, loyer_maison1, loyer_maison2, loyer_maison3, loyer_maison4, loyer_hotel, prix_maison, prix_hotel, nb_maisons, nb_hotel;
    private String nom, quartier;

    public Propriete(int id, int prix_achat, int loyer, int groupe_complet, int loyer_maison1, int loyer_maison2, int loyer_maison3, int loyer_maison4, int loyer_hotel, int prix_maison, int prix_hotel, String nom, String quartier){
        this.id = id;
        this.prix_achat = prix_achat;
        this.loyer = loyer;
        this.groupe_complet = groupe_complet;
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
        this.nb_hotel = 0;
    }

    public Propriete( int prix_achat, int loyer, int groupe_complet, int maison1, int maison2, int maison3, int maison4, int hotel, int prix_maison, int prix_hotel, String nom, String quartier){
        this(compteur_id++, prix_achat, loyer, groupe_complet, maison1, maison2, maison3, maison4, hotel, prix_maison, prix_hotel, nom, quartier);
    }

    public int getId() {
        return id;
    }

    public int getPrixAchat() {
        return prix_achat;
    }

    public int getLoyer() {
        return loyer;
    }

    public int getGroupe_complet() {
        return groupe_complet;
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

    public int getNb_hotel() {
        return nb_hotel;
    }

    public String getNom() {
        return nom;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setNb_hotel(int nb_hotel) {
        this.nb_hotel = nb_hotel;
    }

    public void setNb_maisons(int nb_maisons) {
        this.nb_maisons = nb_maisons;
    }
}

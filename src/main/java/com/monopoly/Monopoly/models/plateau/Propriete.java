package com.monopoly.Monopoly.models.plateau;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopoly.Monopoly.models.Joueur;

public class Propriete implements IPossession {

    private static int compteurId = 0;
    @JsonProperty
    private int id, numero, prixAchat, loyer, loyerGroupeComplet, loyerHotel, prixMaison, prixHotel;
    @JsonProperty
    private int[]loyerMaison;

    private int nbMaisons, nbHotel;
    private String nom, quartier;
    private Joueur proprietaire;
    private boolean estHypothequee,estGroupeComplet,estHotel;

    public Propriete(int id, int prixAchat, int loyer, int loyerGroupeComplet, int[] loyerMaison, int loyerHotel, int prixMaison, int prixHotel, String nom, String quartier){
        this.id = id;
        this.prixAchat = prixAchat;
        this.loyer = loyer;
        this.loyerMaison = loyerMaison;
        this.loyerHotel = loyerHotel;
        this.prixMaison = prixMaison;
        this.prixHotel = prixHotel;
        this.nom = nom;
        this.quartier = quartier;
        this.nbMaisons = 0;
        this.loyerGroupeComplet = loyerGroupeComplet;
        this.estGroupeComplet = false;
        this.estHotel = false;
    }

    public Propriete( int prixAchat, int loyer, int loyerGroupeComplet, int[] loyerMaison, int hotel, int prixMaison, int prixHotel, String nom, String quartier){
        this(compteurId++, prixAchat, loyer, loyerGroupeComplet, loyerMaison, hotel, prixMaison, prixHotel, nom, quartier);
    }

    public Propriete(){
        // Pour Jackson
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

    public int getPrixAchat() {
        return prixAchat;
    }

    public int getLoyer() {
        return loyer;
    }

    public int[] loyerMaison() {
        return loyerMaison;
    }

    public int getLoyer_hotel() {
        return loyerHotel;
    }

    public int getPrix_maison() {
        return prixMaison;
    }

    public int getPrix_hotel() {
        return prixHotel;
    }

    public int getNb_maisons() {
        return nbMaisons;
    }

    public void setNb_maisons(int nbMaisons) {
        this.nbMaisons = nbMaisons;
    }

    public int getNb_hotel() {
        return nbHotel;
    }

    public void setNb_hotel(int nbHotel) {
        this.nbHotel = nbHotel;
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
            return loyerHotel;
        }
        else{
            // Si maison
            if(nbMaisons > 0){
                switch (nbMaisons) {
                    case 1:
                        return loyerMaison[0];
                    case 2:
                        return loyerMaison[1];
                    case 3:
                        return loyerMaison[2];
                    case 4:
                        return loyerMaison[3];
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

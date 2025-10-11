package com.monopoly.Monopoly.models;


import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.monopoly.Monopoly.models.enums.Argent;
import com.monopoly.Monopoly.models.plateau.ICase;
import com.monopoly.Monopoly.models.plateau.IPossession;
import com.monopoly.Monopoly.models.plateau.Propriete;

public class Joueur {
    public Scanner scan;
    private static int compteur_id = 0;
    private int id, caseActuelle = 0, capitalTotal = 1500, nbPropriete = 0, nbMaison = 0, nbHotel = 0, nbGare =0, nbService = 0, tourEntrePrison, cptDouble = 0, lancerDesRestant = 1 ;
    private String nom, pion;
    private Propriete[] listeProprietes = new Propriete[25];// nb max de propriétés
    private boolean estEnPrison = false, estEliminer = false;

    public Joueur(int id, String nom, String pion) {
        this.id = id;
        this.nom = nom;
        this.pion = pion;
    }

    public Joueur(String nom, String pion) {
        this(compteur_id++, nom, pion);
    }

    public Joueur(){
        this(compteur_id++, "", "");
    }



    // -------------------------------
    // 🔧 Getters / Setters
    // -------------------------------

    public static int getCompteur_id() {
        return compteur_id;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPion() {
        return pion;
    }

    public int getLancerDesRestant(){
        return lancerDesRestant;
    }

    public int incrLancerDes(){
        if(lancerDesRestant < 1) lancerDesRestant++;
        return lancerDesRestant;
    }

    public int decrLancerDes(){
        if(lancerDesRestant > 0) lancerDesRestant--;
        return lancerDesRestant;
    }

    public int getCaseActuelle() {
        return this.caseActuelle;
    }

    public void setCaseActuelle(int caseActuelle) {
        this.caseActuelle = caseActuelle;
    }

    public boolean getEstEnPrison() {
        return estEnPrison;
    }

    public void setEstEnPrison(boolean estEnPrison) {
        this.estEnPrison = estEnPrison;
    }

    public boolean getEstEliminer() {
        return estEliminer;
    }

    public void setEstEliminer(boolean estEliminer) {
        this.estEliminer = estEliminer;
    }

    public Propriete[] getListe_proprietes() {
        return listeProprietes;
    }

    public Propriete getPropriete(int id){
        return listeProprietes[id];
    }

    public int getNbPropriete() {
        return nbPropriete;
    }

    public int getNbMaison() {
        return nbMaison;
    }

    public int getNbHotel() {
        return nbHotel;
    }

    public int getCapitalTotal() {
        return capitalTotal;
    }

    public int getNbGare() {
        return nbGare;
    }

    public void setNbGare(int nbGare) {
        this.nbGare = nbGare;
    }

    public int getNbService() {
        return nbService;
    }

    public void setNbService(int nbService) {
        this.nbService = nbService;
    }

    public int getNbTourEntrePrison() {
        return tourEntrePrison;
    }

    public int getCptDouble(){
        return cptDouble;
    }

    public void incrCptDouble(int tour){
        if(cptDouble == 2){
            this.allerEnPrison(tour);
        }
        cptDouble++;
    }


    public void setNbTourEntrePrison(int nbTourEntrePrison) {
        this.tourEntrePrison = nbTourEntrePrison;
    }

    @Override
    public String toString() {
        return "Joueur{" +
                "scan=" + scan +
                ", id=" + id +
                ", caseActuelle=" + caseActuelle +
                ", capitalTotal=" + capitalTotal +
                ", nbPropriete=" + nbPropriete +
                ", nbMaison=" + nbMaison +
                ", nbHotel=" + nbHotel +
                ", nbGare=" + nbGare +
                ", nbService=" + nbService +
                ", tourEntrePrison=" + tourEntrePrison +
                ", cptDouble=" + cptDouble +
                ", nom='" + nom + '\'' +
                ", pion='" + pion + '\'' +
                ", listeProprietes=" + Arrays.toString(listeProprietes) +
                ", estEnPrison=" + estEnPrison +
                ", estEliminer=" + estEliminer +
                '}';
    }


// -------------------------------
    // 💰 Gestion Économie
    // -------------------------------

    public int compteCapital(Map<Integer, Integer> capital){
        int total = 0;
        for (Map.Entry<Integer, Integer> clef : capital.entrySet()) {
            total += clef.getValue()*clef.getKey();
        }
        return total;
    }

    public double incrCapital(int total){
        this.capitalTotal += total; // Actualisation Balance
        return  total;
    }

    public int decrCapital(int total){
        try{
            if(this.capitalTotal < total){
                throw new InsufficientFundsException("Fond Insufisant");
            }
            this.capitalTotal -= total;
        } catch (InsufficientFundsException e){
            System.err.println(e.getMessage());
        }
        return this.capitalTotal;
    }

    public void debitReparation(int[] total) throws InsufficientFundsException {
        // 1er indice = prix maison, 2eme indice = prix hotel
        decrCapital(getNbMaison()*total[0]+getNbHotel()*total[1]);
    }

    // -------------------------------
    // 🏛️ Gestion Patrimoine
    // -------------------------------

    public void ajouterPropriete(Propriete propriete) {
        listeProprietes[nbPropriete++] = propriete;
    }

    public void supprimerPropriete(Propriete propriete) {
        listeProprietes[nbPropriete--] = null;
    }

    public boolean possedePropriete(Propriete propriete) {
        for (Propriete p : listeProprietes){
            if (p == propriete) return true;
        }
        return false;
    }

    public void supprimerPropriete(int id){
        for (Propriete p : listeProprietes){
            if (p.getId() == id){
                listeProprietes[nbPropriete--] = null;
                return;
            }
        }
    }

    public void rembourserHypotheque(Propriete propriete) throws InsufficientFundsException {
        int valeur = (int)Math.round((propriete.getPrixAchat()/2)*1.1);
        if (this.capitalTotal >= valeur) {
            this.decrCapital(valeur);
            propriete.setEstHypothequee(false);
        }
        else System.out.println("Capital insuffisant");
    }

    public void AmeliorationPropriete(Propriete propriete) {
        if (propriete.getNb_hotel() ==0){
            if(propriete.getNb_maisons() == 4){
                this.decrCapital(propriete.getPrix_hotel());
                propriete.setNb_maisons(0);
                propriete.setNb_hotel(1);
            }
            else{
                this.decrCapital(propriete.getPrix_maison());
                propriete.setNb_maisons(propriete.getNb_maisons()+1);
            }
        }
    }



    public boolean verificationHypothequeGroupe(Propriete propriete) {
        //Verifie si 1 bien au moins est hypothéqué dans le groupe ( impossiblité de construire )
        for(Propriete p : listeProprietes){
            if (p.getQuartier() == propriete.getQuartier() && p.getEstHypothequee()){
                return true;
            }
        }
        return true;
    }



    // others

    public void allerEnPrison(int tour){
        this.setEstEnPrison(true);
        this.tourEntrePrison = tour;
    }

    public void carteSortiPrison(){
        // attribue une carte de sortie de prison au joueur
        if(this.estEnPrison){
            setEstEnPrison(false);
            System.out.println("Vous etes libéré de prison :");
        }
        else System.out.println("Vous ne pouvez utiliser cette carte qu'en prison !");
    }

    public void avancer(int nbCase){
        this.caseActuelle = (this.caseActuelle + nbCase) % 40;
        System.out.println(caseActuelle + ""+(caseActuelle + nbCase)%40);
    }
}

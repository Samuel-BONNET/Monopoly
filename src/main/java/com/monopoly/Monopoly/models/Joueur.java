package com.monopoly.Monopoly.models;


import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monopoly.Monopoly.models.plateau.IPossession;
import com.monopoly.Monopoly.models.plateau.Propriete;

public class Joueur {
    public Scanner scan;
    private static int compteur_id = 0;
    private int id, caseActuelle = 0, capitalTotal = 1500, nbPropriete = 0, nbMaison = 0, nbHotel = 0, nbGare =0, nbService = 0, tourEntrePrison, cptDouble = 0, lancerDesRestant = 1 ;
    private String nom, pion;
    @JsonProperty
    private IPossession[] listePossession = new IPossession[28];// nb max de propriétés
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

    public IPossession[] getListePossession() {
        return listePossession;
    }

    public IPossession getPropriete(int id){
        return listePossession[id];
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

    public void setCptDouble(int nb){
        this.cptDouble = nb;
    }

    public void incrCptDouble(){
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
                ", listePossession=" + Arrays.toString(listePossession) +
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

    public void ajouterPropriete(IPossession possession) {
        listePossession[nbPropriete++] = possession;
    }

    public void supprimerPropriete(IPossession possession) {
        for(int i = 0; i<listePossession.length; i++){
            if(listePossession[i].equals(possession)){
                listePossession[i] = null;
            }
        }
    }

    public boolean possedePossession(IPossession possession) {
        for (IPossession p : listePossession){
            if (p == possession) return true;
        }
        return false;
    }

    public void supprimerPossession(int id){
        for (IPossession p : listePossession){
            if (p.getId() == id){
                listePossession[nbPropriete--] = null;
                return;
            }
        }
    }

    public void rembourserHypotheque(IPossession possession) throws InsufficientFundsException {
        int valeur = (int)Math.round((possession.getPrixAchat()/2)*1.1);
        if (this.capitalTotal >= valeur) {
            this.decrCapital(valeur);
            possession.setEstHypothequee(false);
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
        for(IPossession p : listePossession){
            if(p instanceof Propriete) {
                if (Objects.equals(((Propriete) p).getQuartier(), propriete.getQuartier()) && ((Propriete) p).getEstHypothequee()) {
                    return true;
                }
            }
        }
        return false;
    }



    // others

    public void allerEnPrison(){
        this.setEstEnPrison(true);
        this.tourEntrePrison = 0;
    }

    public void incrTourPrison(){
        this.tourEntrePrison++;
    }

    public void carteSortiPrison(){
        // attribue une carte de sortie de prison au joueur
        if(this.estEnPrison){
            setEstEnPrison(false);
            System.out.println("Vous etes libéré de prison :");
        }
        else System.out.println("Vous ne pouvez utiliser cette carte qu'en prison !");
    }

    public String avancer(int nbCase){
        int caseArrive = (this.caseActuelle + nbCase) % 40;
        String sortie = " ";
        if(caseArrive < this.caseActuelle){
            // Le joueur est / est passé sur la case départ
            this.incrCapital(200);
            sortie = "Vous passez par le case départ et recevez 200$ !";
        }
        this.caseActuelle = caseArrive;
        return sortie;
    }

    public void avancerJusqua(int idCase){
        this.caseActuelle = idCase;
    }
}

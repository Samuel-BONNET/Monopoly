package com.monopoly.Monopoly.models;


import java.sql.SQLOutput;
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
    private int id, caseActuelle = 0, capitalTotal = 1500, nbPropriete = 0, nbMaison = 0, nbHotel = 0, nbGare =0, nbService = 0, tourEntrePrison, cptDouble = 0;;
    private String nom, pion;
    private Propriete[] listeProprietes = new Propriete[25];// nb max de propriétés
    private boolean estEnPrison = false, estEliminer = false;
    private Map<Integer, Integer> capital;
    private Map<Integer, Integer> start = new HashMap<>() {{
        put(500, 2);
        put(100, 4);
        put(50, 1);
        put(20, 1);
        put(10, 2);
        put(5, 1 );
        put(1, 5);
    }}; // Banque du Joueur

    public Joueur(int id, String nom, String pion) {
        this.id = id;
        this.nom = nom;
        this.pion = pion;
        this.capital = start;
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

    public int getCaseActuelle() {
        return caseActuelle;
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

    public Map<Integer, Integer> getCapital() {
        return capital;
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
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", pion='" + pion + '\'' +
                ", capital=" + capital +
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

    public void incrCapital(Map<Integer, Integer> total){
        for(Map.Entry<Integer, Integer> clef : total.entrySet()){
            int valeur = clef.getKey();
            int nb = clef.getValue();
            capital.put(valeur, capital.getOrDefault(valeur,0) + nb);
        }
        capitalTotal = compteCapital(capital); // Actualisation Balance
    }

    public void incrCapital(int total){
        //Todo
    }

    public void faireMonnaie(int totalARendre){
        Argent[] listeBillets = {Argent.CINQ_CENTS, Argent.DEUX_CENTS, Argent.CENT, Argent.CINQUANTE, Argent.VINGT, Argent.DIX, Argent.CINQ, Argent.UN};
        int total = 0;
        while(total != totalARendre) {
            System.out.println("Combien de billet(s) de 500 :");
            int nb500 = scan.nextInt();
            total += nb500*500;
            System.out.println("Combien de billet(s) de 100 :");
            int nb100 = scan.nextInt();
            total += nb100*100;
            System.out.println("Combien de billet(s) de 50 :");
            int nb50 = scan.nextInt();
            total += nb50*50;
            System.out.println("Combien de billet(s) de 20 :");
            int nb20 = scan.nextInt();
            total += nb20*20;
            System.out.println("Combien de billet(s) de 10 :");
            int nb10 = scan.nextInt();
            total += nb10*10;
            System.out.println("Combien de billet(s) de 5 :");
            int nb5 = scan.nextInt();
            total += nb5*5;
            System.out.println("Combien de billet(s) de 1 :");
            int nb1 = scan.nextInt();
            total += nb1*1;
            switch (Integer.compare(total,totalARendre)){
                case -1:
                    System.out.println("Somme choisie Inférieure à ce qui est dû");
                    total = 0;
                    break;
                case 0:
                    System.out.println("Vous souhaitez recevoir "+nb500+" billet(s) de 500, " + nb100 +" billet(s) de 100, " + nb50 +" billet(s) de 50 ," + nb20 +" billet(s) de 20, "+ nb10 + " billet(s) de 10, " + nb5 + " billet(s) de 5 et "+ nb1 + " billet(s) de 1.");
                    capital.put(500, capital.getOrDefault(500,0) + nb500);
                    capital.put(100, capital.getOrDefault(100,0) + nb100);
                    capital.put(50, capital.getOrDefault(50,0) + nb50);
                    capital.put(20, capital.getOrDefault(20,0) + nb20);
                    capital.put(10, capital.getOrDefault(10,0) + nb10);
                    capital.put(5, capital.getOrDefault(5,0) + nb5);
                    capital.put(1, capital.getOrDefault(1,0) + nb1);
                    System.out.println("Votre solde à été modifié avec succès");
                    break;
                case 1:
                    System.out.println("Somme choisie Supérieure à ce qui est dû");
                    total = 0;
                    break;
            }
        }
    }

    public Map<Integer,Integer> decrCapital(int total) throws InsufficientFundsException  {
        Map<Integer, Integer> somme = Map.of();
        try {
            if (total > capitalTotal) {
                throw new Exception("Fonds insuffisants");
            }
            else{
                boolean conditionArret = false;
                do {
                    System.out.println("Choissez comment régler : "+total);
                    somme = payerMontant(total);
                }
                while(!conditionArret);
            }
        }catch(InsufficientFundsException e) {
            System.out.println(e.getMessage());
            System.out.println(nom + " est éliminé !");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return somme;
    }

    public Map<Integer, Integer> payerMontant(int totalAregler){
        Scanner scan = new Scanner(System.in);
        String reponse;
        do{
            System.out.println("Souhaitez vous faire de la monnaie ?");
            reponse = scan.nextLine();
            reponse.toLowerCase();
        } while(reponse.equals("oui") || reponse.equals("yes") ||reponse.equals("ouais") || reponse.equals("non") || reponse.equals("no") ||reponse.equals("nan"));
        switch (reponse) {
            // eventuellement fix
            case "oui", "ouais", "yes":
                faireMonnaie(capitalTotal);
                break;
        }
        System.out.println("Procéder au reglement de la somme de : "+totalAregler);
        Map<Integer,Integer> capitalReduit = new HashMap<>();
        boolean conditionArret = false;
        int nb500, nb100, nb50, nb20, nb10, nb5,nb1;
        do{
            System.out.println("Combien de billet(s) de 500 ?");
            nb500 = scan.nextInt();
            conditionArret = nb500 <= capital.getOrDefault(500,0);
        }while(!conditionArret);
        conditionArret = false;
        capitalReduit.put(500, nb500);
        do {
            System.out.println("Combien de billet(s) de 100 ?");
            nb100 = scan.nextInt();
            conditionArret = nb100 <= capital.getOrDefault(100, 0);
        }while(!conditionArret);
        conditionArret = false;
        capitalReduit.put(100, nb100);
        do{
            System.out.println("Combien de billet(s) de 50 ?");
            nb50 = scan.nextInt();
            conditionArret = nb50 <= capital.getOrDefault(50, 0);
        } while(!conditionArret);
        conditionArret = false;
        capitalReduit.put(50, nb50);
        do{
            System.out.println("Combien de billet(s) de 20 ?");
            nb20 = scan.nextInt();
            conditionArret = nb20 <= capital.getOrDefault(20, 0);
        }while(!conditionArret);
        conditionArret = false;
        capitalReduit.put(20, nb20);
        do{
            System.out.println("Combien de billet(s) de 10 ?");
            nb10 = scan.nextInt();
            conditionArret = nb10 <= capital.getOrDefault(10, 0);
        }while(!conditionArret);
        conditionArret = false;
        capitalReduit.put(10, nb10);
        do{
            System.out.println("Combien de billet(s) de 5 ?");
            nb5 = scan.nextInt();
            conditionArret = nb5 <= capital.getOrDefault(5, 0);
        }while(!conditionArret);
        conditionArret = false;
        capitalReduit.put(5, nb5);
        do{
            System.out.println("Combien de billet(s) de 1 ?");
            nb1 = scan.nextInt();
            conditionArret = nb1 <= capital.getOrDefault(1, 0);
        }while(!conditionArret);
        capitalReduit.put(1, nb1);
        scan.close();
        return capitalReduit;
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

    public void choixAchatPropriete(Propriete propriete){
        if (capitalTotal >= propriete.getPrixAchat()){
            System.out.println("Souhaitez vous acheter la propriété "+propriete.getNom()+" pour "+propriete.getPrixAchat()+"$ ? (oui/non)");
            String reponse;
            do{
                reponse = scan.nextLine();
                reponse.toLowerCase();
            } while(!reponse.equals("oui") || !reponse.equals("yes") || !reponse.equals("ouais") || !reponse.equals("non") || !reponse.equals("no") || !reponse.equals("nan"));
            switch (reponse) {
                case "oui", "yes", "ouais":
                    try {
                        this.decrCapital(propriete.getPrixAchat());
                        this.ajouterPropriete(propriete);
                        System.out.println("Félicitations, vous êtes désormais propriétaire de "+propriete.getNom()+" !");
                    } catch (InsufficientFundsException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "non", "no", "nan":
                    System.out.println("Vous avez choisi de ne pas acheter la propriété.");
                    break;
                default:
                    System.out.println("Réponse non reconnue. Veuillez répondre par 'oui' ou 'non'.");
                    break;
            }
        }
        else {
            System.out.println("Capital insuffisant pour acheter cette propriété.");
        }
    }

    public void choixAmelioration(Propriete propriete) {
        System.out.println("Souhaitez vous améliorer la propriété " + propriete.getNom() + " ? (oui/non)");
        String reponse = scan.nextLine();
        reponse.toLowerCase();
        if(reponse.equals("oui") || reponse.equals("yes") || reponse.equals("ouais")){
            if(propriete.getEstGroupeComplet() && verificationHypothequeGroupe(propriete)){
                // possibilité de construire / améliorer
                //todo
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
    }
}

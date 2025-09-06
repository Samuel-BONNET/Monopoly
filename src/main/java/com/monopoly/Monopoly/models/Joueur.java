package com.monopoly.Monopoly.models;


import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.monopoly.Monopoly.models.enums.Argent;
import com.monopoly.Monopoly.models.plateau.Propriete;

public class Joueur {
    public Scanner scan = new Scanner(System.in);
    private static int compteur_id = 0;
    private int id, capitalTotal = 1500, nbPropriete = 0;
    private String nom, piece;
    private Propriete[] listeProprietes = new Propriete[25];// nb max de propriétés
    private boolean estEnPrison = false;
    private Map<Integer, Integer> capital;
    private Map<Integer, Integer> start = new HashMap<>() {{
        put(500, 2);
        put(100, 2);
        put(50, 2);
        put(20, 6);
        put(10, 5);
        put(5, 5);
        put(1, 5);
    }};


    public Joueur(int id, String nom, String piece) {
        this.id = id;
        this.nom = nom;
        this.piece = piece;
        this.capital = start;
    }

    public Joueur(String nom, String piece) {
        this(compteur_id++, nom, piece);
    }

    public static int getCompteur_id() {
        return compteur_id;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPiece() {
        return piece;
    }

    public boolean getEstEnPrison() {
        return estEnPrison;
    }

    public void setEstEnPrison(boolean estEnPrison) {
        this.estEnPrison = estEnPrison;
    }

    public Map<Integer, Integer> getCapital() {
        return capital;
    }

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

    public Map<Integer, Integer> faireMonnaie(int totalARendre){
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
        return capital;
    }

    public void decrCapital(Map<Integer, Integer> total) throws InsufficientFundsException  {
        try {
            if (compteCapital(total) > capitalTotal) {
                throw new Exception("Fonds insuffisants");
            }
            else{

            }
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    public Propriete[] getListe_proprietes() {
        return listeProprietes;
    }

    public Propriete getPropriete(int id){
        return listeProprietes[id];
    }

    public void ajouterPropriete(Propriete propriete) {
        listeProprietes[nbPropriete++] = propriete;
    }

    public void supprimerPropriete(Propriete propriete) {
        listeProprietes[nbPropriete--] = null;
    }

    public void supprimerPropriete(int id){
        for (Propriete p : listeProprietes){
            if (p.getId() == id){
                listeProprietes[nbPropriete--] = null;
                return;
            }
        }
    }

    @Override
    public String toString() {
        return "Joueur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", piece='" + piece + '\'' +
                ", capital=" + capital +
                '}';
    }
}

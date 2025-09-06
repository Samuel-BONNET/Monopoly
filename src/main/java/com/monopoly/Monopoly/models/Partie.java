package com.monopoly.Monopoly.models;

import com.monopoly.Monopoly.models.plateau.CaseEvenement;
import com.monopoly.Monopoly.models.plateau.ICase;
import com.monopoly.Monopoly.models.plateau.Plateau;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Partie {

    private int nbJoueur, tourGolbal=1, tourJoueur=1;
    private Joueur[] listeJoueurs;
    private Scanner sc = new Scanner(System.in);
    private Random rand = new Random();
    private Plateau plateau;
    private static final Map<Integer,Integer> SommeDepart = Map.of(100,2);

    Partie(int nbJoueur) {
        this.nbJoueur = nbJoueur;
    }

    public int getNbJoueur() {
        return nbJoueur;
    }

    // Lancement App
    public void affichageStart(){
        System.out.println("Monopoly");
        System.out.println("Bienvenue dans le jeux Monopoly");
        while (nbJoueur <= 0 || nbJoueur > 4){
            System.out.println("À combien de joueurs souhaitez vous jouer ? ");
            nbJoueur = sc.nextInt();
        }
    }

    public void setUpJoueur(){
        for (int i = 0; i<nbJoueur; i++){
            System.out.println("Quel nom souhaitez vous pour le Joueur "+i+" ?");
            String nom = sc.nextLine();
            System.out.println("Choisissez un pion parmis : 1.♙  2.♕  3.♔  4.♘");
            String pion = sc.nextLine();
            listeJoueurs[nbJoueur++] = new Joueur(nom, pion);
        }
    }

    public void event(CaseEvenement caseEvenement) throws InsufficientFundsException {
        switch (caseEvenement.getId()){
            case 0:
                // depart
                caseDepart();
                break;
            case 1,5,9:
                // caisse commu
                tirageCaisseCommunaute();
                break;
            case 2:
                // impots revenu
                payerImpot(200);
                break;
            case 3,7,10:
                // chance
                tirageChance();
                break;
            case 4,6:
                // visite prison & parc
                break;
            case 8:
                //prison
                allerPrison();
                break;
            case 11:
                // impot de luxe
                payerImpot(100);
                break;
        }
    }

    public int getTourJoueur(){
        return tourJoueur;
    }

    public int getTourGolbal(){
        return tourGolbal;
    }

    public void incrTourJoueur() {
        tourJoueur = (tourJoueur + 1) % nbJoueur; // retour a zero une fois que tous les joueurs ont joué
    }

    public void incrTourGolbal() {
        tourGolbal = tourGolbal++;
    }

    // Reçoit 200 $
    public void caseDepart(){
        System.out.println("Vous passez par la case depart ! \n Recevez 200 $ !");
        listeJoueurs[tourJoueur].incrCapital(SommeDepart);
    }

    public void tirageCaisseCommunaute(){
        //Todo
    }

    public void tirageChance(){
        //Todo
    }

    public void allerPrison(){
        System.out.println("Vous allez directement en Prison ! \n Si vous passez par la case départ, vous ne recevez pas 200 $ !");
        listeJoueurs[tourJoueur].setEstEnPrison(true);
    }

    public void payerImpot(int total) throws InsufficientFundsException {
        listeJoueurs[tourJoueur].decrCapital(total);
    }

    public int lancerDesSimple(){
        return  rand.nextInt(6) + 1;
    }

    public int[] lancerDesDouble(){
        int des1 = lancerDesSimple();
        int des2 = lancerDesSimple();
        return new int[]{des1, des2};
    }

    public void lancerDes(int paire){
        if(paire >= 3){
            listeJoueurs[tourJoueur].setEstEnPrison(true);
        }
        int[] resultat = lancerDesDouble();
        listeJoueurs[tourJoueur].avancer(resultat[0] + resultat[1]);
        if (resultat[0] == resultat[1]){
            lancerDes(paire+1);
        }
    }


    public void play(){
        //plateau = new Plateau();
        affichageStart();
        setUpJoueur();
    }
}

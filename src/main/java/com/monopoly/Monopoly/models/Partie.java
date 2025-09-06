package com.monopoly.Monopoly.models;

import java.util.Scanner;

public class Partie {

    private int nbJoueur, tour=0;
    private Joueur[] listeJoueurs;
    private Scanner sc = new Scanner(System.in);
    //private Plateau plateau;

    Partie(int nbJoueur) {
        this.nbJoueur = nbJoueur;
    }

    public int getNbJoueur() {
        return nbJoueur;
    }

    public void affichageStar(){
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

    public void play(){
        //plateau = new Plateau();
        affichageStar();
        setUpJoueur();
    }
}

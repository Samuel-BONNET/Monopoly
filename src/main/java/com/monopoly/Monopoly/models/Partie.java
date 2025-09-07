package com.monopoly.Monopoly.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopoly.Monopoly.models.plateau.*;

import java.io.File;
import java.util.*;

public class Partie {

    private int nbJoueur, tourGolbal=1, tourJoueur=1, carteChanceTirees = 0, carteCommunauteTirees = 0;
    private boolean victoire = false, carteChanceSortiPrisonEnJeu = true, carteCommunauteSortiPrisonEnJeu = true;
    private Joueur[] listeJoueurs;
    private Scanner sc = new Scanner(System.in);
    private Random rand = new Random();
    private Plateau plateau;
    private List<Carte> listeChance;
    private List<Carte> listeCaisseCommunaute;
    private static final Map<Integer,Integer> SommeDepart = Map.of(100,2);

    Partie(int nbJoueur) {
        this.nbJoueur = nbJoueur;
    }

    // -------------------------------
    // 🔧 Getters / Setters
    // -------------------------------

    public int getNbJoueur() {
        return nbJoueur;
    }

    public int getTourJoueur(){
        return tourJoueur;
    }

    public int getTourGolbal(){
        return tourGolbal;
    }

    public List<Carte> getChance(){
        return listeChance;
    }

    public List<Carte> getCaisseCommunaute(){
        return listeCaisseCommunaute;
    }

    // -------------------------------
    // ⚙️ Mise en place Début de Partie
    // -------------------------------

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

    public void chargerCarte(){
        try {
            ObjectMapper mapper = new ObjectMapper();
            CarteMonopoly cartes = mapper.readValue(
                    new File("cartes.json"),
                    CarteMonopoly.class
            );
            listeCaisseCommunaute = cartes.getCCommunaute();
            listeChance = cartes.getChance();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void tourSuivant(){

    }

    // -------------------------------
    // 🎯 Gestion Évenements
    // -------------------------------

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

    public void caseDepart(){
        // Reçoit 200 $
        System.out.println("Vous passez par la case depart ! \n Recevez 200 $ !");
        listeJoueurs[tourJoueur].incrCapital(SommeDepart);
    }

    public void tirageCaisseCommunaute() throws InsufficientFundsException {
        if (carteCommunauteTirees % 16 == 0){
            melangeCaisseCommunaute();
            carteCommunauteTirees = 0;
        }
        else{
            switch (listeCaisseCommunaute.get(carteCommunauteTirees).getAFaire()){
                case "Credit":
                    listeJoueurs[tourJoueur].incrCapital((int)listeCaisseCommunaute.get(carteCommunauteTirees).getValeur());
                    break;
                case "Credit-Joueur":
                    creditJoueur((int)listeCaisseCommunaute.get(carteCommunauteTirees).getValeur());
                    break;
                case "Debit":
                    listeJoueurs[tourJoueur].decrCapital((int)listeCaisseCommunaute.get(carteCommunauteTirees).getValeur());
                    break;
                case "Prison":
                    allerPrison();
                    break;
                case "Sortie-Prison":
                    if (carteCommunauteSortiPrisonEnJeu){ // si carte sortie de prison encore dans la pioche
                        listeJoueurs[tourJoueur].carteSortiPrison();
                        carteCommunauteSortiPrisonEnJeu = false;
                    }
                    else { // sinon on prend une autre carte
                        carteCommunauteTirees++;
                        tirageCaisseCommunaute();
                    }
                    break;
                default:
                    System.out.println(" Autre évenement !");
            }
            carteCommunauteTirees++;
        }
    }

    public void melangeCaisseCommunaute(){
        Collections.shuffle(listeCaisseCommunaute);
    }

    public void tirageChance() throws InsufficientFundsException {
        if (carteChanceTirees % 16 == 0){
            melangeChance();
            carteChanceTirees = 0;
        }
        else{
            switch (listeChance.get(carteChanceTirees).getAFaire()){
                case "Reculer":
                    listeJoueurs[tourJoueur].reculer((int)listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Avancer":
                    listeJoueurs[tourJoueur].avancer((String)listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Avancer-Gare":
                    listeJoueurs[tourJoueur].avancer("Gare");
                    break;
                case "Avancer-Compagnie":
                    listeJoueurs[tourJoueur].avancer("Compagnie");
                    break;
                case "Crédit":
                    listeJoueurs[tourJoueur].incrCapital((int)listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Débit":
                    listeJoueurs[tourJoueur].decrCapital((int)listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Débit-Joueur":
                    // todo here
                    debitJoueur((int)listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Débit-Réparation":
                    listeJoueurs[tourJoueur].debitReparation((int[])listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Prison":
                    allerPrison();
                    break;
                case "Sortie-Prison":
                    if(carteChanceSortiPrisonEnJeu){ // si la carte de sortie de prison est en jeu
                        listeJoueurs[tourJoueur].carteSortiPrison();
                        carteChanceSortiPrisonEnJeu = false;
                    }
                    else{
                        carteChanceTirees++;
                        tirageChance(); //sinon on tire une autre carte
                    }
                    break;
                default:
                    System.out.println(" Autre évenement !");
                    break;
            }
            carteChanceTirees++;
        }
    }

    public void melangeChance(){
        Collections.shuffle(listeChance);
    }

    public void allerPrison(){
        System.out.println("Vous allez directement en Prison ! \n Si vous passez par la case départ, vous ne recevez pas 200 $ !");
        listeJoueurs[tourJoueur].setEstEnPrison(true);
    }

    public void payerImpot(int total) throws InsufficientFundsException {
        listeJoueurs[tourJoueur].decrCapital(total);
    }

    public void incrTourJoueur() {
        tourJoueur = (tourJoueur + 1) % nbJoueur; // retour a zero une fois que tous les joueurs ont joué
    }

    public void incrTourGolbal() {
        tourGolbal = tourGolbal++;
    }

    // -------------------------------
    // 🎲 Gestion de l'Aléatoire
    // -------------------------------

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
        if (resultat[0] == resultat[1] && listeJoueurs[tourJoueur].getEstEnPrison()){
            listeJoueurs[tourJoueur].setEstEnPrison(false);
            paire--; // Decremente le nombre de double fais avec le dés
        }
        if (resultat[0] == resultat[1]){
            lancerDes(paire+1);
        }
    }


    // others

    public void debitJoueur(int total){
        // todo
    }

    public void creditJoueur(int total){
        // todo
    }

    public void actionEnPrison() throws InsufficientFundsException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Souhaitez vous payer 50$ pour sortir ?");
        String action = scan.nextLine();
        action.toLowerCase();
        if (action.equals("oui") || action.equals("ouais") || action.equals("yes")){
            if (listeJoueurs[tourJoueur].getCapitalTotal() > 50) {
                listeJoueurs[tourJoueur].decrCapital(50);
            }
        }
        else{
            lancerDesDouble();
        }
    }

    // -------------------------------
    // 🕹️ Gestion de la Partie
    // -------------------------------

    public Joueur verifierVictoire() {
        List<Joueur> listeTempJoueurs = Arrays.asList(listeJoueurs);
        long nbActifs = listeTempJoueurs.stream().filter(j -> !j.getEstEliminer()).count();
        if (nbActifs == 1) {
            return listeTempJoueurs.stream().filter(j -> !j.getEstEliminer()).findFirst().orElse(null);
        }
        return null;
    }

    public void play(){
        plateau = new Plateau();
        chargerCarte();
        affichageStart();
        setUpJoueur();
        while(verifierVictoire() == null){
            tourSuivant();
        }
        victoire(verifierVictoire());
    }

    public void victoire(Joueur joueur){
        System.out.println(" Bravo ! \n" + joueur.getNom() + " a gagné !");
    }
}

package com.monopoly.Monopoly.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopoly.Monopoly.models.plateau.*;

import java.io.File;
import java.util.*;

public class Partie {

    private int nbJoueur, tourGolbal=1, tourJoueur=1, carteChanceTirees = 0, carteCommunauteTirees = 0;
    private boolean victoire = false;
    private Joueur carteChanceSortiPrisonEnJeu = null;
    private Joueur carteCommunauteSortiPrisonEnJeu  = null;
    private Joueur[] listeJoueurs;
    private Scanner sc = new Scanner(System.in);
    private Random rand = new Random();
    private Plateau plateau;
    private List<Carte> listeChance;
    private List<Carte> listeCaisseCommunaute;
    private Map<IPossession, Joueur> listePossessionJoueur;
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

    public void tourSuivant() throws InsufficientFundsException {
        lancerDes(0);
        incrTourJoueur();
        switch (listeJoueurs[tourJoueur].getCaseActuelle()){
            case 1:// valeur propriete
                casePropriete((Propriete)plateau.getTotalCase().get(listeJoueurs[tourJoueur].getCaseActuelle()));
                break;
            case 2: // valeur evenement
                caseEvent((CaseEvenement)plateau.getTotalCase().get(listeJoueurs[tourJoueur].getCaseActuelle()));
                break;
            default:
                System.out.println("Autre case" );
                break;
        }
    }

    // -------------------------------
    // 🎯 Gestion Évenements
    // -------------------------------

    public void caseEvent(CaseEvenement caseEvenement) throws InsufficientFundsException {
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

    public void casePropriete(Propriete propriete) throws InsufficientFundsException {
        Joueur joueurActuel = listeJoueurs[tourJoueur];
        if(joueurActuel.possedePropriete(propriete)){
            // cas ou le joueur possde la propriete
            joueurActuel.choixAmelioration(propriete);
        }
        else{
            if (propriete.getProprietaire() != null){
                // cas ou le joueur ne possede pas la propriete & la propriete à deja été achetée
                reglerMontantLoyer(joueurActuel,propriete.getProprietaire(), propriete);
            }
            else{
                // cas ou le joueur ne possede pas la propriete & la propriete n'a pas été achetée
                joueurActuel.choixAchatPropriete(propriete);
            }
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
                    creditJoueur((int)listeCaisseCommunaute.get(carteCommunauteTirees).getValeur(), listeJoueurs[tourJoueur]);
                    break;
                case "Debit":
                    listeJoueurs[tourJoueur].decrCapital((int)listeCaisseCommunaute.get(carteCommunauteTirees).getValeur());
                    break;
                case "Prison":
                    allerPrison();
                    break;
                case "Sortie-Prison":
                    if (carteCommunauteSortiPrisonEnJeu != null){ // si carte sortie de prison encore dans la pioche
                        listeJoueurs[tourJoueur].carteSortiPrison();
                        carteCommunauteSortiPrisonEnJeu = listeJoueurs[tourJoueur];
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
                    listeJoueurs[tourJoueur].avancer(-1 * (int)listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Avancer":
                    avancer((String)listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Avancer-Gare":
                    avancer("Gare");
                    break;
                case "Avancer-Compagnie":
                    avancer("Compagnie");
                    break;
                case "Crédit":
                    listeJoueurs[tourJoueur].incrCapital((int)listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Débit":
                    listeJoueurs[tourJoueur].decrCapital((int)listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Débit-Joueur":
                    debitJoueur((int)listeChance.get(carteChanceTirees).getValeur(), listeJoueurs[tourJoueur]);
                    break;
                case "Débit-Réparation":
                    listeJoueurs[tourJoueur].debitReparation((int[])listeChance.get(carteChanceTirees).getValeur());
                    break;
                case "Prison":
                    allerPrison();
                    break;
                case "Sortie-Prison":
                    if(carteChanceSortiPrisonEnJeu != null){ // si la carte de sortie de prison est en jeu
                        listeJoueurs[tourJoueur].carteSortiPrison();
                        carteChanceSortiPrisonEnJeu = listeJoueurs[tourJoueur];
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



    public void reglerMontantLoyer(Joueur payeur, Joueur receveur, IPossession bien) throws InsufficientFundsException {
        switch(bien.getClass().getSimpleName()){
            case "Propriete":
                Map<Integer, Integer> sommeVersePropriete = payeur.payerMontant(bien.calculerLoyer());
                receveur.incrCapital(sommeVersePropriete);
                break;
            case "Gare":
                // loyer gare = 25 -50 -100 -200 = 25 * 2^(nb_gare-1)
                Map<Integer, Integer> sommeVerseGare = payeur.payerMontant((int)(Math.pow(2,receveur.getNbGare()-1))*bien.calculerLoyer());
                receveur.incrCapital(sommeVerseGare);
                break;
            case "ServicePublic":
                Map<Integer, Integer> sommeVerseService = payeur.payerMontant(eventService(receveur.getNbService()));
                break;
            default:
                System.out.println("Erreur dans le type de bien");
                break;
        }
    }


    public int eventService(int nbService){
        int[] lance = lancerDesDouble();
        int total = lance[0] + lance[1];
        if (nbService == 1){
            return total*4;
        }
        else return total*10;

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
            return;
        }
        int[] resultat = lancerDesDouble();
        if (resultat[0] == resultat[1] && listeJoueurs[tourJoueur].getEstEnPrison()){
            listeJoueurs[tourJoueur].setEstEnPrison(false);
            lancerDes(0);
        }
        else{
            int casePrecedente = listeJoueurs[tourJoueur].getCaseActuelle();
            listeJoueurs[tourJoueur].avancer(resultat[0] + resultat[1]);
            // Cas ou l'on passe par la case départ
            if (listeJoueurs[tourJoueur].getCaseActuelle() < casePrecedente){
                caseDepart();
            }
        }
    }


    // others

    public void debitJoueur(int total, Joueur donneur) throws InsufficientFundsException {
        Map<Integer, Integer> somme;
        for(Joueur joueur : listeJoueurs){
            if(joueur != donneur){
                somme = donneur.decrCapital(total);
                joueur.incrCapital(somme);
            }
        }
    }

    public void creditJoueur(int total, Joueur receveur) throws InsufficientFundsException {
        for(Joueur joueur : listeJoueurs){
            if(joueur != receveur){
                receveur.incrCapital(joueur.decrCapital(total));
            }
        }
    }

    public void hypothequer(Propriete propriete){
        propriete.setEstHypothequee(true);
        listeJoueurs[tourJoueur].incrCapital(propriete.getPrixAchat()/2);
    }

    public void rembourserHypotheque(Propriete propriete) throws InsufficientFundsException {
        propriete.setEstHypothequee(false);
        listeJoueurs[tourJoueur].decrCapital((int)(((double) propriete.getPrixAchat() /2)*1.1));
    }

    public void actionEnPrison() throws InsufficientFundsException {
        Scanner scan = new Scanner(System.in);
        String saisi;
        if (carteChanceSortiPrisonEnJeu == listeJoueurs[tourJoueur] || carteCommunauteSortiPrisonEnJeu == listeJoueurs[tourJoueur]) {
            do {
                System.out.println("Souhaitez vous jouer votre carte pour être libéré de prison ?");
                saisi = scan.nextLine();
                saisi.toLowerCase();
            } while (!(saisi.equals("oui") || saisi.equals("yes") || saisi.equals("ouais") || saisi.equals("non") || saisi.equals("no") || saisi.equals("nan")));
            switch (saisi) {
                case "oui", "ouais", "yes":
                    // todo
                    break;
                case "non", "nan", "no":
                    // todo
                    break;
            }
        }
        else{
            System.out.println("Souhaitez vous payer 50$ pour sortir ?");
            String action = scan.nextLine();
            action.toLowerCase();
            if (action.equals("oui") || action.equals("ouais") || action.equals("yes")) {
                if (listeJoueurs[tourJoueur].getCapitalTotal() > 50) {
                    listeJoueurs[tourJoueur].decrCapital(50);
                }
            } else {
                System.out.println("Dans ce cas, testez votre chance aux dés !");
                lancerDesDouble();
            }

        }
    }

    public void avancer(String destination){
        int caseActuelle = listeJoueurs[tourJoueur].getCaseActuelle();
        List<ICase> cases = plateau.getTotalCase();
        for (int i = caseActuelle; i < cases.size(); i++){
            if (cases.get(i).getNom().equals(destination)){
                listeJoueurs[tourJoueur].avancer(i - caseActuelle);
                return;
            }
        }
    }


    public void acheter(IPossession bienAVendre) throws InsufficientFundsException {
        if(listeJoueurs[tourJoueur].getCaseActuelle() > bienAVendre.getPrixAchat()){
            listeJoueurs[tourJoueur].decrCapital(bienAVendre.getPrixAchat());
            listePossessionJoueur.put(bienAVendre,listeJoueurs[tourJoueur]);
        }
        else{
            System.out.println("Fond insuffisant");
        }
    }

    public void vente(IPossession bienAVendre) throws InsufficientFundsException {
        // todo
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

    public void play() throws InsufficientFundsException {
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

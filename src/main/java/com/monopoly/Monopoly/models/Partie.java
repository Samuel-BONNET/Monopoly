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
    private Map<IPossession, Joueur> listePossessionJoueur = new HashMap<>();
    private static final Map<Integer,Integer> SommeDepart = Map.of(100,2);

    public Partie(int nbJoueur) {
        plateau = new Plateau();
        this.nbJoueur = nbJoueur;
        listeJoueurs = new Joueur[nbJoueur];
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

    public Plateau getPlateau() {
        return plateau;
    }

    public List<Carte> getChance(){
        return listeChance;
    }

    public List<Carte> getCaisseCommunaute(){
        return listeCaisseCommunaute;
    }

    public Joueur[] getListeJoueurs() {
        return this.listeJoueurs;
    }

    public List<Map<String, Object>> getListeJoueursInfo() {
        List<Map<String, Object>> joueursInfo = new ArrayList<>();

        for (Joueur j : listeJoueurs) {
            if (j == null) continue; // sécurité si tableau pas complètement rempli
            Map<String, Object> info = new HashMap<>();
            info.put("nom", j.getNom());
            info.put("pion", j.getPion());
            info.put("capital", j.getCapitalTotal());
            info.put("caseActuelle", j.getCaseActuelle());
            info.put("estEnPrison", j.getEstEnPrison());
            info.put("estEliminer", j.getEstEliminer());
            info.put("nbGare", j.getNbGare());
            info.put("nbService", j.getNbService());
            joueursInfo.add(info);
        }

        return joueursInfo;
    }

    public List<Map<String, Object>> getPlateauInfo() {
        List<Map<String, Object>> plateauInfo = new ArrayList<>();

        for (ICase c : plateau.getTotalCase()) {
            Map<String, Object> info = new HashMap<>();
            info.put("nom", c.getNom());
            info.put("id", c.getId());
            info.put("type", c.getClass().getSimpleName()); // Propriete, CaseEvenement, Gare, ServicePublic, etc.

            // si c'est une propriété ou un bien possédé
            if (c instanceof IPossession poss) {
                Joueur proprietaire = listePossessionJoueur.get(poss);
                info.put("proprietaire", proprietaire != null ? proprietaire.getNom() : null);
                info.put("prixAchat", poss.getPrixAchat());
                info.put("estHypothequee", poss instanceof Propriete p ? p.getEstHypothequee() : null);
            }

            plateauInfo.add(info);
        }

        return plateauInfo;
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
        System.out.println("Au tour de :" + listeJoueurs[tourJoueur].getNom());
        lancerDes(0);
        incrTourJoueur();
        switch (listeJoueurs[tourJoueur].getCaseActuelle()){
            case 1,3,6,8,9,11,13,14,16,18,19,21,23,24,26,27,29,31,32,34,37,39:// valeur propriete
                casePropriete((Propriete)plateau.getTotalCase().get(listeJoueurs[tourJoueur].getCaseActuelle()));
                break;
            case 2,4,5,7,10,12,15,17,20,22,25,28,30,33,35,36,38: // valeur evenement
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

    public int eventService(int nbService){
        int[] lance = lancerDesDouble();
        int total = lance[0] + lance[1];
        if (nbService == 1){
            return total*4;
        }
        else return total*10;

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
                acheter(propriete);
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
        listeJoueurs[tourJoueur].allerEnPrison(tourGolbal);
    }

    public void payerImpot(int total) throws InsufficientFundsException {
        listeJoueurs[tourJoueur].decrCapital(total);
    }

    public void incrTourJoueur() {
        tourJoueur = (tourJoueur + 1) % nbJoueur;
        if (tourJoueur == 0) incrTourGolbal();
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


    // -------------------------------
    // ⚖️ Gestion entre Joueur
    // -------------------------------

    public void creationJoueur(int numeroJoueur,String nomJoueur, String pion){
        listeJoueurs[numeroJoueur-1] = new Joueur(nomJoueur,pion);
    }

    public Joueur getJoueurAJouer(){
        return listeJoueurs[tourJoueur];
    }

    public void debitJoueur(int total, Joueur donneur) throws InsufficientFundsException {
        int nbBeneficiaires = listeJoueurs.length - 1;
        int part = total / nbBeneficiaires; // montant que chaque joueur reçoit

        for (Joueur joueur : listeJoueurs) {
            if (joueur != donneur) {
                Map<Integer, Integer> somme = donneur.decrCapital(part);
                joueur.incrCapital(somme);
            }
        }
    }

    public void creditJoueur(int total, Joueur receveur) throws InsufficientFundsException {
        for (Joueur joueur : listeJoueurs) {
            if (joueur != receveur) {
                Map<Integer, Integer> somme = joueur.decrCapital(total);
                receveur.incrCapital(somme);
            }
        }
    }

    public void acheter(IPossession bienAVendre) throws InsufficientFundsException {
        System.out.println("Souhaitez-vous acheter : " + bienAVendre.getNom() + " pour " + bienAVendre.getPrixAchat() + " ? (oui/non)");

        if (traitementReponse(sc)) {
            Joueur joueur = listeJoueurs[tourJoueur];
            if (joueur.getCapitalTotal() >= bienAVendre.getPrixAchat()) {
                joueur.decrCapital(bienAVendre.getPrixAchat());
                listePossessionJoueur.put(bienAVendre, joueur);
                System.out.println(joueur.getNom() + " a acheté " + bienAVendre.getNom() + " !");
            } else {
                System.out.println("Fonds insuffisants pour acheter " + bienAVendre.getNom());
            }
        }
    }


    public void vente(IPossession bienAVendre, Joueur acheteur, int prix) throws InsufficientFundsException {
        Joueur vendeur = listePossessionJoueur.get(bienAVendre);

        if (vendeur == null) {
            System.out.println("Erreur : le bien n'appartient à aucun joueur.");
            return;
        }
        if (vendeur == acheteur) {
            System.out.println("Erreur : vous possédez déjà ce bien.");
            return;
        }

        // Vérification fonds acheteur
        if (acheteur.getCapitalTotal() < prix) {
            throw new InsufficientFundsException("Fonds insuffisants pour acheter " + bienAVendre.getNom());
        }

        // Transaction
        acheteur.decrCapital(prix);
        vendeur.incrCapital(prix);

        // Transfert de propriété
        listePossessionJoueur.put(bienAVendre, acheteur);

        System.out.println(acheteur.getNom() + " a acheté " + bienAVendre.getNom() +
                " à " + vendeur.getNom() + " pour " + prix + "$.");
    }

    public void hypothequer(Propriete propriete) {
        if (propriete.getEstHypothequee()) {
            System.out.println("Cette propriété est déjà hypothéquée !");
            return;
        }

        propriete.setEstHypothequee(true);
        int total = propriete.getPrixAchat() / 2;
        listeJoueurs[tourJoueur].incrCapital(total);

        System.out.println("Votre propriété " + propriete.getNom() + " a été hypothéquée et vous rapporte "
                + total + "$ !");
    }

    public int getPrixRemboursementHypotheque(Propriete propriete) {
        return (int) (propriete.getPrixAchat() * 0.55);
    }

    public void rembourserHypotheque(Propriete propriete) throws InsufficientFundsException {
        if (!propriete.getEstHypothequee()) {
            System.out.println("Cette propriété n'est pas hypothéquée !");
            return;
        }

        int remboursement = getPrixRemboursementHypotheque(propriete);

        if (listeJoueurs[tourJoueur].getCapitalTotal() < remboursement) {
            throw new InsufficientFundsException("Fonds insuffisants pour rembourser l'hypothèque !");
        }

        listeJoueurs[tourJoueur].decrCapital(remboursement);
        propriete.setEstHypothequee(false);

        System.out.println("Vous avez remboursé l'hypothèque de " + propriete.getNom() +
                " pour " + remboursement + "$. Elle vous appartient de nouveau pleinement !");
    }

    public void actionEnPrison() throws InsufficientFundsException {
        Joueur joueur = listeJoueurs[tourJoueur];

        // Cas : libéré automatiquement après 3 tours
        if (joueur.getNbTourEntrePrison() == tourGolbal + 3) {
            joueur.setEstEnPrison(false);
            System.out.println(joueur.getNom() + " est libéré après 3 tours.");
            return;
        }

        // Cas : joueur possède une carte de sortie
        if (carteChanceSortiPrisonEnJeu == joueur || carteCommunauteSortiPrisonEnJeu == joueur) {
            System.out.println("Souhaitez-vous utiliser votre carte pour être libéré de prison ? (oui/non)");
            if (traitementReponse(sc)) {
                joueur.setEstEnPrison(false);
                // remettre la carte dans la pioche si nécessaire
                if (carteChanceSortiPrisonEnJeu == joueur) carteChanceSortiPrisonEnJeu = null;
                if (carteCommunauteSortiPrisonEnJeu == joueur) carteCommunauteSortiPrisonEnJeu = null;
                System.out.println(joueur.getNom() + " utilise sa carte et sort de prison !");
            } else {
                proposerPaiementOuDes(joueur);
            }
        } else {
            proposerPaiementOuDes(joueur);
        }
    }

    private void proposerPaiementOuDes(Joueur joueur) throws InsufficientFundsException {
        System.out.println("Souhaitez-vous payer 50$ pour sortir ? (oui/non)");
        if (traitementReponse(sc)) {
            if (joueur.getCapitalTotal() >= 50) {
                joueur.decrCapital(50);
                joueur.setEstEnPrison(false);
                System.out.println(joueur.getNom() + " paye 50$ et sort de prison.");
            } else {
                throw new InsufficientFundsException("Fonds insuffisants pour payer la sortie de prison !");
            }
        } else {
            System.out.println("Vous tentez votre chance en lançant les dés...");
            int[] resultat = lancerDesDouble();
            if (resultat[0] == resultat[1]) {
                joueur.setEstEnPrison(false);
                System.out.println(joueur.getNom() + " a fait un double et sort de prison !");
            } else {
                System.out.println(joueur.getNom() + " n'a pas fait de double, il reste en prison.");
            }
        }
    }

    public boolean traitementReponse(Scanner sc) {
        while (true) {
            String saisi = sc.nextLine().trim().toLowerCase();
            if (saisi.equals("oui") || saisi.equals("ouais") || saisi.equals("yes")) {
                return true;
            } else if (saisi.equals("non") || saisi.equals("nan") || saisi.equals("no")) {
                return false;
            } else {
                System.out.println("Erreur de saisie. Veuillez répondre par oui/non.");
            }
        }
    }

    public void avancer(String destination) {
        int caseActuelle = listeJoueurs[tourJoueur].getCaseActuelle();
        List<ICase> cases = plateau.getTotalCase();
        for (int i = caseActuelle; i < cases.size(); i++) {
            if (cases.get(i).getNom().equals("Depart")){
                caseDepart();
            }
            if (cases.get(i).getNom().equals(destination)) {
                listeJoueurs[tourJoueur].avancer(i - caseActuelle);
                return;
            }
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

    public void play() throws InsufficientFundsException {
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

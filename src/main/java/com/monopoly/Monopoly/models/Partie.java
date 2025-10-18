package com.monopoly.Monopoly.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monopoly.Monopoly.models.plateau.*;

import java.io.InputStream;
import java.util.*;

public class Partie {

    private int nbJoueur, tourGolbal = 1, tourJoueur = 0, carteChanceTirees = 0, carteCommunauteTirees = 0;
    private boolean victoire = false;
    private Joueur carteChanceSortiPrisonEnJeu = null;
    private Joueur carteCommunauteSortiPrisonEnJeu  = null;
    private Joueur[] listeJoueurs;
    private Scanner sc = new Scanner(System.in);
    private Random rand = new Random();
    private Plateau plateau;
    private List<Carte> listeChance, deckChance, listeCaisseCommunaute, deckCaisseCommunaute;
    private Map<IPossession, Joueur> listePossessionJoueur = new HashMap<>();
    private static final int SOMME_DEPART = 200;

    public Partie(int nbJoueur) {
        plateau = new Plateau();
        this.nbJoueur = nbJoueur;
        listeJoueurs = new Joueur[nbJoueur];
        chargerCarte();
        remplirDeckChance();
        remplirDeckCaisseCommunaute();
    }

    // -------------------------------
    // 🔧 Getters / Setters
    // -------------------------------

    public List<Carte> getDeckCaisseCommunaute() {
        if (deckCaisseCommunaute == null || deckCaisseCommunaute.isEmpty()) {
            remplirDeckCaisseCommunaute();
        }
        return deckCaisseCommunaute;
    }

    public List<Carte> getDeckChance() {
        if (deckChance == null || deckChance.isEmpty()) {
            remplirDeckChance();
        }
        return deckChance;
    }

    public Carte piocherCarteChance(){
        Joueur joueur = listeJoueurs[tourJoueur];
        if (deckChance == null || deckChance.isEmpty()) {
            remplirDeckChance();
        }
        if (deckChance.isEmpty()) {
            return null; // pas de cartes -> on retourne null (caller doit gérer)
        }

        // Prendre la première carte (top of deck)
        Carte c = deckChance.remove(0);

        if (c != null && "Sortie-Prison".equals(c.getAFaire())) {
            carteChanceSortiPrisonEnJeu = joueur;
        }
        return c;
    }

    public Carte piocherCarteCommunaute(){
        Joueur joueur = listeJoueurs[tourJoueur];
        if (deckCaisseCommunaute == null || deckCaisseCommunaute.isEmpty()) {
            remplirDeckCaisseCommunaute();
        }
        if (deckCaisseCommunaute.isEmpty()) {
            return null;
        }

        Carte c = deckCaisseCommunaute.remove(0);

        if (c != null && "Sortie-Prison".equals(c.getAFaire())) {
            carteCommunauteSortiPrisonEnJeu = joueur;
        }
        return c;
    }

    public Joueur getJoueur(int id){
        return listeJoueurs[id % nbJoueur];
    }

    public Joueur getJoueur(Joueur joueur){
        return listeJoueurs[joueur.getId()];
    }

    public int getPositionJoueur(int id){
        return listeJoueurs[id].getCaseActuelle();
    }

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

    public void chargerCarte() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Charger depuis les resources
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("cartes.json");
            if (inputStream == null) {
                System.err.println("⚠️ Impossible de trouver 'cartes.json' dans le classpath (src/main/resources).");
                listeCaisseCommunaute = new ArrayList<>();
                listeChance = new ArrayList<>();
                return;
            }
            CarteMonopoly cartes = mapper.readValue(inputStream, CarteMonopoly.class);

            listeCaisseCommunaute = cartes.getCcommunaute();
            listeChance = cartes.getChance();
        } catch (Exception e) {
            e.printStackTrace();
            // si erreur lors du parsing, garantir listes non-null
            if (listeCaisseCommunaute == null) listeCaisseCommunaute = new ArrayList<>();
            if (listeChance == null) listeChance = new ArrayList<>();
        }
    }

    // -------------------------------
    // 🎯 Gestion Évenements
    // -------------------------------

    public int eventService(int[] valeurDes){
        int total = valeurDes[0] + valeurDes[1];
        switch (listeJoueurs[tourJoueur].getNbService()){
            case 1:
                return total*4;
            case 2:
                return total*10;
            default:
                return 0;
        }
    }

    public String caseEvenement(int id) throws InsufficientFundsException {
        ICase caseActu = plateau.getCase(id);
        Joueur joueurCourrant = listeJoueurs[tourJoueur];
        switch(caseActu.getNom()){
            case "Impôt sur le revenu":
                return payerImpot(200);
            case "Taxe Luxe":
                return payerImpot(100);
            case "Prison":
                return allerPrison(joueurCourrant);
            case "Parc","Visite-Prison":
                if(listeJoueurs[tourJoueur].getEstEnPrison()){
                    return "Payez 50$ ou tentez votre chance aux dès ! ( un double pour sortir de prison )";
                }
                return "Rien à signaler";
            default:
                return "Autre évenement";
        }
    }

    public void casePropriete(IPossession possession) throws InsufficientFundsException {
        Joueur joueurActuel = listeJoueurs[tourJoueur];
        if(joueurActuel.possedePossession(possession) && possession instanceof Propriete){
            // cas ou le joueur possde la propriete
            choixAmelioration((Propriete) possession);
        }
        else{
            if (possession.getProprietaire() != null){
                // cas ou le joueur ne possede pas la propriete & la propriete à deja été achetée
                listeJoueurs[tourJoueur].decrCapital(possession.getLoyerAPayer());
                possession.getProprietaire().incrCapital(possession.getLoyerAPayer());
            }
            else{
                // cas ou le joueur ne possede pas la propriete & la propriete n'a pas été achetée
                acheter(possession);
            }
        }
    }

    public String caseDepart(){
        // Reçoit 200 $
        listeJoueurs[tourJoueur].incrCapital(SOMME_DEPART);
        return "Vous passez par la case depart ! \n Recevez 200 $ !";
    }

    public void tirageCaisseCommunaute(String Action) throws InsufficientFundsException {
        switch (Action){
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
                allerPrison(listeJoueurs[tourJoueur]);
                break;
            case "Sortie-Prison":
                listeJoueurs[tourJoueur].carteSortiPrison();
                carteCommunauteSortiPrisonEnJeu = listeJoueurs[tourJoueur];
                break;
        }
    }

    public void tirageChance(String Action) throws InsufficientFundsException {

        switch (Action){
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
                allerPrison(listeJoueurs[tourJoueur]);
                break;
            case "Sortie-Prison":
                listeJoueurs[tourJoueur].carteSortiPrison();
                carteChanceSortiPrisonEnJeu = listeJoueurs[tourJoueur];
                break;
        }
    }

    public String allerPrison(Joueur joueur){
        joueur.allerEnPrison();
        return "Vous allez directement en prison sans passer par la case départ !";
    }

    public String payerImpot(int total) throws InsufficientFundsException {
        listeJoueurs[tourJoueur].decrCapital(total);
        return "Vous payez la somme de :" + total;
    }

    public void incrTourJoueur() {
        tourJoueur = (tourJoueur + 1) % nbJoueur;
    }

    public void incrTourGolbal() {
        tourGolbal++;
        incrTourPrison();
    }

    public void incrTourPrison(){
        for(Joueur j : listeJoueurs){
            if (j.getEstEnPrison()){
                j.incrTourPrison();
            }
        }
    }

    public void remplirDeckChance() {
        if (listeChance == null) {
            System.err.println("⚠️ Liste des cartes Chance non chargée !");
            listeChance = new ArrayList<>();
        }

        deckChance = new ArrayList<>(listeChance);

        if (carteChanceSortiPrisonEnJeu != null) {
            deckChance.removeIf(c -> "Sorti de Prison".equals(c.getAFaire()));
        }

        Collections.shuffle(deckChance);
    }

    public void remplirDeckCaisseCommunaute() {
        if (listeCaisseCommunaute == null) {
            System.err.println("⚠️ Liste des cartes Caisse de Communauté non chargée !");
            listeCaisseCommunaute = new ArrayList<>();
        }

        deckCaisseCommunaute = new ArrayList<>(listeCaisseCommunaute);

        if (carteCommunauteSortiPrisonEnJeu != null) {
            deckCaisseCommunaute.removeIf(c -> "Sorti de Prison".equals(c.getAFaire()));
        }

        Collections.shuffle(deckCaisseCommunaute);
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
                int somme = donneur.decrCapital(part);
                joueur.incrCapital(somme);
            }
        }
    }

    public void creditJoueur(int total, Joueur receveur) throws InsufficientFundsException {
        for (Joueur joueur : listeJoueurs) {
            if (joueur != receveur) {
                int somme = joueur.decrCapital(total);
                receveur.incrCapital(somme);
            }
        }
    }

    public String acheter(IPossession bienAVendre) {
        if(bienAVendre.getProprietaire() != null){
            return "Cette propriété appartient déjà à un autre joueur.";
        }
        Joueur jcourrant = listeJoueurs[tourJoueur];
        if (jcourrant.getCapitalTotal()< bienAVendre.getPrixAchat()){
            return "Fonds insuffisants pour acheter " + bienAVendre.getNom();
        }
        bienAVendre.setProprietaire(jcourrant);
        jcourrant.decrCapital(bienAVendre.getPrixAchat());
        jcourrant.ajouterPropriete(bienAVendre);

        if (bienAVendre instanceof Gare) {
            jcourrant.setNbGare(jcourrant.getNbGare() + 1);
        }

        if(bienAVendre instanceof ServicePublic){
            jcourrant.setNbService(jcourrant.getNbGare() + 1);
        }

        listePossessionJoueur.put(bienAVendre, jcourrant);

        return jcourrant.getNom() + " vient de faire l'acquisition de " + bienAVendre.getNom() + " !";

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
    }

    public void jouerTour() {
        Joueur joueurCourrant = listeJoueurs[tourJoueur];

        verifierVictoire();
        incrTourJoueur();
    }

    public void victoire(Joueur joueur){
        System.out.println(" Bravo ! \n" + joueur.getNom() + " a gagné !");
    }

    public boolean isVictoire() {
        return victoire;
    }

    public void setVictoire(boolean victoire) {
        this.victoire = victoire;
    }

    public void deplacerJoueur(int nbCases) throws InsufficientFundsException {
        Joueur joueur = listeJoueurs[tourJoueur];
        int nouvellePosition = (joueur.getCaseActuelle() + nbCases) % 40;
        joueur.setCaseActuelle(nouvellePosition);

        ICase caseActuelle = plateau.getCase(nouvellePosition);
        switch (caseActuelle.getId()){
            case 2,18,33:
                // communauté
                piocherCarteCommunaute();
                break;
            case 7,22,36:
                // chance
                piocherCarteChance();
                break;
            case 30:
                allerPrison(joueur);
                break;
            case 4:
                payerImpot(100);
                break;
            case 38:
                payerImpot(200);
                break;
            default:
                // autres cases
                break;
        }
    }

    public void choixAmelioration(Propriete propriete) {
        listeJoueurs[tourJoueur].AmeliorationPropriete(propriete);
    }

    public Joueur getJoueurAJouerSuivant(){
        return listeJoueurs[(tourJoueur+1)%nbJoueur];
    }

    public boolean payerPrison(){
        return listeJoueurs[tourJoueur].getCapitalTotal() > 50;
    }
}

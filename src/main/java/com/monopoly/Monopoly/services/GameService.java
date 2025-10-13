package com.monopoly.Monopoly.services;

import com.monopoly.Monopoly.models.*;
import com.monopoly.Monopoly.models.plateau.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private Partie partie;
    private Plateau plateau;

    public GameService() {
        this.partie = new Partie(4);
        this.plateau = this.partie.getPlateau();
        partie.creationJoueur(1,"Alice","♙");
        partie.creationJoueur(2,"Bob","♕");
        partie.creationJoueur(3,"Charlie","♠");
        partie.creationJoueur(4,"Dylan","♥");
    }

    public Map<String, Object> getState() {
        Map<String, Object> state = new HashMap<>();
        state.put("tourGlobal", partie.getTourGolbal());
        state.put("tourJoueur", partie.getTourJoueur());
        state.put("nbJoueurs", partie.getNbJoueur());
        state.put("joueurs", partie.getListeJoueursInfo());
        state.put("plateau", partie.getPlateauInfo());
        return state;
    }

    public RollResult rollDice() {
        Joueur joueur = partie.getJoueurAJouer();
        if(joueur.getLancerDesRestant() == 0){
            partie.getJoueurAJouerSuivant().incrLancerDes();
            return null;
        }
        int des1 = partie.lancerDesSimple();
        int des2 = partie.lancerDesSimple();
        int[] total = {des1, des2};

        boolean enPrison = false;

        if (des1 == des2){
            joueur.incrCptDouble();
            joueur.incrLancerDes();

            if( joueur.getCptDouble() >= 3){
                joueur.allerEnPrison();
                partie.getJoueurAJouerSuivant().incrLancerDes();
                enPrison = true;
            }
        }
        else{
            joueur.setCptDouble(0);
        }
        if(!enPrison) {
            partie.getJoueurAJouer().decrLancerDes();
            partie.getJoueurAJouerSuivant().incrLancerDes();
        }
        return new RollResult(total, enPrison);
    }

    public Joueur finTour() {
        Joueur joueurAJouer = partie.getJoueurAJouer();
        if(joueurAJouer.getEstEnPrison()){
            joueurAJouer.incrTourPrison();
        }
        partie.incrTourJoueur();
        return joueurAJouer;
    }

    public String buyProperty(int id) {
        try {
            Propriete propriete = (Propriete) partie.getPlateau().getCase(id);
            partie.acheter(propriete);
            return "Propriété " + propriete.getNom() + " achetée !";
        } catch (Exception e) {
            return "Erreur lors de l'achat : " + e.getMessage();
        }
    }

    public List<ICase> getPlateau() {
        return partie.getPlateau().getTotalCase();
    }

    public ICase getPlateauId(int id) {
        return partie.getPlateau().getTotalCase().get(id);
    }

    public int getCaseJoueur(Joueur joueur) {
        Joueur j = partie.getJoueur(joueur);
        return j.getCaseActuelle();
    }

    public Joueur[] getJoueurs() {
        List<Joueur> joueurs = new ArrayList<>();
        for(Joueur j: partie.getListeJoueurs()){
            if (j != null) joueurs.add(j);
        }
        Joueur[] joueurNonNull = joueurs.toArray(new Joueur[joueurs.size()]);
        return joueurNonNull;
    }

    public int getTourJoueur(){
        return partie.getTourJoueur();
    }

    public int getJoueur(int id){
        return partie.getPositionJoueur(id);
    }

    public Joueur getJoueurAJouer(){
        return partie.getJoueurAJouer();
    }

    public String[] deplacerJoueur(int nbCases){
        Joueur j = getJoueurAJouer();
        String sortie = j.avancer(nbCases);
        return new String[]{partie.getPlateau().getCase(j.getCaseActuelle()).getNom(), sortie };
    }

    public void deplacerJusqua(int idCase){
        Joueur joueurCourrant = partie.getJoueurAJouer();
        joueurCourrant.avancerJusqua(idCase);
    }

    public boolean estPropriete(int caseActuelle) {
        return partie.getPlateau().getTotalCase().get(caseActuelle) instanceof IPossession;
    }

    public PrisonStatus estEnPrison(){
        Joueur joueur = partie.getJoueurAJouer();
        return new PrisonStatus(joueur.getEstEnPrison(),joueur.getNbTourEntrePrison());
    }

    public Carte getChance(){
        return partie.piocherCarteChance();
    }

    public Carte getCommunaute(){
        return partie.piocherCarteCommunaute();
    }

    public void actionCarteChance(Carte carte) throws InsufficientFundsException {
        partie.tirageChance(carte.getAFaire());
    }

    public void actionCarteCommunaute(Carte carte) throws InsufficientFundsException {
        partie.tirageCaisseCommunaute(carte.getAFaire());
    }

    public int getMoney(){
        return partie.getJoueurAJouer().getCapitalTotal();
    }

    public int getMoneyCible(int id){
        return partie.getJoueur(id).getCapitalTotal();
    }

    public int getNbRoll(){
        return partie.getJoueurAJouer().getLancerDesRestant();
    }

    public int incrNbRoll(){
        return partie.getJoueurAJouer().incrLancerDes();
    }

    public void incrNbDouble(){
        partie.getJoueurAJouer().incrCptDouble();
    }

    public int decrNbRoll(){
        return partie.getJoueurAJouer().decrLancerDes();
    }

    public String actionCase(int id) throws InsufficientFundsException {
        return partie.caseEvenement(id);
    }

    public int getNbDouble(){
        return partie.getJoueurAJouer().getCptDouble();
    }

    public void allerEnPrison(){
        partie.getJoueurAJouer().allerEnPrison();
    }

    public void sortiePrison() {
        Joueur joueurCourrant = partie.getJoueurAJouer();
        joueurCourrant.setEstEnPrison(false);
        joueurCourrant.setNbTourEntrePrison(0);
        joueurCourrant.setCptDouble(0);
    }

    public int[] tenterChanceDes(){
        Joueur joueur = getJoueurAJouer();
        if(!joueur.getEstEnPrison()) return null;
        int[] sortie = partie.lancerDesDouble();
        if(sortie[0] == sortie[1]){
            joueur.setEstEnPrison(false);
            joueur.setNbTourEntrePrison(0);
            joueur.incrLancerDes();
        }
        return sortie;
    }

    public int getNbTourPrison(){
        return partie.getJoueurAJouer().getNbTourEntrePrison();
    }

    public boolean payerPrison(){
        return partie.payerPrison();

    }
}

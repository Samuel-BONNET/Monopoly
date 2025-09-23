package com.monopoly.Monopoly.services;

import com.monopoly.Monopoly.models.Joueur;
import com.monopoly.Monopoly.models.Partie;
import com.monopoly.Monopoly.models.plateau.ICase;
import com.monopoly.Monopoly.models.plateau.IPossession;
import com.monopoly.Monopoly.models.plateau.Plateau;
import com.monopoly.Monopoly.models.plateau.Propriete;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private Partie partie;
    private Plateau plateau = new Plateau();
    private List<Joueur> joueurs = List.of(
            new Joueur("Alice", "♙"),
            new Joueur("Bob", "♕"),
            new Joueur("Charlie", "♠"),
            new Joueur("Dylan", "♥")
    );



    public GameService() {
        this.partie = new Partie(4);
        this.partie.getPlateau();
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

    public int rollDice() {
        int des1 = partie.lancerDesSimple();
        int des2 = partie.lancerDesSimple();
        int total = des1 + des2;

        if (des1 == des2) partie.getJoueurAJouer().incrCptDouble(partie.getTourGolbal());
        partie.getJoueurAJouer().avancer(total);
        return total;
    }

    public Joueur finTour() {
        partie.incrTourJoueur();
        return partie.getJoueurAJouer();
    }

    public String buyProperty(int id) {
        try {
            Propriete propriete = (Propriete) partie.getPlateau().getTotalCase().get(id);
            partie.acheter(propriete);
            return "Propriété " + propriete.getNom() + " achetée !";
        } catch (Exception e) {
            return "Erreur : " + e.getMessage();
        }
    }

    public Plateau getPlateau() {
        return partie.getPlateau();
    }

    public ICase getPlateauId(int id) {
        return partie.getPlateau().getTotalCase().get(id);
    }

    public int getCaseJoueur(Joueur joueur) {
        Joueur j = partie.getJoueur(joueur);
        return j.getCaseActuelle();
    }

    public Joueur[] getJoueurs() {
        return partie.getListeJoueurs();
    }

    public int getTourJoueur(){
        return partie.getTourJoueur();
    }

    public int getJoueur(int id){
        return partie.getPositionJoueur(id);
    }

    public Joueur getJoueurAJouer(){
        return joueurs.get(getTourJoueur());
    }

    public void deplacerJoueur(int joueurIndex, int nbCases){
        Joueur j = joueurs.get(joueurIndex);
        j.setCaseActuelle((j.getCaseActuelle() + nbCases) % 40);
    }

    public boolean estPropriete(int caseActuelle) {
        return partie.getPlateau().getTotalCase().get(caseActuelle) instanceof IPossession;
    }
}

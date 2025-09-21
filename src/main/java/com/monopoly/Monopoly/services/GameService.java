package com.monopoly.Monopoly.services;

import com.monopoly.Monopoly.models.Joueur;
import com.monopoly.Monopoly.models.Partie;
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
            new Joueur("Bob", "♕")
    );



    public GameService() {
        this.partie = new Partie(2);
        this.partie.getPlateau();
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
        int total = partie.lancerDesSimple() + partie.lancerDesSimple();
        partie.getListeJoueurs()[partie.getTourJoueur()].avancer(total);
        return total;
    }

    public Map<String, Object> endTurn() {
        partie.incrTourJoueur();
        Map<String, Object> result = new HashMap<>();
        result.put("tourJoueur", partie.getTourJoueur());
        result.put("tourGlobal", partie.getTourGolbal());
        return result;
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

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public int getTourJoueur(){
        return partie.getTourJoueur();
    }

    public void deplacerJoueur(int joueurIndex, int nbCases){
        Joueur j = joueurs.get(joueurIndex);
        j.setCaseActuelle((j.getCaseActuelle() + nbCases) % 40);
    }

}

package com.monopoly.Monopoly.services;

import com.monopoly.Monopoly.models.Partie;
import com.monopoly.Monopoly.models.plateau.Propriete;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameService {

    private Partie partie;

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

    public Map<String, Object> rollDice() {
        int[] des = partie.lancerDesDouble();

        partie.getListeJoueurs()[partie.getTourJoueur()].avancer(des[0] + des[1]);

        Map<String, Object> result = new HashMap<>();
        result.put("de1", des[0]);
        result.put("de2", des[1]);
        result.put("nouvellePosition", partie.getListeJoueurs()[partie.getTourJoueur()].getCaseActuelle());

        return result;
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

}

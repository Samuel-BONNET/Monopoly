package com.monopoly.Monopoly.controller;

import com.monopoly.Monopoly.models.Joueur;
import com.monopoly.Monopoly.models.plateau.ICase;
import com.monopoly.Monopoly.models.plateau.IPossession;
import com.monopoly.Monopoly.models.plateau.Plateau;
import com.monopoly.Monopoly.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GameController {

    @Autowired
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/state")
    public Map<String, Object> getState() {
        return gameService.getState();
    }

    @PostMapping("/roll")
    public int rollDice() {
        return gameService.rollDice();
    }

    @PostMapping("/finTour")
    public Joueur finTour() {
        Joueur joueur = gameService.finTour();
        return joueur;
    }

    @PostMapping("/buy/{id}")
    public String buyProperty(@PathVariable int id) {
        return gameService.buyProperty(id);
    }

    @GetMapping("/plateau")
    public Plateau getPlateau() {
        return gameService.getPlateau();
    }

    @GetMapping("/plateau/{id}")
    public ICase getPlateauId(@PathVariable int id) {
        return gameService.getPlateauId(id);
    }

    @GetMapping("/joueurs")
    public Joueur[] getJoueurs() {
        return gameService.getJoueurs();
    }

    @GetMapping("/joueurs/{id}")
    public int getJoueur(@PathVariable int id) {
        return gameService.getJoueur(id);
    }

    @GetMapping("/caseJoueur")
    public int getCaseJoueur() {
        return gameService.getCaseJoueur(getJoueurAjouer());
    }

    @GetMapping("/estPropriete/{id}")
    public boolean estPropriete(@PathVariable int id) {
        return gameService.estPropriete(id);
    }

    @GetMapping("/tourJoueur")
    public int getTourJoueur() {
        return gameService.getTourJoueur();
    }
    @GetMapping("/joueurAJouer")
    public Joueur getJoueurAjouer() {
        return gameService.getJoueurAJouer();
    }

    @GetMapping("/caseActuelle")
    public int getCaseActuelle(){
        return gameService.getJoueurAJouer().getCaseActuelle();
    }

    @PostMapping("/deplacer/{joueurIndex}/{nbCases}")
    public void deplacerJoueur(@PathVariable int joueurIndex, @PathVariable int nbCases) {
        gameService.deplacerJoueur(joueurIndex, nbCases);
    }
}

package com.monopoly.Monopoly.controller;

import com.monopoly.Monopoly.models.Joueur;
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

    @PostMapping("/end-turn")
    public Map<String, Object> endTurn() {
        return gameService.endTurn();
    }

    @PostMapping("/buy/{id}")
    public String buyProperty(@PathVariable int id) {
        return gameService.buyProperty(id);
    }

    @GetMapping("/plateau")
    public Plateau getPlateau() {
        return gameService.getPlateau();
    }

    @GetMapping("/joueurs")
    public List<Joueur> getJoueurs() {
        return gameService.getJoueurs();
    }

    @GetMapping("/tourJoueur")
    public int getTourJoueur() {
        return gameService.getTourJoueur();
    }



    @PostMapping("/deplacer/{joueurIndex}/{nbCases}")
    public void deplacerJoueur(@PathVariable int joueurIndex, @PathVariable int nbCases) {
        gameService.deplacerJoueur(joueurIndex, nbCases);
    }
}

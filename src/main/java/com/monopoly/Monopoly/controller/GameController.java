package com.monopoly.Monopoly.controller;

import com.monopoly.Monopoly.services.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/state")
    public Map<String, Object> getState() {
        return gameService.getState();
    }

    @PostMapping("/roll")
    public Map<String, Object> rollDice() {
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
}

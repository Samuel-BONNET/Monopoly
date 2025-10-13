package com.monopoly.Monopoly.controller;

import com.monopoly.Monopoly.models.InsufficientFundsException;
import com.monopoly.Monopoly.models.Joueur;
import com.monopoly.Monopoly.models.PrisonStatus;
import com.monopoly.Monopoly.models.RollResult;
import com.monopoly.Monopoly.models.plateau.Carte;
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

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/state")
    public Map<String, Object> getState() {
        return gameService.getState();
    }

    @GetMapping("/roll")
    public RollResult rollDice() {
        return gameService.rollDice();
    }

    @GetMapping("/nbRoll")
    public int getNbRoll() {
        return gameService.getNbRoll();
    }

    @GetMapping("/incrNbRoll")
    public int incrNbRoll() {
        return gameService.incrNbRoll();
    }

    @GetMapping("/incrNbDouble")
    public void incrNbDouble(){
        gameService.incrNbDouble();
    }

    @GetMapping("/estTripleDouble")
    public int getNbDouble(){
        return gameService.getNbDouble();
    }

    @GetMapping("/enPrison")
    public PrisonStatus getPrisonStatus() {
        Joueur joueur = gameService.getJoueurAJouer();
        return new PrisonStatus(joueur.getEstEnPrison(), joueur.getNbTourEntrePrison());
    }

    @GetMapping("/peutTenterChance")
    public boolean peutTenterChance(){
        Joueur joueur = gameService.getJoueurAJouer();
        return joueur.getNbTourEntrePrison() < 4;
    }

    @GetMapping("/decrNbRoll")
    public int decrNbRoll() {
        return gameService.decrNbRoll();
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
    public ICase[] getPlateau() {
        return gameService.getPlateau().toArray(ICase[]::new);
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

    @GetMapping("/money/{id}")
    public int getMoney(@PathVariable int id){
        return gameService.getJoueurs()[getJoueur(id)].getCapitalTotal();
    }

    @GetMapping("/money")
    public int getActualMoney(){
        return gameService.getMoney();
    }

    @PostMapping("/money/{id}")
    public int getActualMoneyCible(@PathVariable int id){
        return gameService.getMoneyCible(id);
    }

    @GetMapping("/decrMoney/{amount}")
    public double decrActualMoney(@PathVariable int amount){
        return gameService.getJoueurAJouer().decrCapital(amount);
    }

    @GetMapping("/incrMoney/{amount}")
    public double incrActualMoney(@PathVariable int amount){
        return gameService.getJoueurAJouer().incrCapital(amount);
    }

    // todo : feat incr/decr specific money

    @PostMapping("/deplacer/{nbCases}")
    public String[] deplacerJoueur(@PathVariable int nbCases) throws InsufficientFundsException {
        return gameService.deplacerJoueur(nbCases);
    }

    @GetMapping("/chance")
    public Carte getChance() {
        return gameService.getChance();
    }

    @GetMapping("/communaute")
    public Carte getCommunaute() {
        return gameService.getCommunaute();
    }

    @PostMapping("/ActionCarteChance")
    public void appliquerCarteChance(@RequestBody Carte carte) throws InsufficientFundsException {
        gameService.actionCarteChance(carte);
    }

    @PostMapping("/ActionCarteCommunaute")
    public void appliquerCarteCommunaute(@RequestBody Carte carte) throws InsufficientFundsException {
        gameService.actionCarteCommunaute(carte);
    }

    @GetMapping ("/actionCase{id}")
    public String appliquerCase(@PathVariable int id) throws InsufficientFundsException {
        return gameService.actionCase(id);
    }

    @PostMapping("/envoyerPrison")
    public void allerEnPrison(){
        gameService.allerEnPrison();
        gameService.deplacerJusqua(10);
    }

    @PostMapping("/sortiePrison")
    public void sortiePrison(){
        gameService.sortiePrison();
    }

    @GetMapping("/tenterChancePrison")
    public int[] tenterChancePrison(){
        int[] des = gameService.tenterChanceDes();
        return des != null ? des : new int[]{0, 0};
    }

    @PostMapping("/payerPrison")
    public boolean payerPrison(){
        return gameService.payerPrison();
    }

    @GetMapping("/testPrison")
    public void testPrison(){
        Joueur j = gameService.getJoueurAJouer();
        j.setEstEnPrison(true);
        j.setNbTourEntrePrison(0);
    }

}

package com.monopoly.Monopoly.models.plateau;

import java.util.List;

public class CarteMonopoly {

    private List<Carte> Ccommunaute,Chance;

    // -------------------------------
    // 🔧 Getters / Setters
    // -------------------------------

    public List<Carte> getCcommunaute() {
        return Ccommunaute;
    }

    public void setCcommunaute(List<Carte> Ccommunaute) {
        this.Ccommunaute = Ccommunaute;
    }

    public List<Carte> getChance() {
        return Chance;
    }

    public void setChance(List<Carte> Chance) {
        this.Chance = Chance;
    }
}

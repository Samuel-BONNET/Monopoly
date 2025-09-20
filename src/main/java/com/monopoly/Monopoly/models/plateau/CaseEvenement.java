package com.monopoly.Monopoly.models.plateau;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseEvenement implements ICase{

    @JsonProperty
    private int id, numero;
    @JsonProperty
    private String nom;
    private boolean estHypotheque = false;

    CaseEvenement(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    CaseEvenement(){
        // Pour Jackson
    }

    // -------------------------------
    // 🔧 Getters / Setters
    // -------------------------------

    public int getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public String getNom() {
        return nom;
    }

    public boolean isEstHypotheque() {
        return estHypotheque;
    }

    public void setEstHypotheque(boolean estHypotheque) {
        this.estHypotheque = estHypotheque;
    }
}

package com.monopoly.Monopoly.models.plateau;

public class CaseEvenement implements ICase{

    private int id;
    private String nom;
    private boolean estHypotheque = false;

    CaseEvenement(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    // -------------------------------
    // 🔧 Getters / Setters
    // -------------------------------

    public int getId() {
        return id;
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

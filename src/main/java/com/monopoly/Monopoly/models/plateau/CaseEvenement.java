package com.monopoly.Monopoly.models.plateau;

public class CaseEvenement implements ICase{

    private int id;
    private String nom;

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


}

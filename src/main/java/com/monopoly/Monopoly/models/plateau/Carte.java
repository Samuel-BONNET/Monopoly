package com.monopoly.Monopoly.models.plateau;

import com.monopoly.Monopoly.models.enums.TypeCarte;

public class Carte {
    private static int compteur = 0;
    private int id;
    private TypeCarte typeCarte;
    private String description, aFaire;

    Carte(int id, TypeCarte typeCarte, String description, String aFaire) {
        this.id = id;
        this.typeCarte = typeCarte;
        this.description = description;
        this.aFaire = aFaire;
    }

    Carte(TypeCarte typeCarte, String description, String aFaire) {
        this(compteur++, typeCarte, description, aFaire);
    }

    public int getId() {
        return id;
    }

    public TypeCarte getTypeCarte() {
        return typeCarte;
    }

    public String getDescription() {
        return description;
    }

    public String getaFaire() {
        return aFaire;
    }

    @Override
    public String toString() {
        return "Carte{" +
                "id=" + id +
                ", typeCarte=" + typeCarte +
                ", description='" + description + '\'' +
                ", aFaire='" + aFaire + '\'' +
                '}';
    }
}

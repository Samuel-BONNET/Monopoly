package com.monopoly.Monopoly.models.plateau;

import com.monopoly.Monopoly.models.Joueur;

public interface IPossession extends ICase {

    public int getId();
    public int getPrixAchat();
    public String getNom();
    public Joueur getProprietaire();
    public void setProprietaire(Joueur joueur);
    public void setEstHypothequee(boolean valeur);
    public boolean getEstHypothequee();
    public int getLoyerAPayer();
}

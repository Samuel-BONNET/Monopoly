package com.monopoly.Monopoly.models.plateau;

public interface IPossession extends ICase {

    public int getId();
    public int getPrixAchat();
    public String getNom();
    public int calculerLoyer();
}

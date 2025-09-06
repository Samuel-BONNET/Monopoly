package com.monopoly.Monopoly.models.enums;

public enum Argent {
    UN(1),
    CINQ(5),
    DIX(10),
    VINGT(20),
    CINQUANTE(50),
    CENT(100),
    DEUX_CENTS(200),
    CINQ_CENTS(500);

    private final int value;

    Argent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

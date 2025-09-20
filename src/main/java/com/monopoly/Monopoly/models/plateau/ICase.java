package com.monopoly.Monopoly.models.plateau;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.monopoly.Monopoly.models.enums.TypeCarte;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "typeCase"
)

@JsonSubTypes({
        @JsonSubTypes.Type(value = Propriete.class, name = "Propriete"),
        @JsonSubTypes.Type(value = Gare.class, name = "Gare"),
        @JsonSubTypes.Type(value = ServicePublic.class, name = "Service-Public"),
        @JsonSubTypes.Type(value = CaseEvenement.class, name = "Depart"),
        @JsonSubTypes.Type(value = CaseEvenement.class, name = "Ccommunauté"),
        @JsonSubTypes.Type(value = CaseEvenement.class, name = "Chance"),
        @JsonSubTypes.Type(value = CaseEvenement.class, name = "Visite-Prison"),
        @JsonSubTypes.Type(value = CaseEvenement.class, name = "Parc"),
        @JsonSubTypes.Type(value = CaseEvenement.class, name = "Prison"),
        @JsonSubTypes.Type(value = CaseEvenement.class, name = "Impot"),
        @JsonSubTypes.Type(value = CaseEvenement.class, name = "Taxe")
})

public interface ICase {

    public int getId();
    public String getNom();
    public int getNumero();
}

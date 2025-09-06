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
        @JsonSubTypes.Type(value = ServicePublic.class, name = "Service-Public")
})

public interface ICase {
    public int getId();
    public String getNom();
}

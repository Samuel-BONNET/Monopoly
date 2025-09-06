package com.monopoly.Monopoly.models.plateau;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;

public class Plateau {

    private ICase[] plateau = new ICase[40];

    public Plateau() {
        setUpPlateau();
    }

    public void setUpPlateau(){
        try{
            ObjectMapper mapper = new ObjectMapper();
            List<ICase> totalCase = mapper.readValue(
                new File("cases.json"),
                new TypeReference<List<ICase>>() {}
            );
        }
        catch(Exception e){
            System.err.println(e);
        }
    }






}

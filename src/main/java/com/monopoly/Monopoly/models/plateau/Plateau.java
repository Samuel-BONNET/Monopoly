package com.monopoly.Monopoly.models.plateau;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;

public class Plateau {

    private ICase[] plateau = new ICase[40];
    private List<Propriete> totalPropriete;
    private List<ICase> totalCase;

    public Plateau() {
        setUpPropriete();
        setUpPlateau();
    }

    public List<ICase> getTotalPropriete() {
        return totalCase;
    }

    public ICase[] getPlateau() {
        return plateau;
    }

    public List<ICase> getTotalCase() {
        return totalCase;
    }

    public ICase getCase(int index) {
        return plateau[index];
    }


    public void setUpPlateau(){
        try{
            ObjectMapper mapper = new ObjectMapper();
            List<CaseJson> caseJsons = mapper.readValue(
                    new File("cases.json"),
                    new TypeReference<List<CaseJson>>(){}
            );

            plateau = new ICase[40];
            for (CaseJson caseJson : caseJsons){
                ICase caseObjet;

                switch (caseJson.typeCase){
                    case "Propriete":
                        // On charge la propriete correspondante dans la liste des propriétées
                        caseObjet = totalPropriete.get(caseJson.idPropriete-1);
                        break;
                    case "Gare":
                        caseObjet = new Gare(caseJson.numero,caseJson.nom);
                        break;
                    case "Service-Public":
                        caseObjet = new ServicePublic(caseJson.numero,caseJson.nom);
                        break;
                    default:
                        caseObjet = new CaseEvenement(caseJson.numero,caseJson.nom);
                }
                plateau[caseJson.numero] = caseObjet;
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setUpPropriete(){
        try{
            ObjectMapper mapper = new ObjectMapper();
            totalPropriete = mapper.readValue(
                    new File("proprietes.json"),
                    new TypeReference<List<Propriete>>(){}
            );
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }






}

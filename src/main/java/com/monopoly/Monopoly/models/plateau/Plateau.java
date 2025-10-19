package com.monopoly.Monopoly.models.plateau;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Plateau {

    private ICase[] plateau = new ICase[40];
    private List<Propriete> totalPropriete= new ArrayList<>();
    private List<ICase> totalCase= new ArrayList<>();

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
                    new File("src/main/resources/cases.json"),
                    new TypeReference<List<CaseJson>>(){}
            );

            Map<Integer, Propriete> proprieteMap = totalPropriete.stream()
                    .collect(Collectors.toMap(Propriete::getId, p -> p));

            for (CaseJson caseJson : caseJsons){
                ICase caseObjet;

                switch (caseJson.typeCase){
                    case "Propriete":
                        // On charge la propriete correspondante dans la liste des propriétées
                        if (caseJson.idPropriete != null && caseJson.idPropriete > 0 && caseJson.idPropriete <= totalPropriete.size()) {
                            caseObjet = totalPropriete.get(caseJson.idPropriete - 1);
                        }
                        else caseObjet = null;
                        break;
                    case "Gare":
                        caseObjet = new Gare(caseJson.numero,caseJson.nom);
                        break;
                    case "Service-Public":
                        caseObjet = new ServicePublic(caseJson.numero,caseJson.nom);
                        break;
                    case "Chance","Ccommunauté","Prison","Visite-Prison","Parc","Taxe","Depart":
                        caseObjet = new CaseEvenement(caseJson.numero,caseJson.nom);
                    default:
                        caseObjet = new CaseEvenement(caseJson.numero,caseJson.nom);
                }
                plateau[caseJson.numero] = caseObjet;
                if (caseObjet != null) totalCase.add(caseObjet);
                else System.out.println("Attention : case " + caseJson.numero + " non initialisée !");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        totalCase.sort(Comparator.comparingInt(ICase::getId));
    }

    public void setUpPropriete(){
        try{
            ObjectMapper mapper = new ObjectMapper();
            totalPropriete = mapper.readValue(
                    new File("src/main/resources/proprietes.json"),
                    new TypeReference<List<Propriete>>(){}
            );
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

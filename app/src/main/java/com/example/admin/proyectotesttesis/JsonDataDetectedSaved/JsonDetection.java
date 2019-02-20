package com.example.admin.proyectotesttesis.JsonDataDetectedSaved;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class JsonDetection {

    private JSONObject jsonDetections = null;
    private List<BoxDetected> boxDetectedList = null;

    public JsonDetection( List<BoxDetected> boxDetectedList ){
        this.boxDetectedList = boxDetectedList;
        this.jsonDetections = new JSONObject();
    }

    public void addBoxDetected( BoxDetected boxDetected ) {
        if( this.boxDetectedList == null ) { this.boxDetectedList = new LinkedList<BoxDetected>(); }
        this.boxDetectedList.add(boxDetected);
    }

    public void makeJsonDetections(){
        if ( this.jsonDetections== null ){ this.jsonDetections = new JSONObject(); }

        for ( BoxDetected boxDetected : this.boxDetectedList ){
            try {
                JSONObject jsonImg = new JSONObject();
                jsonImg.put("name", boxDetected.getNameImg());


                if( boxDetected.hasRegions() ){
                    JSONObject jsonBoxes = new JSONObject();
                    int i = 0;
                    for (Region region : boxDetected.getRegions()) {
                        JSONObject jsonBox = new JSONObject();
                        jsonBox.put("xmin", region.getX1());
                        jsonBox.put("ymin", region.getY1());
                        jsonBox.put("xmax", region.getX2());
                        jsonBox.put("ymax", region.getY2());
                        jsonBox.put("width", region.getWidth());
                        jsonBox.put("height", region.getHeight());

                        jsonBoxes.put(""+i,jsonBox);
                        i++;
                    }
                    jsonImg.put("Boxes", jsonBoxes);
                }

                this.jsonDetections.put(boxDetected.getNameImg().split("\\.")[0], jsonImg);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return this.jsonDetections.toString();
    }
}

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
                JSONObject jsonBox = new JSONObject();
                jsonBox.put("filename", boxDetected.getNameImg());

                JSONArray jsonArrRegion = new JSONArray();
                if( boxDetected.hasRegions() ){
                    for (Region region : boxDetected.getRegions()) {
                        JSONObject jsonRegion = new JSONObject();
                        jsonRegion.put("x", region.getX1());
                        jsonRegion.put("y", region.getY1());
                        jsonRegion.put("x2", region.getX2());
                        jsonRegion.put("y2", region.getY2());
                        jsonRegion.put("width", region.getWidth());
                        jsonRegion.put("height", region.getHeight());

                        jsonArrRegion.put(jsonRegion);
                    }
                }
                jsonBox.put("regions", jsonArrRegion);
                this.jsonDetections.put("" + boxDetected.getNameImg(), jsonBox);
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

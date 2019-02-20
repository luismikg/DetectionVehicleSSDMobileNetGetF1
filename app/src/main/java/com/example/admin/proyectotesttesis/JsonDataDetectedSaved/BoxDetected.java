package com.example.admin.proyectotesttesis.JsonDataDetectedSaved;

import com.example.admin.proyectotesttesis.Box;

import java.util.LinkedList;
import java.util.List;

public class BoxDetected {

    private String nameImg;
    private List<Region> regions;

    public BoxDetected() {
    }

    public BoxDetected(String name, List<Region> regions) {
        this.setNameImg(name);
        this.setRegions(regions);
    }

    public BoxDetected(String name) {
        this.setNameImg(name);
    }

    public String getNameImg() {
        return nameImg;
    }

    public void setNameImg(String nameImg) {
        this.nameImg = nameImg;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    public void  addRegionFromBox(Box box){
        this.addRegion( box.getxMin(), box.getyMin(), box.getxMax(), box.getyMax(), box.getConfidence() );
    }

    public void  addRegion( float x1, float y1, float x2, float y2, float score ){
        Region region = new Region( x1, y1, x2, y2, x2-x1,  y2-y1, score);
        this.addRegion( region );
    }

    public void  addRegion( Region region ){
        if( this.regions == null ){ this.regions = new LinkedList<Region>(); }
        this.regions.add( region );
    }

    public boolean hasRegions(){
        return (this.regions != null);
    }
}

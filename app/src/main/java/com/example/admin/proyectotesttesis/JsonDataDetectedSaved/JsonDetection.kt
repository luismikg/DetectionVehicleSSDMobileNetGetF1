package com.example.admin.proyectotesttesis.JsonDataDetectedSaved

import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JsonDetection( boxDetectedList: List<BoxDetected> ){
    private var jsonDetections: JSONObject?
    private lateinit  var boxDetectedList: List<BoxDetected>


    init {
        this.jsonDetections = JSONObject()
    }

    fun addBoxDetected( boxDetected: BoxDetected ) {
        if( this.boxDetectedList == null ) { this.boxDetectedList = LinkedList<BoxDetected>() }

        (this.boxDetectedList as LinkedList<BoxDetected> ).add(boxDetected)
    }

    fun saveDetections(){
        if ( this.jsonDetections== null ){ this.jsonDetections = JSONObject() }

        for ( boxDetected: BoxDetected in this.boxDetectedList ){
            var jsonBox = JSONObject()
            jsonBox.put("filename", boxDetected.nameImg)

            var jsonArrRegion = JSONArray()
            for ( region:Region in boxDetected.regions ){
                var jsonRegion = JSONObject()
                jsonRegion.put("x1", region.x1)
                jsonRegion.put("y1", region.y1)
                jsonRegion.put("x2", region.x2)
                jsonRegion.put("y2", region.y2)
                jsonRegion.put("witdth", region.width)
                jsonRegion.put("height", region.height)

                jsonArrRegion.put( jsonRegion )
            }
            jsonBox.put("Regions", jsonArrRegion)
            this.jsonDetections?.put(""+boxDetected.nameImg, jsonBox)
        }


    }

    override fun toString(): String {
        return this.jsonDetections.toString()
    }

}
package com.example.admin.proyectotesttesis.JsonDataDetectedSaved;

import android.system.ErrnoException;

public class Region {
    private float x1;
    private float y1;
    private float x2;
    private float y2;
    private float width;
    private float height;
    private float score;

    public Region( float x1, float y1, float x2, float y2, float width, float height, float score ){
        this.setX1( x1 );
        this.setY1( y1 );
        this.setX2( x2 );
        this.setY2( y2 );
        this.setWidth( width );
        this.setHeight( height );
        this.setScore( score );
    }

    public Region( float x1, float y1, float x2, float y2, float score ){
        this( x1, y1, x2, y2, x2 - x1, y2 - y1, score );
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}

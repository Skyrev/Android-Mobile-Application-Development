package com.cs646.skyrev.circollisioncourse;

/**
 * Created by skyrev on 2/25/18.
 */

public class Circle {
    private float centreX;
    private float centreY;
    private float radius;
    private float veloX;
    private float veloY;

    public Circle(float centreX, float centreY) {
        this.centreX = centreX;
        this.centreY = centreY;
        this.radius = 20.0f;
        this.veloX = 0.0f;
        this.veloY = 0.0f;
    }

    public float getCentreX() {
        return centreX;
    }

    public void setCentreX(float centreX) {
        this.centreX = centreX;
    }

    public float getCentreY() {
        return centreY;
    }

    public void setCentreY(float centreY) {
        this.centreY = centreY;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
    public float getVeloX() {
        return veloX;
    }

    public void setVeloX(float veloX) {
        this.veloX = veloX;
    }

    public float getVeloY() {
        return veloY;
    }

    public void setVeloY(float veloY) {
        this.veloY = veloY;
    }
}

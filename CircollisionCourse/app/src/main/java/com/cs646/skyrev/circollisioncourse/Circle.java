package com.cs646.skyrev.circollisioncourse;

/**
 * Created by skyrev on 2/25/18.
 */

public class Circle {
    private float centreX;
    private float centreY;

    private float radius;

    public Circle() {
        //
    }

    public Circle(float centreX, float centreY) {
        this.centreX = centreX;
        this.centreY = centreY;
        this.radius = 20.0f;
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
}

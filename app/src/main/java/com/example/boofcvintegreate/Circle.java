package com.example.boofcvintegreate;

public class Circle {
    private float centerX;
    private float centerY;
    private float radius;

    public Circle(float centerX, float centerY, float radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isInside(float x, float y, float scaleFactor, float canvasTranslateX, float canvasTranslateY) {
        float originalCenterX = (centerX - canvasTranslateX) / scaleFactor;
        float originalCenterY = (centerY - canvasTranslateY) / scaleFactor;

        float scaledX = (x - canvasTranslateX) / scaleFactor;
        float scaledY = (y - canvasTranslateY) / scaleFactor;

        float distance = (float) Math.sqrt(Math.pow(scaledX - originalCenterX, 2) + Math.pow(scaledY - originalCenterY, 2));

        boolean result  = distance <= radius;
        return result;
    }

}


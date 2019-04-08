package com.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

// represent the beam
// center is 0,0
// angle is counter-clockwise under radians
public class Beam {
    @JsonProperty
    int width;
    @JsonProperty
    int height;
    @JsonProperty
    double angle;

    public Beam(int width, int height) {
        this.width = width;
        this.height = height;
        this.angle = Math.PI/180;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}

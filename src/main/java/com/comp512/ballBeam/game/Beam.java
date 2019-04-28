package com.comp512.ballBeam.game;

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

    public Beam(){
        this(800,40);
    }
}

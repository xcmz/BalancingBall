package com.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ietf.jgss.Oid;

// represent the ball
public class Ball {
    @JsonProperty
     double speed;

    @JsonProperty
     double position;

    @JsonProperty
     int radius;

    public Ball(double speed, double position, int radius) {
        this.speed = speed;
        this.position = position;
        this.radius = radius;
    }

    public Ball(){
        this.speed = 1;
        this.position = 0;
        this.radius = 50;
    }
}

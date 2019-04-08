package com.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BallBeamSys {
    @JsonProperty
    Beam beam;

    @JsonProperty
    Ball ball;

    @JsonProperty
    private double gravity;

    @JsonProperty
    private double timeProceedPerFrame;

    // when the ball bound on the board, how much speed it will lose.
    // new_seed = -original_speed / factor.
    @JsonProperty
    private double speedReduceFactor;

    public double points;
    private int pointsCalcInterval;
    private int frameProceedSinceLastPoints;


    public BallBeamSys() {
        ball = new Ball();
        beam = new Beam(800, 40);
        gravity = 1;
        timeProceedPerFrame = 1;
        pointsCalcInterval = 5;
        frameProceedSinceLastPoints = 0;
        speedReduceFactor = 1;
    }

    public void nextFrame() {
        // calc point
        if (frameProceedSinceLastPoints == pointsCalcInterval){
            frameProceedSinceLastPoints=0;
            points += Math.abs(ball.speed*ball.position)/100;
        }
        frameProceedSinceLastPoints++;

        // update ball state
        double acc = -Math.sin(beam.angle) * gravity;
        ball.position += (
                ball.speed * timeProceedPerFrame +
                        acc * timeProceedPerFrame * timeProceedPerFrame / 2);
        ball.speed += (acc * timeProceedPerFrame);
        int tmp = beam.width / 2;
        if (ball.position + ball.radius > tmp) {
            ball.position = tmp - ball.radius;
            ball.speed = -ball.speed/speedReduceFactor;
            points/=2;
        } else if (ball.position - ball.radius < (-tmp)) {
            ball.position = -tmp + ball.radius;
            ball.speed = -ball.speed/speedReduceFactor;
            points/=2;
        }

    }

    void rotate(double angle){
        beam.angle+=angle;
    }

}

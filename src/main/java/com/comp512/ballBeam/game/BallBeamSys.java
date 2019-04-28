package com.comp512.ballBeam.game;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonProperty
    public double points;
    private int pointsCalcInterval;
    private int frameProceedSinceLastPoints;

    public TempGameState saveState(){
        return new TempGameState(ball.position,ball.speed, beam.angle, points);
    }

    public void restoreState(TempGameState state){
        ball.position = state.getPosition();
        ball.speed = state.getSpeed();
        beam.angle = state.getAngle();
        points = state.getPoints();
    }

    public BallBeamSys() {
        ball = new Ball();
        beam = new Beam();
        gravity = 1;
        timeProceedPerFrame = 1;
        pointsCalcInterval = 20;
        frameProceedSinceLastPoints = 0;
        speedReduceFactor = 2;
    }

    public void nextFrame() {
        // calc point
        if (frameProceedSinceLastPoints == pointsCalcInterval) {
            frameProceedSinceLastPoints = 0;
            points += Math.abs(ball.speed * ball.position) / 100;
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
            ball.speed = -ball.speed / speedReduceFactor;
            points /= 2;
        } else if (ball.position - ball.radius < (-tmp)) {
            ball.position = -tmp + ball.radius;
            ball.speed = -ball.speed / speedReduceFactor;
            points /= 2;
        }

    }

    void rotate(double angle) {
        beam.angle += angle;
    }

}

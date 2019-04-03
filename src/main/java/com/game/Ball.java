package com.game;

// represent the ball
public class Ball {

    //when the ball is moving to the right the speed is positive.
    //when the ball is moving to the left the speed is negative.
    private double speed;

    //position of the ball with respect to the beam.
    //when the ball is in the middle of the beam, pos = 0
    //when the ball is in the left-end of the beam, pos = beam.left-end
    //when the ball is in the right-end of the beam, pos = beam.right-end
    //initially, the ball is in the middle.
    private double position;

    public Ball(double speed) {
        this.speed = speed;
        position = 0;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}

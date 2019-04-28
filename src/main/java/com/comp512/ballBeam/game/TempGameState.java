package com.comp512.ballBeam.game;

public class TempGameState {
    private double position;
    private double speed;
    private double angle;

    public double getPosition() {
        return position;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAngle() {
        return angle;
    }

    public double getPoints() {
        return points;
    }

    private double points;

    public TempGameState(double position, double speed, double angle, double points) {
        this.position = position;
        this.speed = speed;
        this.angle = angle;
        this.points = points;
    }
}

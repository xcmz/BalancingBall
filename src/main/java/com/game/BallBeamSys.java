package com.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.Converter;
import org.springframework.core.serializer.Serializer;
import org.springframework.messaging.converter.MessageConverter;

public class BallBeamSys {
    private ObjectMapper mapper = new ObjectMapper();
    private Beam beam;
    private int frameCnt;
    private Ball ball;
    private double gravity;
    // how much time each frame will proceed.
    private double timeInterval;

    public BallBeamSys() {
        ball = new Ball(0);
        beam = new Beam(400, 40);
        gravity = 1;
        timeInterval = 0.1;
    }

    public static void main(String[] args) throws JsonProcessingException {
        BallBeamSys ballBeamSys = new BallBeamSys();
        System.out.println(ballBeamSys);
    }

    public Beam getBeam() {
        return beam;
    }

    public void setBeam(Beam beam) {
        this.beam = beam;
    }

    public Ball getBall() {
        return ball;
    }

    public void setBall(Ball ball) {
        this.ball = ball;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public void NextFrame() {
        double position = ball.getSpeed() * timeInterval + acc() * timeInterval * timeInterval / 2 + ball.getPosition();
        ball.setPosition(position);
        double speed = acc() * timeInterval + ball.getSpeed();
        ball.setSpeed(speed);
    }

    private boolean onBeam() {
        return Math.abs(ball.getPosition()) <= beam.width / 2;
    }

    public double acc() {
        return -Math.sin(beam.angle) * gravity;
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public int getFrameCnt() {
        return frameCnt;
    }

    public void setFrameCnt(int frameCnt) {
        this.frameCnt = frameCnt;
    }
}

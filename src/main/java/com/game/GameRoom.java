package com.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private ObjectMapper objectMapper;
    private int roomID;
    private BallBeamSys ballBeamSys;
    private SimpMessagingTemplate template;
    private int playCnt;
    public GameRoom(int roomID, ObjectMapper objectMapper, SimpMessagingTemplate template) {
        this.ballBeamSys = new BallBeamSys();
        this.objectMapper = objectMapper;
        this.template = template;
        this.roomID = roomID;
    }

    public BallBeamSys getBallBeamSys() {
        return ballBeamSys;
    }

    public void setBallBeamSys(BallBeamSys ballBeamSys) {
        this.ballBeamSys = ballBeamSys;
    }

    public boolean isFull() {
        return false;
    }

    int addPlayer(Message message) {
        playCnt += 1;
        return STATUS.EXIST.getVal();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private List<Action> actionsBuffer = new ArrayList<>();

    public void pushAction(Action action) {

        actionsBuffer.add(action);
    }

    enum STATUS {
        NOT_EXIST(0), EXIST(1);

        private int val;

        STATUS(int i) {
            this.val = i;
        }

        public int getVal() {
            return val;
        }
    }
}

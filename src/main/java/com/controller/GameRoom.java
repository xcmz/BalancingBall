package com.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.log4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.*;

public class GameRoom {
    @JsonProperty
    private BallBeamSys ballBeamSys;

    @JsonProperty
    private long expectedClientUpdateInterval;

    @JsonProperty
    private int playerCnt;

    @JsonProperty
    // how long the server will issue an sync
    private long syncInterval;

    private int roomID;

    private final List<Action> actions;

    private SimpMessagingTemplate sender;
    private ByteBuffer byteBuffer;
    private static final Logger log = Logger.getLogger(GameRoom.class);
    private final String des;
    private int syncID;

    public GameRoom(int roomID, SimpMessagingTemplate sender) {
        this.ballBeamSys = new BallBeamSys();
        this.roomID = roomID;
        this.sender = sender;
        this.byteBuffer = ByteBuffer.allocate(4 * 8 + 4);
        this.des = "/topic/room/" + roomID + "/gameUpdate/";
        syncID = 0;
        expectedClientUpdateInterval = 10;
        syncInterval = 50;
        playerCnt = 0;
        actions = new ArrayList<>();
    }

    public void stateSync() {
        // between each communication
        // the game will proceed 5 frame.
        // the interval of communication is 50ms.
        long frameProceed = syncInterval / expectedClientUpdateInterval;
        synchronized (actions){
            actions.sort(Comparator.comparingInt(o -> o.id));
            int  k = 0;
            double controlWeight = Math.PI/(360*playerCnt);
            for (int i = 0; i < frameProceed; i++) {
                while (k < actions.size() && actions.get(k).id == i){
                    ballBeamSys.rotate(actions.get(k).direction*controlWeight);
                    k++;
                }
                ballBeamSys.nextFrame();
            }
            actions.clear();
        }

        syncID++;
        sender.send(
                des,
                MessageBuilder.withPayload(generatePayload()).build());
    }


    public byte[] generatePayload() {
        byteBuffer.clear();
        byteBuffer.putDouble(ballBeamSys.ball.position);
        byteBuffer.putDouble(ballBeamSys.ball.speed);
        byteBuffer.putDouble(ballBeamSys.beam.angle);
        byteBuffer.putInt(syncID);
        byteBuffer.putDouble(ballBeamSys.points);
        return Base64.getEncoder().encode(byteBuffer.array());
    }

    public long getSyncInterval() {
        return syncInterval;
    }

    public void addPlayer(NewPlayerListener listener) {
        playerCnt++;
        listener.newPlayerIn(roomID,playerCnt);
    }

    public int getSyncID() {
        return syncID;
    }

    public void addNewAction(int clientSyncID, int frameCnt, int action) {
        if (clientSyncID != syncID){
            //discard this action
            log.debug("DISCARD ACTION");
        }else {
            actions.add(new Action(frameCnt,action));
        }

    }
}


class Action{
    int direction;
    int id;

    Action(int frameCnt, int action) {
        this.id = frameCnt;
        if (action==0){
            direction = 1;
        }else{
            direction = -1;
        }
    }
}
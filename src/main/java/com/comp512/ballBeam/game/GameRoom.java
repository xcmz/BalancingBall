package com.comp512.ballBeam.game;

import com.comp512.ballBeam.bean.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.ByteBuffer;
import java.util.*;

public class GameRoom {

    @JsonProperty
    private BallBeamSys ballBeamSys;

//************************************************************
//********** the following field is about the sync. **********
//************************************************************

    // this field represent the expected interval
    // that user update their game. i.e. the the 1 / (frame refresh rate).
    @JsonProperty
    private long expectedClientUpdateInterval;

    // how long the server will issue an sync
    // i.e. syncInterval = 1 / (tick rate)
    @JsonProperty
    private long syncInterval;

    @JsonProperty
    private int roomID;

    private int syncID;

    private Set<String> users;

    private final List<Action> actions;

    private ByteBuffer byteBuffer;
    private static final Logger logger = LoggerFactory.getLogger(GameRoom.class);
    private final String des;

    private onGameStateUpdateListener gameStateUpdateListener;

    private final GameUpdater updater;

    public GameRoom(int roomID, onGameStateUpdateListener gameStateUpdateListener) {
        this.roomID = roomID;
        this.gameStateUpdateListener = gameStateUpdateListener;

        this.ballBeamSys = new BallBeamSys();
        this.byteBuffer = ByteBuffer.allocate(4 * 8 + 4);
        this.des = "/topic/room-" + roomID + "-gameUpdate";
        syncID = 0;
        expectedClientUpdateInterval = 10;
        syncInterval = 50;
        actions = new ArrayList<>();
        users = new HashSet<>();
        this.updater = new GameUpdater(ballBeamSys.saveState());
    }

    public void addUser(String username) {
        users.add(username);
    }

    public int currentUserAmount() {
        return users.size();
    }

    public void stateSync() {
        // between each communication
        // the game will proceed 5 frame.
        // the interval of sync is 50ms.
        // expect user refresh interval is 10ms.
        long frameProceed = syncInterval / expectedClientUpdateInterval;
        synchronized (updater) {
            double controlWeight = (Math.PI/500)/users.size();
            updater.updateGame(frameProceed, ballBeamSys, controlWeight);
        }

        syncID++;
        gameStateUpdateListener.publishGameState(des, MessageBuilder.withPayload(generatePayload()).build());

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

    public int getSyncID() {
        return syncID;
    }

    public void addNewAction(int clientSyncID, int frameCnt, int action) {
        if (clientSyncID < syncID - 4) {
            logger.warn("DISCARD ACTION.\nCurrent SyncID: " + syncID + "\nClient SyncID:" + clientSyncID);
        } else {
            updater.addAction(clientSyncID, syncID, new Action(frameCnt, action));
        }
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public void removeUser(String username) {
        users.remove(username);
    }
}



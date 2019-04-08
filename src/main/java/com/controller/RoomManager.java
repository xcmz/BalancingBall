package com.controller;

import org.apache.log4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class RoomManager {
    private static final Logger log = Logger.getLogger(RoomManager.class);
    private Map<Integer, GameRoom> rooms;
    private Random random;
    SimpMessagingTemplate sender;

    public RoomManager(SimpMessagingTemplate sender) {
        this.rooms = new HashMap<>();
        random = new Random();
        this.sender = sender;
    }

    // create a game room and return  the room id.
    public int createRoom(){
        int id = generateRandomID();
        GameRoom room = new GameRoom(id, sender);
        rooms.put(id, room);
        return id;
    }

    // generate a random ID that is not used by current rooms.
    private int generateRandomID(){
        int id;
        random.setSeed(System.currentTimeMillis());
        do {
            id = random.nextInt(10000)+1;
        } while (rooms.containsKey(id));
        return id;
    }

    public GameRoom getRoom(int roomID) {
        return rooms.get(roomID);
    }
}

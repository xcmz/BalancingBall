package com.comp512.ballBeam.services;

import com.comp512.ballBeam.game.GameRoom;
import com.comp512.ballBeam.game.onGameStateUpdateListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class RoomService {
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    private Map<Integer, GameRoom> rooms;
    private Map<String , Integer> userRoomMapping;
    private Random random;
    private TaskScheduler gameStateUpdateScheduler;
    private onGameStateUpdateListener gameStateUpdateListener;
    private SimpMessagingTemplate messageSender;

    @Autowired
    public RoomService(TaskScheduler messageBrokerTaskScheduler, SimpMessagingTemplate simpMessagingTemplate) {
        this.gameStateUpdateScheduler = messageBrokerTaskScheduler;
        this.messageSender = simpMessagingTemplate;

        this.rooms = new HashMap<>();
        this.userRoomMapping = new HashMap<>();
        this.random = new Random();

        gameStateUpdateListener = (des, payload) -> messageSender.send(des, payload);

        // a default room for test
        createRoom();
    }

    public Collection<GameRoom> getCurrentRooms(){
        return rooms.values();
    }

    // create a game room and return  the room id.
    public int createRoom() {
        int id = generateRandomID();
        GameRoom room = new GameRoom(id, gameStateUpdateListener);
        rooms.put(id, room);
        StartRoom(id);
        return id;
    }

    // generate a random ID that is not used by current rooms.
    private int generateRandomID() {
        int id;
        random.setSeed(System.currentTimeMillis());
        do {
            id = random.nextInt(10000) + 1;
        } while (rooms.containsKey(id));
        return id;
    }

    public GameRoom getRoom(int roomID) {
        return rooms.get(roomID);
    }

    public boolean existRoom(int roomID) {
        return rooms.containsKey(roomID);
    }

    public boolean isUserAlreadyInARoom(String user){
        return userRoomMapping.containsKey(user);
    }

    public void addUserTo(String user, int roomID) {
        userRoomMapping.put(user,roomID);
        rooms.get(roomID).addUser(user);
    }

    public int getRoomOfUser(String username) {
        return userRoomMapping.get(username);
    }

    public void removeUser(String user) {
        if (userRoomMapping.containsKey(user)){
            int roomID = userRoomMapping.get(user);
            getRoom(roomID).removeUser(user);
            userRoomMapping.remove(user);
        }
    }

    // schedule the game update task
    private void StartRoom(int roomID){
        GameRoom room = getRoom(roomID);
        gameStateUpdateScheduler.scheduleWithFixedDelay(room::stateSync, room.getSyncInterval());
    }

    public String  queryRoomInfo(int roomID) {
        GameRoom room = getRoom(roomID);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(room);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }
}

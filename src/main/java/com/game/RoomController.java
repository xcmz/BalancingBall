package com.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
public class RoomController {
    private static final Logger log = Logger.getLogger(RoomController.class);
    private Map<Integer, GameRoom> rooms;
    private Random random;
    private SimpMessagingTemplate template;
    private ObjectMapper objectMapper;

    @Autowired
    public RoomController(SimpMessagingTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
        this.rooms = new HashMap<>();
        this.random = new Random();
    }


    @MessageMapping("/createRoom/")
    @SendToUser
    public String CreateRoom(Message message) {
        random.setSeed(System.currentTimeMillis());
        int roomID = random.nextInt(10000);
        while (rooms.containsKey(roomID))
            roomID = random.nextInt(10000);
        GameRoom room = new GameRoom(roomID, objectMapper, template);
        rooms.put(roomID, room);
        log.debug(roomID);
        return String.valueOf(roomID);
    }

    @MessageMapping("/roomState/{roomID}/")
    @SendToUser
    public String roomState(Message message, @DestinationVariable int roomID) {
        if (rooms.containsKey(roomID)) {
            return rooms.get(roomID).getBallBeamSys().toString();
        } else {
            return String.valueOf(-GameRoom.STATUS.NOT_EXIST.getVal());
        }
    }

    @MessageMapping("/joinRoom/{id}/")
    public void joinRoom(Message<String> message, @DestinationVariable int id) {
        rooms.get(id).addPlayer(message);
    }

    @MessageMapping("/room/{id}/")
    public void chat(Message<String> message, @DestinationVariable int id) {
        try {
            Action action = objectMapper.readValue(message.getPayload(), Action.class);
            rooms.get(id).pushAction(action);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

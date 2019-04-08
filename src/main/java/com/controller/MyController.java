package com.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Controller;

@Controller
public class MyController {
    private static final Logger log = Logger.getLogger(MyController.class);
    private RoomManager roomManager;
    private final SimpMessagingTemplate template;
    private MessageBuilder<String> messageBuilder;
    private ObjectMapper objectMapper;
    private TaskScheduler scheduler;

    @Autowired
    public MyController(RoomManager roomManager,
                        SimpMessagingTemplate template,
                        ObjectMapper objectMapper,
                        TaskScheduler messageBrokerTaskScheduler) {
        this.roomManager = roomManager;
        this.template = template;
        this.objectMapper = objectMapper;
        this.scheduler = messageBrokerTaskScheduler;
    }

    @MessageMapping("/createRoom/")
    @SendToUser
    public String createRoom() {
        int roomID = roomManager.createRoom();
        GameRoom room = roomManager.getRoom(roomID);
        scheduler.scheduleWithFixedDelay(room::stateSync, room.getSyncInterval());
        return String.valueOf(roomID);
    }

    @MessageMapping("/room/{roomID}/basicInfo/")
    @SendToUser
    public String basicRoomInfo(@DestinationVariable int roomID) {
        GameRoom room = roomManager.getRoom(roomID);
        try {
            room.addPlayer((roomID1, playCnt) -> {
                template.convertAndSend(
                        "/topic/room/" + roomID1 + "/newPlayerNotification/",
                        playCnt
                );
            });
            return objectMapper.writeValueAsString(room);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    @MessageMapping("/room/{roomID}/action/")
    public void routeAction(
            @Header int syncID,
            @Header int frameCnt,
            @DestinationVariable int roomID,
            @Payload int action) {
        GameRoom room = roomManager.getRoom(roomID);
        room.addNewAction(syncID, frameCnt, action);
    }
}


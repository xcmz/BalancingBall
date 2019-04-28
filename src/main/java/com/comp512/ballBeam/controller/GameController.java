package com.comp512.ballBeam.controller;

import com.comp512.ballBeam.bean.User;
import com.comp512.ballBeam.game.GameRoom;
import com.comp512.ballBeam.services.RoomService;
import com.comp512.ballBeam.services.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final UserServices userServices;
    private final RoomService roomService;

    @Autowired
    public GameController(UserServices userServices, RoomService roomService) {
        this.userServices = userServices;
        this.roomService = roomService;
    }

    @RequestMapping("/rooms")
    public ModelAndView toRoomCenter(ModelAndView mv, HttpServletRequest request) {
        mv.setViewName("rooms");
        User user = userServices.getUserByUsername(request.getRemoteUser());
        mv.addObject("user", user);
        mv.addObject("rooms", roomService.getCurrentRooms());
        return mv;
    }

    @PostMapping("/createRoom")
    public ModelAndView createRoom(ModelAndView mv) {
        int roomID = roomService.createRoom();
        mv.setViewName("forward:/rooms");
        mv.addObject("createdRoomID", roomID);
        return mv;
    }

    @GetMapping("/joinRoom/{roomID}")
    public ModelAndView joinRoom(@PathVariable int roomID, ModelAndView mv, HttpServletRequest request) {
        if (!roomService.existRoom(roomID)) {
            mv.setViewName("forward:/rooms");
            mv.addObject("tips", "the room " + roomID + " is not exist.");
        } else {
            GameRoom room = roomService.getRoom(roomID);
            String username = request.getRemoteUser();
            if (roomService.isUserAlreadyInARoom(username)) {
                int roomOfUser = roomService.getRoomOfUser(username);
                if (roomOfUser == roomID) {
                    mv.setViewName("forward:/game/" + roomID);
                } else {
                    mv.setViewName("forward:/rooms");
                    mv.addObject("tips", "you are already in the room " + roomOfUser);
                }
            } else {
                roomService.addUserTo(request.getRemoteUser(), roomID);
                mv.setViewName("forward:/game/"+roomID);
            }
        }
        return mv;
    }

    @PostMapping("/leaveRoom")
    public String leaveRoom(HttpServletRequest request, @RequestParam float points){
        float point = userServices.getUserByUsername(request.getRemoteUser()).getHighestPoint();
        point = point < points ? points : point;
        userServices.updateUserPoints(request.getRemoteUser(), point);
        roomService.removeUser(request.getRemoteUser());
        return "forward:/rooms";
    }

    @PostMapping("/roomInfo/{roomID}")
    @ResponseBody
    public String queryRoomInfo(@PathVariable int roomID){
        return roomService.queryRoomInfo(roomID);
    }

    @RequestMapping("/game/{roomID}")
    public ModelAndView toGame(HttpServletRequest request, @PathVariable int roomID) {
        ModelAndView mv = new ModelAndView("game");
        User user = userServices.getUserByUsername(request.getRemoteUser());
        mv.addObject("roomID", roomID);
        mv.addObject("user", user);
        mv.addObject("sockEntryPath", "/sockEntry");
        mv.addObject("roomInfo",roomService.queryRoomInfo(roomID));
        return mv;
    }

    @MessageMapping("/room/{roomID}/action/")
    public void routeAction(
            @Header int syncID,
            @Header int frameCnt,
            @DestinationVariable int roomID,
            @Payload int action) {
        GameRoom room = roomService.getRoom(roomID);
        room.addNewAction(syncID, frameCnt, action);
    }
}

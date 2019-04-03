package com.controller;

import com.bean.HelloCounter;
import org.apache.log4j.Logger;
import org.apache.zookeeper.server.quorum.QuorumCnxManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {
    private static final Logger log = Logger.getLogger(GameController.class);

    @MessageMapping("/test")
    @SendToUser
    public String sss(@Payload String s){
        log.debug(s);
        return "received.";
    }
}

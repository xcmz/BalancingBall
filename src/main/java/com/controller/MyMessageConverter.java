package com.controller;

import com.game.GameRoom;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;

public class MyMessageConverter implements MessageConverter{
    @Override
    public Object fromMessage(Message<?> message, Class<?> aClass) {
        return null;
    }

    public void test(Message<?> message){

    }
    @Override
    public Message<?> toMessage(Object o, MessageHeaders messageHeaders) {
        return null;
    }
}

package com.comp512.ballBeam.game;

import org.springframework.messaging.Message;

public interface onGameStateUpdateListener {
    void publishGameState(String des, Message<byte[]> payload);
}

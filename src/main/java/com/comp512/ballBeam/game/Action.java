package com.comp512.ballBeam.game;

class Action{
    int direction;
    int frameID;

    Action(int frameCnt, int action) {
        this.frameID = frameCnt;
        this.direction = action;
    }
}
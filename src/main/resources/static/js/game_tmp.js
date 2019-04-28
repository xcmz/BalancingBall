// basic things for drawing
var canvas;
var ctx;
var localUpdateTaskID;
var frameCnt;

$(document).ready(function () {
    gameInit();
    game.initGameData(roomInfo);
    window.onkeydown = onControlKeyPressedEvent;
    startListenGameUpdate();
    startLocalUpdate();
});

var game = {
    gravity: 0,

    // how many time proceed per frame.
    // this time is respect to the game world time
    dt: 0,

    // the interval that user update their game.
    // i.e.  the FPS = 1 / (frame refresh rate).
    refreshInterval: 9999,

    syncInterval:9999,

    speedReduceFactor:999,

    controlWeight:Math.PI/360,

    beam: {
        x: 0,
        y: 0,
        width: 0,
        height: 0,
        angle: 0,

        update: function () {
            ctx.save();
            ctx.rotate(this.angle);
            ctx.strokeRect(this.x, this.y, this.width, this.height);
            ctx.strokeRect(this.x - 21, this.y, 20, 100);
            ctx.strokeRect(-this.x + 1, this.y, 20, 100);
            ctx.restore();
        },
        leftX: function () {
            return this.x;
        },
        rightX: function () {
            return this.x + this.width;
        }
    },

    ball: {
        cx: 0,
        cy: 0,
        r: 0,
        speed: 0,
        update: function () {
            var angle = game.beam.angle;
            var t = game.dt;
            var a = -Math.sin(angle) * game.gravity;
            this.cx += this.speed * t + a * t * t / 2;
            this.speed += a * t;
            if (this.cx + this.r > game.beam.rightX()) {
                this.cx = game.beam.rightX() - this.r;
                this.speed = -this.speed/game.speedReduceFactor;
            } else if (this.cx - this.r < game.beam.leftX()) {
                this.cx = game.beam.leftX() + this.r;
                this.speed = -this.speed/game.speedReduceFactor;
            }

            ctx.save();
            ctx.rotate(angle);
            ctx.arc(this.cx, this.cy, this.r, 0, 2 * Math.PI);
            ctx.restore();
        }
    },

    initGameData: function (data) {
        this.refreshInterval = data.expectedClientUpdateInterval;
        this.syncInterval = data.syncInterval;

        this.dt = data.ballBeamSys.timeProceedPerFrame;
        this.gravity = data.ballBeamSys.gravity;

        this.beam.width = data.ballBeamSys.beam.width;
        this.beam.height = data.ballBeamSys.beam.height;
        this.beam.angle = data.ballBeamSys.beam.angle;
        this.beam.x = -this.beam.width/2;
        this.beam.y = -this.beam.height/2;

        this.ball.r = data.ballBeamSys.ball.radius;
        this.ball.cx = data.ballBeamSys.ball.position;
        this.ball.speed = data.ballBeamSys.ball.speed;
        this.ball.cy = this.beam.height/2 + this.ball.r;
        this.controlWeight =  Math.PI/500*0.8;
        this.speedReduceFactor = data.ballBeamSys.speedReduceFactor;

    },

    update: function () {
        ctx.beginPath();
        this.beam.update();
        this.ball.update();
        ctx.stroke();
    },


};

function gameInit() {
    canvas = document.getElementById("mainCanvas");
    ctx = canvas.getContext("2d");
    canvas.cx = canvas.width / 2;
    canvas.cy = canvas.height / 2;
    ctx.transform(1, 0, 0, -1, canvas.cx, canvas.cy);
}

function onControlKeyPressedEvent(ev) {
    if (!ev.repeat) {
        var currentFrame = frameCnt;
        if (ev.code === "KeyA" || ev.code === "ArrowLeft") {
            stompClient.send("/game/room/"+roomInfo.roomID+"/action/",{syncID:syncID,frameCnt:currentFrame},"1");
            game.beam.angle += game.controlWeight;
        } else if (ev.code === "KeyD" || ev.code === "ArrowRight") {
            stompClient.send("/game/room/"+roomInfo.roomID+"/action/",{syncID:syncID,frameCnt:currentFrame},"-1");
            game.beam.angle -= game.controlWeight;
        }
        log("Action frame cnt: " + frameCnt);
        if (frameCnt === 6) {
            log("!!! DISCARD ACTION");
        }
    }
}

function clearCanvas() {
    // Store the current transformation matrix

    // Use the identity matrix while clearing the canvas
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.transform(1, 0, 0, -1, canvas.cx, canvas.cy);
    // Restore the transform
    ctx.restore();
}

function startLocalUpdate() {
    localUpdateTaskID = setInterval(localUpdate, game.refreshInterval);
}

function localUpdate() {
    clearCanvas();
    game.update();
    frameCnt++;
}

function stopLocalUpdate() {
    clearInterval(localUpdateTaskID);
}
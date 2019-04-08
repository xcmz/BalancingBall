var canvas;
var ctx;
var frameCnt = 0;
var stompClient;
var connectUrl = "http://localhost:8080/sockEntry/connect";
var roomID;
// a simulated enum obj
var ROOM_STATUS = {
    NOT_EXIST: 0,
    EXIST: 1
};
$(document).ready(function () {
    gameInit();
    stompClient = connectTo(connectUrl);
    controllerRegister();
});

function gameInit() {
    canvas = document.getElementById("mainCanvas");
    ctx = canvas.getContext("2d");
    canvas.cx = canvas.width / 2;
    canvas.cy = canvas.height / 2;
    ctx.transform(1, 0, 0, -1, canvas.cx, canvas.cy);
}

function controllerRegister() {
    window.onkeydown = onKeyPressedEvent;
}

function connectTo(url) {
    var client;
    var socketConn = new SockJS(url);
    client = Stomp.over(socketConn);
    client.connect({}, function (msg) {
        log("connect success\nsubscribe basic things");
    });
    return client;
}

function createRoom() {
    stompClient.subscribe(
        "/user/queue/createRoom/",
        function (msg) {
            joinRoom(msg.body);
        });
    stompClient.send("/game/createRoom/", {}, "")
}

var gameUpdateSub = null;
var newPlayerNotifySub = null;
var taskID;
var syncID;



function joinRoom(roomID) {
    if (gameUpdateSub != null) {
        gameUpdateSub.unsubscribe();
        clearInterval(taskID);
    }
    window.roomID = roomID;
    initAndJoin(roomID);
}

function initAndJoin(roomID) {
    var roomInfoSub = stompClient.subscribe("/user/queue/room/" + roomID + "/basicInfo/", function (msg) {
        var state = JSON.parse(msg.body);
        console.log(state);
        game.initData(state);
        taskID = setInterval(simpleUpdate, game.expectedClientUpdateInterval);
        roomInfoSub.unsubscribe();
        newPlayerNotifySub = stompClient.subscribe("/topic/room/"+roomID+"/newPlayerNotification/", onNewPlayerJoinNotify);
        gameUpdateSub = stompClient.subscribe("/topic/room/" + roomID + "/gameUpdate/", onGameUpdateReply)
    });
    stompClient.send("/game/room/" + roomID + "/basicInfo/")
}

function onNewPlayerJoinNotify(msg) {
    log("current user : " + msg.body);
    game.playerCnt = parseInt(msg.body);
    game.updateControlWeight();
}

function onGameUpdateReply(msg) {
    var data = decodeUpdateReply(msg.body);
    log(data);
    game.ball.cx = data[0];
    game.ball.speed = data[1];
    game.beam.angle = data[2];
    syncID = data[3];
    $("#points").text("Points: "+data[4]);
    frameCnt=0;
}

function decodeUpdateReply(s) {
    var bytes = toArrayBuffer(atob(s));
    var view = new DataView(bytes);
    // [position, speed, angle, syncID, points]
    // [       d,     d,     d,      i,      d]
    return [
        view.getFloat64(0, false),
        view.getFloat64(8, false),
        view.getFloat64(16, false),
        view.getInt32(24, false),
        view.getFloat64(28,false)
    ]
}

function toArrayBuffer(s) {
    return Uint8Array.from(s, function (v) {
        return v.charCodeAt(0)
    }).buffer;
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

function simpleUpdate() {
    clearCanvas();
    game.update();
}

function log(s) {
    console.log("***********\n" + s + "\n***********");
}

function onKeyPressedEvent(ev) {
    if (!ev.repeat) {
        frameCnt++;
        if (ev.code === "KeyA" || ev.code === "ArrowLeft") {
            stompClient.send("/game/room/"+roomID+"/action/",{syncID:syncID,frameCnt:frameCnt},"0");
            game.beam.angle += game.controlWeight;
        } else if (ev.code === "KeyD" || ev.code === "ArrowRight") {
            stompClient.send("/game/room/"+roomID+"/action/",{syncID:syncID,frameCnt:frameCnt},"1");
            game.beam.angle -= game.controlWeight;
        }
    }
}
var game = {
    gravity: 0,
    timeProceedPerFrame: 0,
    expectedClientUpdateInterval: 9999,
    syncInterval:9999,
    speedReduceFactor:999,
    controlWeight:Math.PI/360,
    playerCnt:0,
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
            var t = game.timeProceedPerFrame;
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

    initData: function (data) {
        this.expectedClientUpdateInterval = data.expectedClientUpdateInterval;
        this.syncInterval = data.syncInterval;

        this.timeProceedPerFrame = data.ballBeamSys.timeProceedPerFrame;
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

        this.speedReduceFactor = data.ballBeamSys.speedReduceFactor;
        this.playerCnt = data.playerCnt;
        game.updateControlWeight();
    },

    update: function () {
        ctx.beginPath();
        this.beam.update();
        this.ball.update();
        ctx.stroke();
    },

    updateControlWeight:function () {
        this.controlWeight = Math.PI/(360*this.playerCnt);
    }
};


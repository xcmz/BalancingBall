var canvas;
var ctx;
var intervalID;
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
    canvas = document.getElementById("mainCanvas");
    ctx = canvas.getContext("2d");
    canvas.cx = canvas.width / 2;
    canvas.cy = canvas.height / 2;
    coordinatesToCenter();
    stompClient = connectTo(connectUrl);
    setInterval(FPSRateDisplay, 1000);
});

// user:   listen on /user/queue/createRoom/
// server: handled by simpBroker
// user:   send msg to /game/createRoom
// server: captured by annotated method
// server: server do thing.
// server: server replay to directly to user(@SendToUser)
// server: des is: /user/queue/createRoom/ (consistent with user listening)
// u: unsub previous sub.
// u: join the room.
function createRoom() {
    var sub = stompClient.subscribe("/user/queue/createRoom/", function (msg) {
        console.log("created room ID: " + msg.body);
        joinRoom(msg.body);
        sub.unsubscribe();
    });
    stompClient.send("/game/createRoom/");
}

// u: listen on: /user/queue/roomState/{id}/
// u: send msg to check whether the room is available
// s: captured by @method,
// s: check room states
// s: return a value directly to the user:
// s: des: /user/queue/roomState/{id}
// u: not exist: stop
// u: exist: subscribe on /topic/room/{id}/
// s: handle by simpBroker.
function joinRoom(roomID) {
    var sub = stompClient.subscribe("/user/queue/roomState/{id}/",
        function (msg) {
            var states = JSON.parse(msg.body);
            if (states === ROOM_STATUS.NOT_EXIST) {
                console.log("Room " + roomID + " not exist.\n");
            } else {
                stompClient.send("/game/joinRoom/" + roomID + "/");
                window.roomID = roomID;
                console.log("Room " + roomID + " exist.\n");
                console.log("Adjust to the server");

                game.adjust(states);
                start(states);
                stompClient.subscribe("/topic/room/" + roomID + "/",
                    function (msg) {
                        game.adjust(msg);
                        start();
                    });
            }
            sub.unsubscribe();
        });
    stompClient.send("/game/roomState/" + roomID + "/");
}

var actions = [];

function controllerRegester() {
    window.onkeydown = onKeyPressedEvent;
}

function connectTo(url) {
    var client;
    var socketConn = new SockJS(url);
    client = Stomp.over(socketConn);
    client.connect({}, function (frame) {
        console.log("connect " + url + " success.\n");
    });
    return client;
}

function FPSRateDisplay() {
    $("#fps").text("FPS Rate:" + frameCnt);
    frameCnt = 0;
}

function coordinatesToCenter() {
    ctx.transform(1, 0, 0, -1, canvas.cx, canvas.cy);
}

function clearCanvas() {
    // Store the current transformation matrix

    // Use the identity matrix while clearing the canvas
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    coordinatesToCenter();
    // Restore the transform
    ctx.restore();
}

function update() {
    clearCanvas();
    game.update();
    if (frameProceeded === syncFrameInterval) {
        cancelUpdate();

    }
}


function onKeyPressedEvent(ev) {
    if (!ev.repeat) {
        if (ev.code === "KeyA" || ev.code === "ArrowLeft") {
            game.beam.angle += 0.01;
        } else if (ev.code === "KeyD" || ev.code === "ArrowRight") {
            game.beam.angle -= 0.01;
        }
    }
}

var syncFrameInterval = 10;
var frameProceeded = 0;
var updateTaskID;

function start(gameStatus) {
    game.adjust(gameStatus);
    frameProceeded = 0;
    updateTaskID = setInterval(update, 10);
}

function cancelUpdate() {
    clearInterval(updateTaskID);
}

var game = {
    pressedKeyCode: false,
    gravity: 0,
    frameCnt: 0,
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
        r: 50,
        speedX: 0,
        speedY: 0,
        dropped: false,
        update: function () {
            if (this.isOnBeam()) {
                ctx.save();
                var angle = game.beam.angle;
                ctx.rotate(angle);
                this.cx += this.speedX;
                this.speedX += Math.sin(-angle) * game.gravity;
                ctx.arc(this.cx, this.cy, this.r, 0, 2 * Math.PI);
                ctx.restore();
            } else {
                if (!this.dropped) {
                    // first time out the beam
                    // translate cx,cy(not the coordinates system)
                    var cosine = Math.cos(game.beam.angle);
                    var sine = Math.sin(game.beam.angle);
                    var nx = cosine * this.cx - sine * this.cy;
                    var ny = sine * this.cx + cosine * this.cy;
                    this.cx = nx;
                    this.cy = ny;
                    this.dropped = true;
                    this.speedY = sine * this.speedX;
                    this.speedX = cosine * this.speedX;
                }
                ctx.arc(this.cx, this.cy, this.r, 0, 2 * Math.PI);
                this.cx += this.speedX;
                this.cy += this.speedY;
                this.speedY -= this.gravity;
            }
        },

        // judge whether current the ball is on beam
        isOnBeam: function () {
            return this.cx > game.beam.leftX() - this.r / 2 && this.cx < game.beam.rightX() + this.r / 2;
        }
    },

    update: function () {
        ctx.beginPath();
        this.beam.update();
        this.ball.update();
        // this.coordinates.update();
        ctx.stroke();
        this.frameCnt += 1;
        if (frameProceeded === syncFrameInterval) {
            frameProceeded = 0;
            cancelUpdate();
        }
    },

    adjust: function (states) {
        this.gravity = states.gravity;
        this.frameCnt = states.frameCnt;

        this.beam.width = states.beam.width;
        this.beam.height = states.beam.height;
        this.beam.angle = states.beam.angle;
        this.beam.x = -this.beam.width / 2;
        this.beam.y = -this.beam.height / 2;

        this.ball.speedX = states.ball.speed;
        this.ball.cx = states.ball.position;
        this.ball.cy = this.beam.height / 2 + this.ball.r;
        clearCanvas();
        game.update();
    }
};


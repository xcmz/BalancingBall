var stompClient = null;

function connectURL(url) {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    var ws = new SockJS(url);
    stompClient = Stomp.over(ws);
    stompClient.connect({},on_connect, on_error())
}
var gameUpdateSub;
var on_connect = function (x) {
    log("Stomp client success");
    stompClient.debug = null;
    gameUpdateSub = stompClient.subscribe("/topic/room-"+roomInfo.roomID+"-gameUpdate", onGameUpdateListener)
};

var on_error = function(x) {
    log("Stomp connect error!!!!!!");
};

function log(s) {
    console.log("***********\n" + s + "\n***********");
}


function onGameUpdateListener(msg) {
    var data = decodeUpdateReply(msg.body);
    log(data);
    game.ball.cx = data[0];
    game.ball.speed = data[1];
    game.beam.angle = data[2];
    syncID = data[3];
    if (data[4] > highestPoints) {
        highestPoints = data[4]
    }
    $("#points").text("Points: "+data[4]);
    log("frame passed since last sync: " + frameCnt);
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

function startListenGameUpdate() {
    connectURL(webSocketConnectPath);
}

function stopReceiveGameUpdate() {
    gameUpdateSub.unsubscribe();
    stompClient.disconnect();
}
var canvas;
var ctx;
var intervalID;
$(document).ready(function () {
    canvas = document.getElementById("mainCanvas");
    ctx = canvas.getContext("2d");
    canvas.cx = canvas.width / 2;
    canvas.cy = canvas.height / 2;
    coordinatesToCenter();
    window.addEventListener("keypress", function (e) {
        console.log(e.key);
        if (e.key == "a") {
            game.beam.angle += 0.01;
        } else if (e.key == "d") {
            game.beam.angle -= 0.01;
        }
    });
    intervalID = setInterval(update, 8);
});

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
}

var game = {
    pressedKeyCode: false,
    beam: {
        x: -200,
        y: -20,
        width: 400,
        height: 40,
        angle: 0,
        update: function () {
            ctx.save();
            ctx.rotate(this.angle);
            ctx.strokeRect(this.x, this.y, this.width, this.height);
            ctx.restore();

            if (game.pressedKeyCode && game.pressedKeyCode == "ArrowLeft") {
                this.angle += 0.1;
            }
            if (game.pressedKeyCode && game.pressedKeyCode == "ArrowRight") {
                this.angle -= 0.1;
            }
        }
    },

    ball: {
        cx: 0,
        cy: 70,
        r: 50,
        speed: 0.5,
        gravity: 0.5,
        update: function () {
            ctx.save();
            var angle = game.beam.angle;
            ctx.rotate(angle);
            this.cx += this.speed;
            this.speed += Math.sin(-angle) * this.gravity;
            ctx.arc(this.cx, this.cy, this.r, 0, 2 * Math.PI);
            ctx.restore();
        }
    },

    coordinates: {
        update: function () {
            ctx.moveTo(0, 0);
            ctx.lineTo(0, 200);
            ctx.moveTo(0, 0);
            ctx.lineTo(200, 0);
        }
    },
    update: function () {
        ctx.beginPath();
        this.beam.update();
        this.ball.update();
        this.coordinates.update();
        ctx.stroke();
    }
};


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hello 512</title>
    <script src="../../js/jquery-3.3.1.min.js"></script>
    <script src="../../js/sockjs.min.js"></script>
    <script src="../../js/game.js"></script>
</head>
<body>
<h1>Ball and Beam</h1>
<h1>${sid}</h1>
<h3 id="left">left :0</h3>
<h3 id="right">right:0</h3>
<h1 id="fps">FPS:</h1>
<canvas id="mainCanvas" width="1000" height="690" style="border:2px solid #000000;">

</canvas>
</body>

</html>
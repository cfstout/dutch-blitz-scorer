<!DOCTYPE html>
<html>
<head>
    <title>Start New Game</title>

    <script type="text/javascript">
        function updatePlayerFields() {
            var playerCount = document.getElementById('playerCount').value;
            var container = document.getElementById('playerNamesContainer');
            container.innerHTML = '';
            for (var i = 1; i <= playerCount; i++) {
                container.innerHTML += '<label for="playerName' + i + '">Player ' + i + ' Name:</label>' +
                    '<input type="text" id="playerName' + i + '" name="playerName' + i + '"><br><br>';
            }
        }
    </script>
</head>
<body>
<h1>Start a New Game</h1>
<form action="/setup-game" method="post">
    <label for="playerCount">Number of Players:</label>
    <input type="number" id="playerCount" name="playerCount" min="2" max="4" oninput="updatePlayerFields()"><br><br>

    <label for="scoreToWin">Score to Win:</label>
    <input type="number" id="scoreToWin" name="scoreToWin" min="1"><br><br>

    <div id="playerNamesContainer">
        <!-- Player name fields will be added here by JavaScript -->
    </div>

    <input type="submit" value="Start Game">
</form>
</body>
</html>

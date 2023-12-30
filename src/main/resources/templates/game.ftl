<!DOCTYPE html>
<html>
<head>
    <title>Game Round</title>
    <!-- Add any additional head elements here -->
</head>
<body>
<h1>Game Round</h1>

<!-- Displaying rounds -->
<#list rounds as round>
    <div>
        <h2>Round ${round.index}</h2>
        <#list round.scores as score>
            <p>${score.playerName}: ${score.points}</p>
        </#list>
        <!-- Add edit round button/link here if needed -->
    </div>
</#list>

<!-- Add new round form -->
<form action="/add-round" method="post">
    <#list players as player>
        <label for="blitz${player.id}">Blitz cards for ${player.name}:</label>
        <input type="number" id="blitz${player.id}" name="blitz${player.id}"><br>

        <label for="points${player.id}">Points for ${player.name}:</label>
        <input type="number" id="points${player.id}" name="points${player.id}"><br><br>
    </#list>
    <input type="submit" value="Add Round">
</form>

<!-- Check if game is over and show start new game button -->
<#if gameIsOver>
    <form action="/start-new-game" method="get">
        <input type="submit" value="Start New Game">
    </form>
</#if>
</body>
</html>

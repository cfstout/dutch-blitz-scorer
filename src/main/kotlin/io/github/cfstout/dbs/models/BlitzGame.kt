package io.github.cfstout.dbs.models

class BlitzGame(
    val pointsToWin: Int,
    val numberOfPlayers: Int,
) {
    private val playerNames: Map<Int, String>
    private val totalScores: MutableMap<Int, Int> =
        (1..numberOfPlayers)
            .associateWith { 0 }
            .toMutableMap()

    init {
        playerNames = promptForPlayerNames()
    }

    fun isGameOver(): Boolean {
        return totalScores.values.any { it >= pointsToWin }
    }

    fun playRound() {
        val blitzes = promptForBlitzes()
        val points = promptForPoints()
        val roundScores = calculateScoreThisRound(blitzes, points)
        for (i in 1..numberOfPlayers) {
            totalScores[i] = totalScores[i]!! + roundScores[i]!!
        }
        printRoundDetails(blitzes, points, roundScores)
    }

    fun printWinners() {
        val winners = totalScores.filter { it.value >= pointsToWin }
        if (winners.size == 1) {
            val winner = winners.entries.first().key
            println("${playerNames[winner]} wins!")
        } else {
            println("It's a tie!")
            winners.forEach { println("${playerNames[it.key]}: ${totalScores[it.key]}") }
        }
    }

    private fun promptForPlayerNames(): Map<Int, String> {
        val playerNameInput = mutableMapOf<Int, String>()
        for (i in 1..numberOfPlayers) {
            println("Enter name for player $i")
            playerNameInput[i] = readln()
        }
        return playerNameInput
    }

    private fun promptForBlitzes(): Map<Int, Int> {
        val blitzes = mutableMapOf<Int, Int>()
        for (i in 1..numberOfPlayers) {
            println("How many in the blitz pile for ${playerNames[i]}?")
            blitzes[i] = readln().toInt()
        }
        return blitzes
    }

    private fun promptForPoints(): Map<Int, Int> {
        val scores = mutableMapOf<Int, Int>()
        for (i in 1..numberOfPlayers) {
            println("How many points did ${playerNames[i]} get?")
            scores[i] = readln().toInt()
        }
        return scores
    }

    private fun calculateScoreThisRound(
        blitzes: Map<Int, Int>,
        points: Map<Int, Int>,
    ): Map<Int, Int> {
        val scores = mutableMapOf<Int, Int>()
        for (i in 1..numberOfPlayers) {
            scores[i] = points[i]!! - (blitzes[i]!! * 2)
        }
        return scores
    }

    private fun printRoundDetails(
        blitzes: Map<Int, Int>,
        points: Map<Int, Int>,
        roundScores: Map<Int, Int>,
    ) {
        for (i in 1..numberOfPlayers) {
            println(
                "${playerNames[i]} got ${points[i]} points and had ${blitzes[i]} in the blitz pile giving them a " +
                        "score this round of ${roundScores[i]}. Total: ${totalScores[i]}",
            )
        }
    }
}

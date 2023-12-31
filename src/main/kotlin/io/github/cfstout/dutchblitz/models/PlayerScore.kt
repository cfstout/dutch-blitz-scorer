package io.github.cfstout.dutchblitz.models

data class PlayerScore(
    val playerNumber: Int,
    val blitzCardsRemaining: Int,
    val pointCards: Int,
) {
    val score = pointCards - (2 * blitzCardsRemaining)
}

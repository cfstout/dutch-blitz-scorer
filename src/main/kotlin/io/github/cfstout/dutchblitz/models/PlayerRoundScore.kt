package io.github.cfstout.dutchblitz.models

import java.util.UUID

data class PlayerRoundScore(
    val gameId: UUID,
    val roundNumber: Int,
    val playerScore: PlayerScore,
)

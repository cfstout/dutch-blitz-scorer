package io.github.cfstout.dutchblitz.dao

import io.github.cfstout.dutchblitz.models.PlayerRoundScore
import io.github.cfstout.dutchblitz.models.PlayerScore
import io.github.cfstout.ktor.jooq.Tables.ROUND_SCORES
import io.github.cfstout.ktor.jooq.tables.records.RoundScoresRecord
import org.jooq.DSLContext
import java.util.UUID

interface RoundScoresDao {
    fun getScoresForGame(gameId: UUID): List<PlayerRoundScore>

    fun storeRound(
        gameId: UUID,
        roundNumber: Int,
        playerScores: List<PlayerScore>,
    )
}

class SqlRoundScoreDao(private val txnContext: DSLContext) : RoundScoresDao {
    override fun getScoresForGame(gameId: UUID): List<PlayerRoundScore> {
        return txnContext.selectFrom(ROUND_SCORES)
            .where(ROUND_SCORES.GAME_ID.eq(gameId.toString()))
            .fetch()
            .map { it.toPlayerRoundScore() }
    }

    override fun storeRound(
        gameId: UUID,
        roundNumber: Int,
        playerScores: List<PlayerScore>,
    ) {
        txnContext.batch(
            playerScores.map {
                txnContext.insertInto(ROUND_SCORES)
                    .set(ROUND_SCORES.GAME_ID, gameId.toString())
                    .set(ROUND_SCORES.ROUND_NUMBER, roundNumber.toShort())
                    .set(ROUND_SCORES.PLAYER_NUMBER, it.playerNumber.toShort())
                    .set(ROUND_SCORES.BLITZ_CARDS_REMAINING, it.blitzCardsRemaining.toShort())
                    .set(ROUND_SCORES.POINT_CARDS, it.pointCards.toShort())
            },
        ).execute()
    }

    companion object {
        private fun RoundScoresRecord.toPlayerRoundScore() =
            PlayerRoundScore(
                UUID.fromString(this.gameId),
                this.roundNumber.toInt(),
                PlayerScore(
                    this.playerNumber.toInt(),
                    this.blitzCardsRemaining.toInt(),
                    this.pointCards.toInt(),
                ),
            )
    }
}

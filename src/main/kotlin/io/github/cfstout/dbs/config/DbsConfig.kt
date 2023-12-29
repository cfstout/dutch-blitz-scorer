package io.github.cfstout.dbs.config

import com.natpryce.konfig.Configuration
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType

class DbsConfig private constructor(
    val pointsToWin: Int,
    val numberOfPlayers: Int,
) {
    companion object {
        fun fromConfig(config: Configuration): DbsConfig {
            return DbsConfig(
                pointsToWin = config[Keys.pointsToWin],
                numberOfPlayers = config[Keys.numberOfPlayers],
            )
        }
    }

    object Keys {
        val pointsToWin = Key("POINTS_TO_WIN", intType)
        val numberOfPlayers = Key("NUMBER_PLAYERS", intType)
    }
}

package io.github.cfstout.dbs

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.overriding
import io.github.cfstout.dbs.config.DbsConfig
import io.github.cfstout.dbs.config.fromDirectory
import io.github.cfstout.dbs.models.BlitzGame
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

object DutchBlitzScorer {
    private val logger = LoggerFactory.getLogger(DutchBlitzScorer::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val warmupPool = Executors.newCachedThreadPool(DaemonThreadFactory)
        val configDir =
            Path.of(
                args.getOrNull(0)
                    ?: throw IllegalArgumentException("First argument must be config dir"),
            )

        val config = EnvironmentVariables() overriding ConfigurationProperties.fromDirectory(configDir)
        val dbsConfigFuture: Future<DbsConfig> =
            warmupPool.submit(
                Callable {
                    DbsConfig.fromConfig(config)
                },
            )
        logger.info("Starting up game")
        val dbsConfig = dbsConfigFuture.get(1, TimeUnit.SECONDS)
        warmupPool.shutdown()
        val game =
            BlitzGame(
                pointsToWin = dbsConfig.pointsToWin,
                numberOfPlayers = dbsConfig.numberOfPlayers,
            )

        while (!game.isGameOver()) {
            game.playRound()
        }
        game.printWinners()
    }
}

object DaemonThreadFactory : ThreadFactory {
    private val delegate = Executors.defaultThreadFactory()

    override fun newThread(r: Runnable): Thread =
        delegate.newThread(r).apply {
            isDaemon = true
        }
}

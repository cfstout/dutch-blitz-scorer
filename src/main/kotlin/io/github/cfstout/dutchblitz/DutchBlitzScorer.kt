package io.github.cfstout.dutchblitz

import com.fasterxml.jackson.core.JsonProcessingException
import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import freemarker.cache.ClassTemplateLoader
import freemarker.template.TemplateNotFoundException
import io.github.cfstout.dutchblitz.config.DbsConfig
import io.github.cfstout.dutchblitz.config.fromDirectory
import io.github.cfstout.dutchblitz.models.BlitzGame
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.StatusPages
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.HttpMethodRouteSelector
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

object DutchBlitzScorer {
    private val logger = LoggerFactory.getLogger(DutchBlitzScorer::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val start = Instant.now()
        val warmupPool = Executors.newCachedThreadPool(DaemonThreadFactory)
        val configDir =
            Path.of(
                args.getOrNull(0)
                    ?: throw IllegalArgumentException("First argument must be config dir"),
            )

        val config = EnvironmentVariables() overriding ConfigurationProperties.fromDirectory(configDir)
//        val jooqFuture =
//            warmupPool.submit(
//                Callable {
//                    DSL.using(
//                        HikariDataSource(buildHikariConfig(config, "DB_")),
//                        SQLDialect.POSTGRES,
//                    )
//                },
//            )
        val dbsConfigFuture: Future<DbsConfig> =
            warmupPool.submit(
                Callable {
                    DbsConfig.fromConfig(config)
                },
            )
        logger.info("Starting up game")
        val dbsConfig = dbsConfigFuture.get(1, TimeUnit.SECONDS)
        val server =
            embeddedServer(Netty, port = HttpServerConfig(config).port) {
                install(CallLogging) {
                    level = Level.INFO
                }
                install(StatusPages) {
                    exception<Throwable> {
                        logger.error("Unhandled exception", it)
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                    exception<TemplateNotFoundException> {
                        logger.warn("Template ${it.templateName} not found")
                        call.respond(HttpStatusCode.NotFound, "Template ${it.templateName} not found")
                    }
                    exception<JsonProcessingException> { t ->
                        logger.warn("Bad request json", t)
                        call.respond(HttpStatusCode.BadRequest, "Invalid JSON")
                    }
                }

                install(FreeMarker) {
                    templateLoader = ClassTemplateLoader(this::class.java.classLoader, "/templates")
                }

                routing {
                    get("/") {
                        val model = mapOf("user" to "Ktor User")
                        call.respond(FreeMarkerContent("main.ftl", model))
                    }
                    get("/game/new") {
                        val model = mapOf<String, String>()
                        call.respond(FreeMarkerContent("new_game.ftl", model))
                    }
                    get("/game/id/{gameId}") {
                        val gameId = UUID.fromString(call.parameters["gameId"])
                        if (gameId == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid game id")
                        } else {
                            val model = mapOf<String, String>()
                            call.respond(FreeMarkerContent("game.ftl", model))
                        }
                    }
                }

                val root = feature(Routing)
                val allRoutes = allRoutes(root)
                val allRoutesWithMethod = allRoutes.filter { it.selector is HttpMethodRouteSelector }
                allRoutesWithMethod.forEach {
                    logger.info("route: $it")
                }
                logger.info("Startup time: ${Duration.between(start, Instant.now()).toMillis()}ms")
            }
        warmupPool.shutdown()
        // wait = true
        server.start()
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

    private fun allRoutes(root: Route): List<Route> {
        return listOf(root) + root.children.flatMap { allRoutes(it) }
    }
}

class HttpServerConfig(config: Configuration) {
    val port: Int = config[Key("HTTP_LISTEN_PORT", intType)]
}

object DaemonThreadFactory : ThreadFactory {
    private val delegate = Executors.defaultThreadFactory()

    override fun newThread(r: Runnable): Thread =
        delegate.newThread(r).apply {
            isDaemon = true
        }
}

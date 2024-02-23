plugins {
    application
    kotlin("jvm") version "1.9.10"
    id("org.jmailen.kotlinter") version "4.0.0"
    id("org.flywaydb.flyway") version "9.19.0"
    id("nu.studer.jooq") version "4.1"
}

group = "io.github.cfstout.dutchblitz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val dbUser by extra { "cfstout" }
val dbPw by extra { "password" }
val dbUrl by extra { "jdbc:postgresql://localhost:5432/cfstout" }

apply(from = "jooq.gradle")

flyway {
    url = dbUrl
    user = dbUser
    password = dbPw
    validateMigrationNaming = true
}

val deps by extra {
    mapOf(
        "flyway" to "10.0.0",
        "hikari" to "3.4.2",
        "konfig" to "1.6.10.0",
        "jackson" to "2.15.3",
        "junit" to "5.6.2",
        "ktor" to "1.6.8",
        "logback" to "1.4.11",
        "postgres" to "42.2.12",
        "slf4j" to "2.0.9"
    )
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.slf4j", "slf4j-api", deps["slf4j"])
    implementation("ch.qos.logback", "logback-classic", deps["logback"])
    implementation("com.natpryce", "konfig", deps["konfig"])
    implementation("com.zaxxer", "HikariCP", deps["hikari"])
    implementation("io.ktor", "ktor-jackson", deps["ktor"])
    implementation("io.ktor", "ktor-server-netty", deps["ktor"])
    implementation("org.jooq", "jooq")
    implementation("com.fasterxml.jackson.core", "jackson-databind", deps["jackson"])
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", deps["jackson"])
    implementation("io.ktor","ktor-freemarker", deps["ktor"])

    runtimeOnly("org.postgresql", "postgresql", deps["postgres"])

    jooqRuntime("org.postgresql", "postgresql", deps["postgres"])

    testImplementation("org.junit.jupiter", "junit-jupiter-api", deps["junit"])
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", deps["junit"])
    testImplementation("io.ktor", "ktor-server-tests", deps["ktor"])
    testImplementation("org.flywaydb", "flyway-core", deps["flyway"])
    testImplementation("org.flywaydb", "flyway-database-postgresql",deps["flyway"])
}

tasks {
    (run) {
        args = listOf("config")
        standardInput = System.`in`
    }

    test {
        useJUnitPlatform()
    }
}

// DB setup
val startDockerCompose = tasks.register("startDockerCompose", Exec::class) {
    commandLine("docker-compose", "up", "-d")
    doLast {
        println("Docker Compose up!")
    }
}

val waitForDatabase = tasks.register("waitForDatabase", Exec::class) {
    dependsOn(startDockerCompose)
    commandLine("sh", "-c", "until docker-compose exec -T postgres pg_isready ; do sleep 1; done")
    doLast {
        println("Database is ready!")
    }
}

tasks.named("generatePrimaryDbJooqSchemaSource").configure {
    dependsOn(waitForDatabase)
    dependsOn("formatKotlin")
    dependsOn("lintKotlin")
}

tasks.named("flywayMigrate").configure {
    dependsOn(waitForDatabase)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.github.cfstout.dbs.DutchBlitzScorer")
}

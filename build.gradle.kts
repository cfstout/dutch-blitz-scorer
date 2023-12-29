plugins {
    application
    kotlin("jvm") version "1.9.10"
    id("org.jmailen.kotlinter") version "4.0.0"
}

group = "io.github.cfstout.hersko-server"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val deps by extra {
    mapOf(
        "konfig" to "1.6.10.0",
        "jackson" to "2.15.3",
        "junit" to "5.6.2",
        "logback" to "1.4.11",
        "slf4j" to "2.0.9"
    )
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.slf4j", "slf4j-api", deps["slf4j"])
    implementation("ch.qos.logback", "logback-classic", deps["logback"])
    implementation("com.natpryce", "konfig", deps["konfig"])
    implementation("com.fasterxml.jackson.core", "jackson-databind", deps["jackson"])
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", deps["jackson"])

    testImplementation("org.junit.jupiter", "junit-jupiter-api", deps["junit"])
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", deps["junit"])
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

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.github.cfstout.dbs.DutchBlitzScorer")
}

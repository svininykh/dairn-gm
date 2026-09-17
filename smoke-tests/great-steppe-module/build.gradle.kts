plugins {
    kotlin("jvm") version "2.0.20"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenLocal()
    mavenCentral()
}

val rulesetVersion = providers.gradleProperty("rulesetVersion").orElse("0.1.0-SNAPSHOT")

dependencies {
    implementation("org.dairn:dairn-gm-great-steppe:${rulesetVersion.get()}")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

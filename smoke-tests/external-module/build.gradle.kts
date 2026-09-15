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

val engineVersion = providers.gradleProperty("engineVersion").orElse("0.1.0-SNAPSHOT")

dependencies {
    implementation("org.dairn:dairn-gm-engine:${engineVersion.get()}")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

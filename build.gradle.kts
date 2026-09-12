plugins {
    kotlin("jvm") version "2.0.20" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
}

allprojects {
    group = "io.github.dairn"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

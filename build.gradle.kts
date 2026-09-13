plugins {
    kotlin("jvm") version "2.0.20" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
}

allprojects {
    group = "org.dairn"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    tasks.withType<Jar>().configureEach {
        from(rootProject.file("LICENSE")) { into("META-INF") }
        from(rootProject.file("NOTICE")) { into("META-INF") }
        from(rootProject.file("CONTENT_LICENSE.md")) { into("META-INF") }
        from(rootProject.file("THIRD_PARTY_NOTICES.md")) { into("META-INF") }
    }
}

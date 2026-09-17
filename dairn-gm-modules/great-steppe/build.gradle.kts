plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-library`
    `maven-publish`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":dairn-gm-engine"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    testImplementation(kotlin("test"))
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("greatSteppe") {
            from(components["java"])
            artifactId = "dairn-gm-great-steppe"

            pom {
                name = "DAIRN GM: Great Steppe"
                description = "Great Steppe ruleset module for DAIRN GM"
                url = "https://github.com/svininykh/dairn-gm"
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "svininykh"
                        name = "Andrey Svininykh"
                    }
                }
                scm {
                    url = "https://github.com/svininykh/dairn-gm"
                    connection = "scm:git:https://github.com/svininykh/dairn-gm.git"
                    developerConnection = "scm:git:ssh://git@github.com/svininykh/dairn-gm.git"
                }
            }
        }
    }
}

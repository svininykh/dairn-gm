plugins {
    kotlin("jvm")
    `maven-publish`
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
}

dependencies {
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("engine") {
            from(components["java"])
            artifactId = "dairn-gm-engine"

            pom {
                name = "DAIRN GM Engine"
                description = "Ruleset-neutral stateless process engine for DAIRN GM modules"
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

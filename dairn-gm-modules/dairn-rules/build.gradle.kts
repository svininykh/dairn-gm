plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

kotlin { jvmToolchain(21) }
java { withSourcesJar() }

dependencies {
    api(project(":dairn-gm-engine"))
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("dairnRules") {
            from(components["java"])
            artifactId = "dairn-gm-dairn-rules"
            pom {
                name = "DAIRN GM: Common Rules"
                description = "Setting-neutral DAIRN Ability Score checks"
                url = "https://github.com/svininykh/dairn-gm"
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
            }
        }
    }
}

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":dairn-gm-engine:core"))
    testImplementation(kotlin("test"))
}

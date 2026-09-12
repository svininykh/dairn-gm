plugins {
    kotlin("jvm")
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":dairn-gm-engine:core"))
    implementation(project(":dairn-gm-engine:cairn-2e"))
    implementation(project(":dairn-gm-engine:great-steppe"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("io.github.dairn.cli.MainKt")
}

plugins {
    kotlin("jvm")
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":dairn-gm-engine"))
    implementation(project(":dairn-gm-modules:cairn-2e"))
    implementation(project(":dairn-gm-modules:great-steppe"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.dairn.cli.MainKt")
}

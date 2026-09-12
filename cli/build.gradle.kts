plugins {
    kotlin("jvm")
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":cairn-2e"))
    implementation(project(":great-steppe"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("io.github.dairn.cli.MainKt")
}


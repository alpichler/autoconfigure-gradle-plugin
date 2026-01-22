pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://artifacts.cloudflight.io/repository/plugins-maven") }
    }
}

plugins {
    // id("io.cloudflight.autoconfigure-settings") version "1.2.0"
}

rootProject.name = "autoconfigure"

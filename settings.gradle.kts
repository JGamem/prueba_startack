pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.github.johnrengelman.shadow") {
                useModule("com.github.johnrengelman:shadow:${requested.version}")
            }
        }
    }
}

rootProject.name = "kotlin-htmx"
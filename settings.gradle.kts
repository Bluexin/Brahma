rootProject.name = "Brahma"

includeBuild("kaeron") {
    dependencySubstitution {
        substitute(module("be.bluexin:kaeron")).with(project(":"))
    }
}

includeBuild("brahma-generator")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val kotlin_version: String by settings
    resolutionStrategy {
        eachPlugin {
            when (requested.id.namespace) {
                "org.jetbrains.kotlin" -> useVersion(kotlin_version)
                "org.jetbrains.kotlin.plugin" -> useVersion(kotlin_version)
            }
        }
    }
}

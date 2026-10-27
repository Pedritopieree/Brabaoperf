rootProject.name = "GCPerf"
include(":app")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://api.xposed.info/") } // <--- A PORRA DO XPOSED AQUI
    }
}
rootProject.name = "GCPerf"
include(":app")

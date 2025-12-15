
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}

include(":launchers:connector")
include(":launchers:virtual-connector")
include(":launchers:controlplane")
include(":extensions:testing-extension")

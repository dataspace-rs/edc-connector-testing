
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

include(":launchers:virtual-controlplane")
include(":launchers:controlplane")
include(":extensions:testing-extension")

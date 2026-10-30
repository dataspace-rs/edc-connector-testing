
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
include(":launchers:controlplane-dcp")
include(":launchers:identityhub")
include(":launchers:issuerservice")
include(":extensions:testing-extension")
include(":extensions:embedded-dataplane")
include(":extensions:ih-superuser-seed")

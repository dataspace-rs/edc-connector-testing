import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin

plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
    alias(libs.plugins.docker)
}

dependencies {
    runtimeOnly(libs.edc.bom.controlplane)
    runtimeOnly(libs.edc.bom.dataplane) {
        exclude("org.eclipse.edc", "data-plane-selector-client")
    }
    runtimeOnly(project(":extensions:testing-extension"))
    runtimeOnly(libs.edc.iam.mock)
    runtimeOnly(libs.edc.cp.api.configuration)
    runtimeOnly(libs.edc.dp.selector.api)
    runtimeOnly(libs.edc.dp.signaling)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    dependsOn("distTar", "distZip")
    mergeServiceFiles()
    archiveFileName.set("connector.jar")
}

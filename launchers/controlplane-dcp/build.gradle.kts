plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
    alias(libs.plugins.docker)
}

// An in-memory EDC control plane with DCP identity (tokens from an IdentityHub STS) and an embedded
// testing data plane: one container per participant, configured entirely through environment variables.
dependencies {
    runtimeOnly(libs.edc.bom.controlplane.dcp)
    runtimeOnly(libs.edc.api.secrets)
    runtimeOnly(project(":extensions:testing-extension"))
    runtimeOnly(project(":extensions:embedded-dataplane"))
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    dependsOn("distTar", "distZip")
    // transformers must see duplicates, otherwise EXCLUDE drops them before mergeServiceFiles() runs
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
    archiveFileName.set("controlplane-dcp.jar")
}

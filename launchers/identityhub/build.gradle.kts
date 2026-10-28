plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
    alias(libs.plugins.docker)
}

// An in-memory IdentityHub (DID documents, Credential Service, STS, Identity API) with an admin API key
// seeded at boot: a participant's wallet for tests.
dependencies {
    runtimeOnly(libs.edc.bom.identityhub)
    runtimeOnly(project(":extensions:ih-superuser-seed"))
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    dependsOn("distTar", "distZip")
    // transformers must see duplicates, otherwise EXCLUDE drops them before mergeServiceFiles() runs
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
    archiveFileName.set("identityhub.jar")
}

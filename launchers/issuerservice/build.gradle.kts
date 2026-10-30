plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
    alias(libs.plugins.docker)
}

// An in-memory EDC IssuerService (DCP issuance, Issuer Admin API, status lists, DID documents) with an
// admin API key seeded at boot (set testing.ih.superuser.scopes to include issuer-admin-api:admin): an
// issuer for tests.
dependencies {
    runtimeOnly(libs.edc.bom.issuerservice)
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
    archiveFileName.set("issuerservice.jar")
}

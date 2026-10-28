
plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.spi.core)
    implementation(libs.dataplane.sdk.core)
    implementation(libs.dataplane.sdk.jakarta.ee)
}

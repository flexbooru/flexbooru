plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugin.agp)
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.oss.licenses)
    implementation(libs.javapoet)
}
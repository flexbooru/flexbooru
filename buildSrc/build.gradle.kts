plugins {
    `kotlin-dsl`
}

apply(from = "../repositories.gradle.kts")

dependencies {
    implementation(rootProject.extra["android_gradle_plugin"].toString())
    implementation(kotlin("gradle-plugin", rootProject.extra["kotlin_version"].toString()))
    implementation("com.squareup:javapoet:1.13.0")
}
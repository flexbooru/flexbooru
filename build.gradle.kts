apply(plugin = "com.github.ben-manes.versions")

buildscript {
    extra.apply {
        set("nav_version", "2.3.0-alpha04")
        set("room_version", "2.2.5")
        set("work_version", "2.4.0-alpha02")
        set("glide_version", "4.11.0")
        set("lifecycle_version", "2.3.0-alpha01")
        set("exoplayer_version", "2.11.3")
        set("okhttp_version", "4.5.0")
        set("retrofit_version", "2.8.1")
        set("tikxml_version", "0.8.13")
        set("kodein_version", "6.5.4")
        set("coroutines_version", "1.3.5")
        set("serialization_version", "0.20.0")
        set("ktor_version", "1.3.2")
    }
    repositories {
        google()
        jcenter()
        maven(url = "https://maven.fabric.io/public")
    }
    dependencies {
        val kotlinVersion = "1.3.71"
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath(kotlin("serialization", kotlinVersion))
        classpath("com.android.tools.build:gradle:4.0.0-beta04")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.28.0")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("io.fabric.tools:gradle:1.31.2")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://jitpack.io")
        maven(url = "https://kotlin.bintray.com/kotlinx/")
    }
    gradle.projectsEvaluated {
        tasks.withType(JavaCompile::class.java) {
            options.compilerArgs.plusAssign(listOf("-Xlint:deprecation"))
        }
    }
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    // optional parameters
    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}

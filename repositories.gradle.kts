rootProject.extra.apply {
    set("android_gradle_plugin", "com.android.tools.build:gradle:7.4.2")
    set("kotlin_version", "1.8.10")
    set("kotlin_compose_compiler_version", "1.4.2")
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

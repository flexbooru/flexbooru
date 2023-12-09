plugins {
    alias(libs.plugins.ben.versions)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.navigation) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    //    alias(libs.plugins.gms.oss.licenses) apply false
}

buildscript {
    dependencies {
        classpath(libs.plugin.oss.licenses)
    }
}
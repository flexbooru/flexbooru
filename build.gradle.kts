plugins {
    alias(libs.plugins.ben.versions)
    id(libs.plugins.android.application.get().pluginId) apply false
    id(libs.plugins.kotlin.android.get().pluginId) apply false
    id(libs.plugins.kotlin.parcelize.get().pluginId) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.navigation) apply false
    alias(libs.plugins.gms) apply false
    id(libs.plugins.gms.oss.licenses.get().pluginId) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
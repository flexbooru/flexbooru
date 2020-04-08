/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("io.fabric")
}

val properties = Properties()
properties.load(project.rootProject.file(".gradle/keystore.properties").inputStream())
val byteOut = org.apache.commons.io.output.ByteArrayOutputStream()
project.exec {
    commandLine = "git rev-list HEAD --first-parent --count".split(" ")
    standardOutput = byteOut
}
val verCode = String(byteOut.toByteArray()).trim().toInt()

android {
    signingConfigs {
        create("release") {
            storeFile = file("../.gradle/flexbooru_play.jks")
            keyAlias = properties.getProperty("KEY_ALIAS")
            keyPassword = properties.getProperty("KEY_PASS")
            storePassword = properties.getProperty("STORE_PASS")
        }
    }
    compileSdkVersion(29)
    buildToolsVersion = "29.0.3"
    defaultConfig {
        applicationId = "onlymash.flexbooru.play"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = verCode
        versionName = "2.1.3"
        versionNameSuffix = ".c$verCode"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders = mapOf("backupApiKey" to "AEdPqrEAAAAICNAmVRgkNfsB1ObTK7LGamWWT5FMDLiGqhIcyw")
        resConfigs(listOf("en", "zh-rCN", "zh-rHK", "zh-rTW", "nl-rNL",
            "pt-rBR", "es-rES", "pl-rPL", "fr-rFR", "hu-rHU", "ru-rRU",
            "ja-rJP", "in-rID", "de-rDE"))
    }
    applicationVariants.all {
        outputs.map {
            it as com.android.build.gradle.internal.api.BaseVariantOutputImpl
        }
            .forEach { output ->
                output.outputFileName = "flexbooru_${defaultConfig.versionName}${defaultConfig.versionNameSuffix}.apk"
            }
    }
    buildTypes {
        buildTypes {
            getByName("release") {
                isShrinkResources = true
                isMinifyEnabled = true
                signingConfig = signingConfigs.getByName("release")
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
            getByName("debug") {
                applicationIdSuffix = ".debug"
            }
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xopt-in=kotlinx.serialization.UnstableDefault",
            "-Xopt-in=kotlinx.serialization.ImplicitReflectionSerializer"
        )
    }
    compileOptions {
        coreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kapt {
        useBuildCache = true
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas".toString())
        }
    }
    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

androidExtensions.isExperimental = true

dependencies {
    val ext = rootProject.extra
    val kodeinVersion = ext.get("kodein_version") as String
    val coroutinesVersion = ext.get("coroutines_version") as String
    val serializationVersion = ext.get("serialization_version") as String
    val navVersion = ext.get("nav_version") as String
    val roomVersion = ext.get("room_version") as String
    val workVersion = ext.get("work_version") as String
    val glideVersion = ext.get("glide_version") as String
    val lifecycleVersion = ext.get("lifecycle_version") as String
    val exoplayerVersion = ext.get("exoplayer_version") as String
    val okhttpVersion = ext.get("okhttp_version") as String
    val retrofitVersion = ext.get("retrofit_version") as String
    val tikxmlVersion = ext.get("tikxml_version") as String

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.0.5")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":common"))
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
    implementation("org.kodein.di:kodein-di-framework-android-x:$kodeinVersion")
    implementation("org.kodein.di:kodein-di-erased-jvm:$kodeinVersion")
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.appcompat:appcompat:1.2.0-beta01")
    implementation("androidx.fragment:fragment-ktx:1.3.0-alpha03")
    implementation("androidx.preference:preference-ktx:1.1.0")
    implementation("androidx.core:core-ktx:1.3.0-beta01")
    implementation("androidx.recyclerview:recyclerview:1.2.0-alpha02")
    implementation("androidx.viewpager2:viewpager2:1.1.0-alpha01")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0-beta01")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.browser:browser:1.3.0-alpha01")
    implementation("androidx.drawerlayout:drawerlayout:1.1.0-beta01")
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-beta4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Lifecycles only (without ViewModel or LiveData)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    // optional - helpers for implementing LifecycleOwner in a Service
    implementation("androidx.lifecycle:lifecycle-service:$lifecycleVersion")
    // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.paging:paging-runtime-ktx:2.1.2")
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("com.google.android.material:material:1.2.0-alpha05")
    implementation("com.google.android:flexbox:2.0.1")
    implementation("com.google.android.apps.muzei:muzei-api:3.2.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.github.onlymash:subsampling-scale-image-view:3.10.3")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.onlymash:OMFM:1.1.4")
    implementation("com.mikepenz:materialdrawer:8.0.1")
    implementation("com.github.kenglxn.QRGen:android:2.6.0")
    implementation("xyz.belvi.mobilevision:barcodescanner:2.0.3")
    implementation("com.google.firebase:firebase-core:17.3.0")
    implementation("com.crashlytics.sdk.android:crashlytics:2.10.1")
    implementation("com.google.android.gms:play-services-vision:19.0.0")
    implementation("com.android.billingclient:billing:2.2.0")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:1.1.0")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.5.0")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.squareup.okio:okio:2.5.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    implementation("com.github.bumptech.glide:okhttp3-integration:$glideVersion")
    kapt("com.github.bumptech.glide:compiler:$glideVersion")
    implementation("com.google.android.exoplayer:exoplayer-core:$exoplayerVersion")
    implementation("com.google.android.exoplayer:exoplayer-ui:$exoplayerVersion")
    implementation("com.tickaroo.tikxml:annotation:$tikxmlVersion")
    implementation("com.tickaroo.tikxml:core:$tikxmlVersion")
    implementation("com.tickaroo.tikxml:retrofit-converter:$tikxmlVersion")
    kapt("com.tickaroo.tikxml:processor:$tikxmlVersion")
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.robolectric:robolectric:4.3.1")
    androidTestImplementation("androidx.work:work-testing:$workVersion")
    androidTestImplementation("androidx.test:core:1.3.0-alpha05")
    androidTestImplementation("androidx.test.ext:junit:1.1.2-alpha05")
    androidTestImplementation("androidx.test:runner:1.3.0-alpha05")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0-alpha05")
}

apply { plugin("com.google.gms.google-services") }
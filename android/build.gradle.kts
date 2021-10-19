/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.apache.commons.io.output.ByteArrayOutputStream
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.gms.oss-licenses-plugin")
    kotlin("android")
    kotlin("plugin.serialization")
    kotlin("kapt")
}

val releaseStoreFile = file("../.gradle/flexbooru_play.jks")
val storePropertyFile = file("../.gradle/keystore.properties")

val properties = Properties()
if (storePropertyFile.exists()) {
    properties.load(storePropertyFile.inputStream())
}
val byteOut = ByteArrayOutputStream()
exec {
    commandLine = "git rev-list HEAD --first-parent --count".split(" ")
    standardOutput = byteOut
}
val verCode = String(byteOut.toByteArray()).trim().toInt()

android {
    signingConfigs {
        if (storePropertyFile.exists() && releaseStoreFile.exists()) {
            create("release") {
                storeFile = releaseStoreFile
                keyAlias = properties.getProperty("KEY_ALIAS")
                keyPassword = properties.getProperty("KEY_PASS")
                storePassword = properties.getProperty("STORE_PASS")
            }
        }
    }
    compileSdk = 31
    buildToolsVersion = "31.0.0"
    defaultConfig {
        applicationId = "onlymash.flexbooru.play"
        minSdk = 21
        targetSdk = 31
        versionCode = verCode
        versionName = "2.7.7"
        versionNameSuffix = ".c$verCode"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["backupApiKey"] = "AEdPqrEAAAAICNAmVRgkNfsB1ObTK7LGamWWT5FMDLiGqhIcyw"
        resourceConfigurations.addAll(listOf("en", "zh-rCN", "zh-rHK", "zh-rTW", "nl-rNL", "pt-rBR", "es-rES",
                "pl-rPL", "fr-rFR", "hu-rHU", "ru-rRU", "ja-rJP", "in-rID", "de-rDE"))
    }
    applicationVariants.all {
        outputs.map {
            it as BaseVariantOutputImpl
        }
            .forEach { output ->
                output.outputFileName = "flexbooru_${defaultConfig.versionName}${defaultConfig.versionNameSuffix}.apk"
            }
    }
    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            val config = try {
                signingConfigs.getByName("release")
            } catch (_: UnknownDomainObjectException) {
                null
            }
            if (config != null) {
                signingConfig = config
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xopt-in=kotlin.ExperimentalStdlibApi",
            "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xopt-in=kotlinx.coroutines.DelicateCoroutinesApi",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=kotlinx.coroutines.FlowPreview",
            "-Xopt-in=androidx.paging.ExperimentalPagingApi"
        )
    }
    kapt {
        useBuildCache = true
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
    packagingOptions {
        resources.excludes.add("META-INF/*.kotlin_module")
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    buildFeatures {
        viewBinding = true
    }
    lint {
        disable += setOf("DialogFragmentCallbacksDetector")
    }
}

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
    val xmlutilVersion = ext.get("xmlutil_version") as String

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":common"))
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("org.kodein.di:kodein-di-framework-android-core:$kodeinVersion")
    implementation("org.kodein.di:kodein-di-framework-android-x:$kodeinVersion")
    implementation("androidx.annotation:annotation:1.3.0-beta01")
    implementation("androidx.appcompat:appcompat:1.4.0-beta01")
    implementation("androidx.activity:activity-ktx:1.4.0-rc01")
    implementation("androidx.fragment:fragment-ktx:1.4.0-alpha10")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.core:core-ktx:1.7.0-rc01")
    implementation("androidx.recyclerview:recyclerview:1.3.0-alpha01")
    implementation("androidx.viewpager2:viewpager2:1.1.0-beta01")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("androidx.documentfile:documentfile:1.1.0-alpha01")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.browser:browser:1.4.0-rc01")
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
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
    implementation("androidx.paging:paging-runtime-ktx:3.1.0-beta01")
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("com.google.android.material:material:1.5.0-alpha04")
    implementation("com.google.android:flexbox:2.0.1")
    implementation("com.google.android.apps.muzei:muzei-api:3.4.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.github.onlymash:subsampling-scale-image-view:3.10.3")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.onlymash:OMFM:1.1.4")
    implementation("com.mikepenz:materialdrawer:8.4.4")
    implementation("com.google.zxing:core:3.4.1")
    implementation("xyz.belvi.mobilevision:barcodescanner:2.0.3")
    implementation("com.google.firebase:firebase-analytics-ktx:19.0.2")
    implementation("com.google.firebase:firebase-crashlytics:18.2.3")
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("com.android.billingclient:billing:4.0.0")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:1.1.0")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:$okhttpVersion")
    implementation("com.squareup.okio:okio:3.0.0-alpha.10")
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    implementation("com.github.bumptech.glide:okhttp3-integration:$glideVersion")
    kapt("com.github.bumptech.glide:compiler:$glideVersion")
    implementation("com.google.android.exoplayer:exoplayer-core:$exoplayerVersion")
    implementation("com.google.android.exoplayer:exoplayer-ui:$exoplayerVersion")
    implementation("io.github.pdvrieze.xmlutil:core-android:$xmlutilVersion")
    implementation("io.github.pdvrieze.xmlutil:serialization-android:$xmlutilVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.6.1")
    androidTestImplementation("androidx.work:work-testing:$workVersion")
    androidTestImplementation("androidx.test:core:1.4.1-alpha03")
    androidTestImplementation("androidx.test.ext:junit:1.1.4-alpha03")
    androidTestImplementation("androidx.test:runner:1.4.1-alpha03")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0-alpha03")
}

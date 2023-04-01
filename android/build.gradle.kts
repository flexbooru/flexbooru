/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.apache.commons.io.output.ByteArrayOutputStream
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9"
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.gms.oss-licenses-plugin")
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
    compileSdk = 33
    defaultConfig {
        applicationId = "onlymash.flexbooru.play"
        minSdk = 21
        targetSdk = 33
        versionCode = verCode
        versionName = "3.1.6"
        versionNameSuffix = ".c$verCode"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations.addAll(listOf("en", "zh-rCN", "zh-rHK", "zh-rTW", "nl-rNL", "pt-rBR", "es-rES",
                "pl-rPL", "fr-rFR", "hu-rHU", "ru-rRU", "ja-rJP", "in-rID", "de-rDE", "tr-rTR"))
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
            val config = try {
                signingConfigs.getByName("release")
            } catch (_: UnknownDomainObjectException) {
                null
            }
            if (config != null) {
                signingConfig = config
            }
        }
    }
    compileOptions {
//        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlin.ExperimentalStdlibApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=androidx.paging.ExperimentalPagingApi",
            "-opt-in=nl.adaptivity.xmlutil.ExperimentalXmlUtilApi",
            "-opt-in=coil.annotation.ExperimentalCoilApi",
            "-Xjvm-default=all-compatibility"
        )
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    packagingOptions {
        resources.excludes.add("META-INF/*.kotlin_module")
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    lint {
        disable += setOf("DialogFragmentCallbacksDetector")
    }
    namespace = "onlymash.flexbooru"
}

dependencies {

//    coreLibraryDesugaring(Libs.desugarJdkLibs)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":common"))

    implementation(Libs.kotlinxMetadataJvm)
    implementation(Libs.kotlinxCoroutinesAndroid)
    implementation(Libs.kotlinxSerializationJson)

    implementation(Libs.kodeinCore)
    implementation(Libs.kodeinAndroidX)

    implementation(Libs.annotation)
    implementation(Libs.appcompat)
    implementation(Libs.activityKtx)
    implementation(Libs.fragment)
    implementation(Libs.fragmentKtx)
    implementation(Libs.preferenceKtx)
    implementation(Libs.coreKtx)
    implementation(Libs.recyclerview)

    implementation(Libs.material)
    implementation(Libs.constraintLayout)
    implementation(Libs.swipeRefreshLayout)
    implementation(Libs.drawerLayout)
    implementation(Libs.viewPager2)
    implementation(Libs.flexboxLayout)

    implementation(Libs.multidex)
    implementation(Libs.browser)
    implementation(Libs.documentFile)

    implementation(Libs.navigationFragmentKtx)
    implementation(Libs.navigationUiKtx)
    implementation(Libs.navigationDynamicFeaturesFragment)

    implementation(Libs.roomKtx)
    implementation(Libs.roomRuntime)
    implementation(Libs.roomPaging)
    ksp(Libs.roomCompiler)


    implementation(Libs.lifecycleRuntimeKtx)
    implementation(Libs.lifecycleViewModelKtx)
    implementation(Libs.lifecycleLivedataKtx)
    implementation(Libs.lifecycleViewModelSavedState)

    implementation(Libs.pagingRuntimeKtx)
    implementation(Libs.workRuntimeKtx)
    implementation(Libs.muzeiApi)
    implementation(Libs.photoView)
    implementation(Libs.subsamplingScaleImageView)
    implementation(Libs.omfm)
    implementation(Libs.materialDrawer)
    implementation(Libs.zxingCore)
    implementation(Libs.barCodeScanner)

    implementation(platform(Libs.firebaseBom))
    implementation(Libs.firebaseAnalyticsKtx)
    implementation(Libs.firebaseCrashlytics)

    implementation(Libs.playServicesAds)
    implementation(Libs.playServicesVision)
    implementation(Libs.playServicesOssLicenses)
    implementation(Libs.billingKtx)

    implementation(Libs.retrofit)
    implementation(Libs.retrofitKotlinxSerializationConverter)
    implementation(Libs.okhttp)
    implementation(Libs.okhttpLoggingInterceptor)
    implementation(Libs.okhttpDoH)
    implementation(Libs.okio)

    implementation(Libs.coil)
    implementation(Libs.coilGif)

    implementation(Libs.xmlutilAndroidCore)
    implementation(Libs.xmlutilAndroidSerialization)

    implementation(Libs.exoplayerCore)
    implementation(Libs.exoplayerUi)

    testImplementation(Libs.junit)
    testImplementation(Libs.robolectric)
    androidTestImplementation(Libs.androidxJunit)
    androidTestImplementation(Libs.espressoCore)
}

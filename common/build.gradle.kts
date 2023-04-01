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

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

android {
    compileSdk = 33
    defaultConfig.minSdk = 21
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "onlymash.flexbooru.common"
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    android {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi",
                )
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Libs.kotlinxCoroutinesCore)
                implementation(Libs.kotlinxSerializationJson)
                implementation(Libs.kodein)
                implementation(Libs.ktorClientContentNegotiation)
                implementation(Libs.ktorSerializationKotlinxJson)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(Libs.kotlinxCoroutinesCoreJvm)
                implementation(Libs.kotlinxSerializationJson)
                implementation(Libs.kodeinJvm)
                implementation(Libs.ktorClientCoreJvm)
                implementation(Libs.ktorClientOkhttp)
                implementation(Libs.ktorClientJsonJvm)
                implementation(Libs.ktorClientSerializationJvm)
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependsOn(jvmMain)
        }
    }
}
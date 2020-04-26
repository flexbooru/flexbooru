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

import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

val ext = rootProject.extra
val ktorVersion = ext.get("ktor_version") as String
val kodeinVersion = ext.get("kodein_version") as String
val coroutinesVersion = ext.get("coroutines_version") as String
val serializationVersion = ext.get("serialization_version") as String

android {
    compileSdkVersion(29)
    buildToolsVersion = "29.0.3"
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
        }
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    android {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xopt-in=kotlinx.serialization.UnstableDefault",
                    "-Xopt-in=kotlinx.serialization.ImplicitReflectionSerializer",
                    "-Xuse-experimental=kotlin.Experimental"
                )
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationVersion")
                implementation("org.kodein.di:kodein-di-core:$kodeinVersion")
                implementation("org.kodein.di:kodein-di-erased:$kodeinVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-json:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
                implementation("org.kodein.di:kodein-di-erased-jvm:$kodeinVersion")
                implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependsOn(jvmMain)
        }
    }
}
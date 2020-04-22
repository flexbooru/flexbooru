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
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}
kotlin {
    jvm {
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
}

fun DependencyHandler.jvmMainImplementation(dependencyNotation: Any): Dependency? =
    add("jvmMainImplementation", dependencyNotation)

dependencies {
    val ext = rootProject.extra
    val ktorVersion = ext.get("ktor_version") as String
    val kodeinVersion = ext.get("kodein_version") as String
    val coroutinesVersion = ext.get("coroutines_version") as String
    val serializationVersion = ext.get("serialization_version") as String

    commonMainImplementation(kotlin("stdlib-common"))
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationVersion")
    commonMainImplementation("org.kodein.di:kodein-di-core:$kodeinVersion")
    commonMainImplementation("org.kodein.di:kodein-di-erased:$kodeinVersion")
    commonMainImplementation("io.ktor:ktor-client-core:$ktorVersion")
    commonMainImplementation("io.ktor:ktor-client-json:$ktorVersion")
    commonMainImplementation("io.ktor:ktor-client-serialization:$ktorVersion")
    jvmMainImplementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    jvmMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    jvmMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
    jvmMainImplementation("org.kodein.di:kodein-di-erased-jvm:$kodeinVersion")
    jvmMainImplementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    jvmMainImplementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    jvmMainImplementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
    jvmMainImplementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")

}
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

plugins {
    id("com.github.ben-manes.versions") version("0.41.0")
}

buildscript {
    extra.apply {
        set("nav_version", "2.5.0-alpha03")
        set("room_version", "2.5.0-alpha01")
        set("work_version", "2.8.0-alpha01")
        set("glide_version", "4.13.1")
        set("lifecycle_version", "2.5.0-alpha04")
        set("exoplayer_version", "2.17.0")
        set("okhttp_version", "5.0.0-alpha.5")
        set("retrofit_version", "2.9.0")
        set("xmlutil_version", "0.84.1")
        set("kodein_version", "7.11.0")
        set("coroutines_version", "1.6.0")
        set("serialization_version", "1.3.2")
        set("ktor_version", "1.6.7")
    }
    repositories {
        google()
    }
    dependencies {
        val kotlinVersion = "1.6.10"
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath(kotlin("serialization", kotlinVersion))
        classpath("com.android.tools.build:gradle:7.1.2")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.5")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://jitpack.io")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/releases/")
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
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

plugins {
    id("com.github.ben-manes.versions") version("0.39.0")
}

buildscript {
    extra.apply {
        set("nav_version", "2.4.0-alpha10")
        set("room_version", "2.4.0-beta01")
        set("work_version", "2.7.0")
        set("glide_version", "4.12.0")
        set("lifecycle_version", "2.4.0-rc01")
        set("exoplayer_version", "2.15.1")
        set("okhttp_version", "5.0.0-alpha.2")
        set("retrofit_version", "2.9.0")
        set("xmlutil_version", "0.83.0")
        set("kodein_version", "7.8.0")
        set("coroutines_version", "1.5.2")
        set("serialization_version", "1.3.0")
        set("ktor_version", "1.6.4")
    }
    repositories {
        google()
    }
    dependencies {
        val kotlinVersion = "1.5.31"
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath(kotlin("serialization", kotlinVersion))
        classpath("com.android.tools.build:gradle:7.1.0-beta01")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.4")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")
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
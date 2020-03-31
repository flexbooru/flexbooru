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
    jvmMainImplementation(kotlin("stdlib-jdk8"))
    jvmMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    jvmMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
    jvmMainImplementation("org.kodein.di:kodein-di-erased-jvm:$kodeinVersion")
    jvmMainImplementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    jvmMainImplementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    jvmMainImplementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
    jvmMainImplementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")

}
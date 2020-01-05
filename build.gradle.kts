import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.noarg")
}

group = "be.bluexin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(coroutine("jdk8"))

    implementation("be.bluexin:kaeron")

    implementation("it.unimi.dsi", "fastutil", version("fastutil"))

    implementation("net.onedaybeard.artemis", "artemis-odb", version("artemis"))
    implementation("net.onedaybeard.artemis", "artemis-odb-serializer-kryo", version("artemis"))
    implementation("net.onedaybeard.artemis", "artemis-odb-serializer-json", version("artemis"))

    implementation("com.github.javafaker", "javafaker", version("javafaker"))
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", version("jackson"))

    // Logging
    implementation("org.slf4j", "slf4j-api", version("slf4j"))
    implementation("io.github.microutils", "kotlin-logging", version("klog"))
    runtimeOnly("ch.qos.logback", "logback-classic", version("logback"))
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += listOf("-Xuse-experimental=kotlin.Experimental", "-XXLanguage:+InlineClasses")
    }
}

tasks.withType<AbstractArchiveTask> {
    archiveBaseName.convention(provider { project.name.toLowerCase() })
}

noArg {
    annotation("be.bluexin.brahma.Noarg")
}

fun Project.version(name: String) = extra.properties["${name}_version"] as? String

fun Project.coroutine(module: String): Any =
    "org.jetbrains.kotlinx:kotlinx-coroutines-$module:${version("coroutines")}"

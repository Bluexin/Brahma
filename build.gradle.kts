import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
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

    implementation("be.bluexin:kaeron:1.0-SNAPSHOT")

    implementation("it.unimi.dsi", "fastutil", version("fastutil"))

    api("net.onedaybeard.artemis", "artemis-odb", version("artemis"))
    implementation("net.onedaybeard.artemis", "artemis-odb-serializer-kryo", version("artemis"))
    implementation("net.onedaybeard.artemis", "artemis-odb-serializer-json", version("artemis"))

    implementation("com.github.javafaker", "javafaker", version("javafaker"))
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", version("jackson"))

    // Logging
    implementation("org.slf4j", "slf4j-api", version("slf4j"))
    implementation("io.github.microutils", "kotlin-logging", version("klog"))
    runtimeOnly("ch.qos.logback", "logback-classic", version("logback"))

    // Testing
    testImplementation("org.junit.jupiter", "junit-jupiter-api", version("junit"))
    testImplementation("org.junit.jupiter", "junit-jupiter-params", version("junit"))
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", version("junit"))
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        javaParameters = true
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += listOf("-Xuse-experimental=kotlin.Experimental", "-XXLanguage:+InlineClasses")
    }
}

tasks.withType<AbstractArchiveTask> {
    archiveBaseName.convention(provider { project.name.toLowerCase() })
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
}

noArg {
    annotation("be.bluexin.brahma.Noarg")
}

publishing {
    publications.create<MavenPublication>("publication") {
        from(components["java"])
        this.artifactId = rootProject.name.toLowerCase()
    }

    repositories {
        val mavenPassword = if (hasProp("local")) null else prop("sbxMavenPassword")
        maven {
            url =
                uri(if (mavenPassword != null) "sftp://maven.sandboxpowered.org:22/sbxmvn/" else "file://$buildDir/repo")
            if (mavenPassword != null) {
                credentials(PasswordCredentials::class.java) {
                    username = prop("sbxMavenUser")
                    password = mavenPassword
                }
            }
        }

    }
}

fun Project.hasProp(name: String): Boolean = name in project.properties

fun Project.prop(name: String): String? = project.properties[name] as? String

fun Project.version(name: String) = extra.properties["${name}_version"] as? String

fun Project.coroutine(module: String): Any =
    "org.jetbrains.kotlinx:kotlinx-coroutines-$module:${version("coroutines")}"

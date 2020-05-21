import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group = "be.bluexin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", version("jackson"))
    implementation("org.apache.velocity", "velocity-engine-core", version("velocity"))
}

gradlePlugin {
    plugins {
        create("brahma-generator") {
            id = "${project.group}.${project.name.toLowerCase()}"
            implementationClass = "be.bluexin.brahma.generator.BrahmaGeneratorPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

java {
    withSourcesJar()
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

publishing {
    publications.create<MavenPublication>("pluginMaven") {
        this.artifactId = base.archivesBaseName.toLowerCase()
    } // from will be set by java-gradle-plugin

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


fun hasProp(name: String): Boolean = extra.has(name)

fun prop(name: String): String? = extra.properties[name] as? String

fun Project.version(name: String) = extra.properties["${name}_version"] as? String
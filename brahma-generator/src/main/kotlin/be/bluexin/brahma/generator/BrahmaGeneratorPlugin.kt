/*
 * Copyright (C) 2019-2020 Arnaud 'Bluexin' Solé
 *
 * This file is part of Brahma-generator.
 *
 * Brahma-generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Brahma-generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Brahma-generator.  If not, see <https://www.gnu.org/licenses/>.
 */

package be.bluexin.brahma.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.GenerateIdeaModule
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.io.File

@Suppress("unused")
class BrahmaGeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        addSourceSetExtensions(target)
        val extension = target.extensions.create("brahmagen", BrahmaGeneratorPluginExtension::class.java)
        target.afterEvaluate {
            addTaskToSourceSets(target, extension)
            addSourcesToIde(target)

            println(target.extensions.extensionsSchema.elements.joinToString(separator = "\n") { "${it.name} -> ${it.publicType}" })
        }
    }

    private fun addSourceSetExtensions(target: Project) {
        target.sourceSets.all {
            val name = this.name

            @Suppress("UnstableApiUsage")
            val sds = target.objects.sourceDirectorySet(name, "$name Brahma Component definitions")
            this.extensions.add("brahma", sds)
            sds.srcDir("src/$name/brahma")
            sds.include("**/*.json")
        }
    }

    private fun addTaskToSourceSets(target: Project, extension: BrahmaGeneratorPluginExtension) {
        target.sourceSets.all {
            val name = "generate${this.name.capitalize()}Brahma"
            val brahmaTask = target.task<GenerateBrahmaTask>(name) {
                group = "Brahma Generation"
                description = "Generates Brahma Components for source set ${this@all.name}"
                outputBaseDir = { "${extension.output}/${this@all.name}" }
                addSourceFiles(this@all.extensions["brahma"] as FileCollection)
            }
            linkTaskToSourceCompile(target, this, brahmaTask)
        }
    }

    private fun linkTaskToSourceCompile(target: Project, sourceSet: SourceSet, brahmaTask: GenerateBrahmaTask) {
        val compileTask = target.tasks.findByName(sourceSet.compileJavaTaskName)
        if (compileTask != null) linkBrahmaTaskToTask(brahmaTask, compileTask)
        else target.tasks.whenTaskAdded {
            if (this.name == sourceSet.compileJavaTaskName) linkBrahmaTaskToTask(brahmaTask, this)
        }
    }

    private fun linkBrahmaTaskToTask(brahmaTask: GenerateBrahmaTask, task: Task) {
        task.dependsOn(brahmaTask)
        (task as JavaCompile).source(brahmaTask.getOutputSourceDirectorySet())
    }

    private fun addSourcesToIde(target: Project) {
        target.sourceSets.forEach {
            val sds = it.extensions["brahma"] as SourceDirectorySet
            sds.srcDirs.forEach { f ->
                addToIdeSources(target, false, it.name == "test", f)
            }
        }
        target.tasks.withType<GenerateBrahmaTask>().forEach {
            it.getOutputSourceDirectorySet().srcDirs.forEach { f ->
                addToIdeSources(target, true, it.name.contains("Test"), f)
            }
        }
    }

    private fun addToIdeSources(target: Project, isGenerated: Boolean, isTest: Boolean, file: File) {
        val ideaModel = target.extensions.findByType<IdeaModel>()
        if (ideaModel != null) { // FIXME: this doesn't actually do shit smh
            if (isTest) {
                if (isGenerated) ideaModel.module.generatedSourceDirs.add(file)
                else ideaModel.module.testSourceDirs.add(file)
            } else {
                if (isGenerated) ideaModel.module.generatedSourceDirs.add(file)
                else ideaModel.module.sourceDirs.add(file)
            }
            target.tasks.withType<GenerateIdeaModule>().forEach {
                println(it)
                it.doFirst {
                    println("boo!")
                    file.mkdirs()
                }
            }
        }
    }
}

open class BrahmaGeneratorPluginExtension {
    var output: String? = null
}

val Project.sourceSets get() = this.extensions["sourceSets"] as SourceSetContainer
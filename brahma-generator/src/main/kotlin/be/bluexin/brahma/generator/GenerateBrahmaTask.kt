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

import be.bluexin.brahma.generator.data.Component
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.context.Context
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.*
import java.io.File

open class GenerateBrahmaTask : DefaultTask() {
    lateinit var outputBaseDir: () -> Any?
    private val sourceFiles = project.files()

    fun addSourceFiles(files: FileCollection) {
        sourceFiles.from(files)
    }

    @Internal
    fun getOutputSourceDirectorySet(): SourceDirectorySet {
        val srcSetName = "generate-brahma-$name"

        @Suppress("UnstableApiUsage")
        val srcSet = project.objects.sourceDirectorySet(srcSetName, srcSetName)
        srcSet.srcDir(outputBaseDir)
        return srcSet
    }

    private val velocity by lazy {
        VelocityEngine().apply {
            setProperty(RuntimeConstants.RUNTIME_LOG_NAME, "Brahma Generator")
            setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, true)
            setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath")
            setProperty("resource.loader.classpath.class", ClasspathResourceLoader::class.java.name)

            init()
        }
    }

    private val template by lazy {
        velocity.getTemplate("/vm/component.vm").apply {
            process()
        }
    }

    private val globalContext by lazy {
        StringType
        IntType
        VelocityContext(
            mapOf(
                "resolver" to typeMap
            )
        )
    }

    private val json by lazy { jacksonObjectMapper() }

    @SkipWhenEmpty
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    fun getSourceFiles(): FileCollection = sourceFiles

    @OutputDirectory
    fun getTarget(): File = project.file(outputBaseDir)

    @TaskAction
    fun generate() {
        val output = getTarget()
        if (!output.exists()) output.mkdirs()

        getSourceFiles().forEach {
            try {
                generateComponent(it, output)
            } catch (e: Exception) {
                println("Something went wrong: `${e.message}`")
                throw e
            }
        }
    }

    protected open fun generateComponent(input: File, outputDir: File) {
        val parsedComponent = json.readValue<Component>(input)
        val imports = parsedComponent.fields.asSequence()
            .flatMap { typeMap.getOrElse(it.type) { error("Unknown type `${it.type}`") }.imports.asSequence() }
            .distinct()
        val context: Context = VelocityContext(
            mutableMapOf(
                "component" to parsedComponent,
                "imports" to imports
            ), globalContext
        )
        val od = File(outputDir, parsedComponent.`package`.replace('.', '/'))
        if (!od.exists()) od.mkdirs()
        val of = File(od, "${parsedComponent.name}.java")
        if (of.exists()) of.delete()
        of.writer().use {
            template.merge(context, it)
        }
    }
}
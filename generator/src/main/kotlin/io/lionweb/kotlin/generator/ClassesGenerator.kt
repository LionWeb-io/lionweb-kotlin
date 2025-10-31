package io.lionweb.kotlin.generator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.lionweb.LionWebVersion
import io.lionweb.language.Concept
import io.lionweb.language.Enumeration
import io.lionweb.language.Interface
import io.lionweb.language.Language
import io.lionweb.language.PrimitiveType
import io.lionweb.serialization.AbstractSerialization
import io.lionweb.serialization.JsonSerialization
import io.lionweb.serialization.ProtoBufSerialization
import io.lionweb.serialization.SerializationProvider
import java.io.File

class ClassesGeneratorCommand : CliktCommand("classgen") {
    val dependenciesFiles: List<File> by option("--dependency", help = "Dependency file to generate classes for")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true, canBeFile = true)
        .multiple(required = false)
    val languageFiles: List<File> by option("--language", help = "Language file to generate classes for")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true, canBeFile = true)
        .multiple(required = true)
    val outputDir: File by option("--output", help = "Output directory for generated classes")
        .file(mustExist = false, canBeDir = true, mustBeReadable = false, canBeFile = false)
        .default(File("out"))
    val lwVersion : LionWebVersion by option("--lwversion", help = "LionWeb version to generate classes for")
        .enum<LionWebVersion>(ignoreCase = true)
        .default(LionWebVersion.currentVersion)

    override fun run() {
        val extensions = (languageFiles.map { it.extension } + dependenciesFiles.map { it.extension }).toSet()
        if (extensions.size != 1) {
            throw IllegalArgumentException("All language files and dependencies must have the same extension")
        }
        val extension = extensions.first().lowercase()
        val serialization : AbstractSerialization = when (extension) {
            "json" -> SerializationProvider.getStandardJsonSerialization(lwVersion)
            "pb" -> SerializationProvider.getStandardProtoBufSerialization(lwVersion)
            else -> throw IllegalArgumentException("Unsupported language extension: $extension")
        }
        fun loadLanguage(file: File): Language {
            val language = when (serialization) {
                is ProtoBufSerialization -> {
                    val nodes = serialization.deserializeToNodes(file)
                    val languages = nodes.filterIsInstance(Language::class.java)
                    if (languages.size != 1) {
                        throw IllegalArgumentException("Expected exactly one language in language file: $file")
                    }
                    languages.first()
                }
                is JsonSerialization -> {
                    serialization.loadLanguage(file)
                }
                else -> throw UnsupportedOperationException("Serialization not supported for language file: $file")
            }
            serialization.registerLanguage(language)
            return language
        }
        dependenciesFiles.forEach { dependencyFile ->
            loadLanguage(dependencyFile)
        }
        val languages = languageFiles.map { languageFile ->
            loadLanguage(languageFile)
        }
        languages.forEach { generateLanguage(it) }
    }

    private fun generateLanguage(language: Language) {
        echo("Generating classes for language ${language.name}")
        echo("-------------------------------------------------------------------")
        echo()
        language.elements.forEach { element ->
            echo(" - Generating class for ${element.javaClass.simpleName} ${element.name}")
            val fileSpec : FileSpec? = when (element) {
                is Enumeration -> generateEnumeration(element)
                is Concept -> generateConcept(element)
                is Interface -> generateInterface(element)
                is PrimitiveType -> null
                else -> TODO("Not yet implemented for element type ${element::class.simpleName}")
            }
            fileSpec?.let { save(it) }
        }
    }

    private fun save(fileSpec: FileSpec) {
        fileSpec.writeTo(outputDir)
    }

    private fun generateConcept(concept: Concept) : FileSpec {
        val dynamicNode = ClassName("io.lionweb.model.impl", "DynamicNode")
        val conceptType = TypeSpec.classBuilder(concept.name!!)
            .superclass(dynamicNode)
            .addModifiers(KModifier.PUBLIC)
        if (concept.isAbstract) {
            conceptType.addModifiers(KModifier.ABSTRACT)
        }
        if (concept.extendedConcept != null) {
            when (val superConcept = concept.extendedConcept) {
                else -> TODO()
            }
        }
        concept.implemented.forEach { TODO() }

        return FileSpec.builder(concept.language!!.name!!, concept.name!!)
            .addType(conceptType.build())
            .build()
    }

    private fun generateInterface(interf: Interface) : FileSpec {
        val interfaceType = TypeSpec.interfaceBuilder(interf.name!!)
            .addModifiers(KModifier.PUBLIC)
        return FileSpec.builder(interf.language!!.name!!, interf.name!!)
            .addType(interfaceType.build())
            .build()
    }

    private fun generateEnumeration(enumeration: Enumeration) : FileSpec {
        val enumType = TypeSpec.enumBuilder(enumeration.name!!)
            .addModifiers(KModifier.PUBLIC)

        for (literal in enumeration.literals) {
            enumType.addEnumConstant(literal.name!!)
        }

        return FileSpec.builder(enumeration.language!!.name!!, enumeration.name!!)
            .addType(enumType.build())
            .build()
    }
}

fun main(args: Array<String>) {
    ClassesGeneratorCommand().main(args)
}

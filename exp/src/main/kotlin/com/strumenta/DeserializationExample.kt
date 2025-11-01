package com.strumenta

import com.strumenta.rpgparser.model.RPGLanguage
import com.strumenta.starlasu.base.v1.ASTLanguageV1
import io.lionweb.LionWebVersion
import io.lionweb.serialization.SerializationProvider
import java.io.File

fun main(args: Array<String>) {
    val astFile = File("/Users/ftomassetti/repos/rpg-parser/example1.json")
    val jsonSerialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1)
    jsonSerialization.registerLanguage(ASTLanguageV1.getLanguage())
    jsonSerialization.registerLanguage(RPGLanguage)
    val ast = jsonSerialization.deserializeToNodes(astFile)
}
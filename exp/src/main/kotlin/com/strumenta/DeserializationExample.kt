package com.strumenta

import io.lionweb.LionWebVersion
import io.lionweb.serialization.SerializationProvider
import java.io.File

fun main(args: Array<String>) {
    val astFile = File("/Users/ftomassetti/repos/rpg-parser/example1.json")
    val jsonSerialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1)
    val ast = jsonSerialization.deserializeToNodes(astFile)
}
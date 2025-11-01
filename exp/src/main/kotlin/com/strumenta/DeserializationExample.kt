package com.strumenta

import com.strumenta.rpgparser.model.AssignmentType
import com.strumenta.rpgparser.model.ClearStatement
import com.strumenta.rpgparser.model.CompilationUnit
import com.strumenta.rpgparser.model.RPGLanguage
import com.strumenta.rpgparser.model.ReferenceExpr
import com.strumenta.rpgparser.model.serialization.registerDeserializersForRPGLanguage
import com.strumenta.starlasu.base.v1.ASTLanguageV1
import io.lionweb.LionWebVersion
import io.lionweb.language.Classifier
import io.lionweb.language.Property
import io.lionweb.model.ClassifierInstance
import io.lionweb.serialization.AbstractSerialization
import io.lionweb.serialization.Instantiator
import io.lionweb.serialization.PrimitiveValuesSerialization.PrimitiveDeserializer
import io.lionweb.serialization.SerializationProvider
import io.lionweb.serialization.UnavailableNodePolicy
import io.lionweb.serialization.data.SerializedClassifierInstance
import java.io.File
import kotlin.text.removePrefix
import kotlin.text.startsWith

data class Point(
    val line: Int,
    val column: Int,
)

data class Position(
    val start: Point,
    val end: Point,
)

val pointDeserializer: PrimitiveDeserializer<Point> =
    PrimitiveDeserializer<Point> { serialized ->
        if (serialized == null) {
            return@PrimitiveDeserializer null
        }
        require(serialized.startsWith("L"))
        require(serialized.removePrefix("L").isNotEmpty())
        val parts = serialized.removePrefix("L").split(":")
        require(parts.size == 2)
        Point(parts[0].toInt(), parts[1].toInt())
    }


fun main(args: Array<String>) {
    val astFile = File("/Users/ftomassetti/repos/rpg-parser/example1.json")
    val jsonSerialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1)
    jsonSerialization.registerLanguage(ASTLanguageV1.getLanguage())
    jsonSerialization.registerLanguage(RPGLanguage)
    jsonSerialization.primitiveValuesSerialization.registerDeserializer(ASTLanguageV1.getPosition().id!!) { serialized ->
        if (serialized == null) {
            null
        } else {
            val parts = serialized.split("-")
            require(parts.size == 2) {
                "Position has an unexpected format: $serialized"
            }
            Position(pointDeserializer.deserialize(parts[0]), pointDeserializer.deserialize(parts[1]))
        }
    }
    jsonSerialization.unavailableReferenceTargetPolicy = UnavailableNodePolicy.PROXY_NODES
    jsonSerialization.registerDeserializersForRPGLanguage()

    val ast = jsonSerialization.deserializeToNodes(astFile).first() as CompilationUnit
    println(ast)
}

package io.lionweb.lioncore.kotlin.repoclient

interface NodeIDTranslator {
    fun toTranslatedNodeID(originalNodeID: String) : String
    fun toOriginalNodeID(translatedNodeID: String) : String
}

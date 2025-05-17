import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.LionCoreBuiltins
import io.lionweb.lioncore.java.serialization.SerializationProvider
import io.lionweb.lioncore.kotlin.Multiplicity
import io.lionweb.lioncore.kotlin.createConcept
import io.lionweb.lioncore.kotlin.createContainment
import io.lionweb.lioncore.kotlin.lwLanguage
import java.io.File

val propertiesPartition: Concept
val propertiesFile: Concept
val property: Concept
val propertiesLanguage =
    lwLanguage("Properties").apply {
        propertiesPartition = createConcept("PropertiesPartition")
        propertiesFile = createConcept("PropertiesFile")
        property = createConcept("Property")

        propertiesPartition.isPartition = true
        propertiesPartition.createContainment("files", propertiesFile, Multiplicity.ZERO_TO_MANY)
        propertiesFile.createContainment("properties", property, Multiplicity.ZERO_TO_MANY)
        property.addImplementedInterface(LionCoreBuiltins.getINamed())
    }

fun main(args: Array<String>) {
    // Save the language to file
    File(
        "properties-language.json",
    ).writeText(SerializationProvider.getStandardJsonSerialization().serializeTreesToJsonString(propertiesLanguage))
}

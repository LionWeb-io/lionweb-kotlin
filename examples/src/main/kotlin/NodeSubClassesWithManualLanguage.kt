import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.LionCoreBuiltins
import io.lionweb.lioncore.java.model.impl.DynamicNode
import io.lionweb.lioncore.java.serialization.SerializationProvider
import io.lionweb.lioncore.kotlin.MetamodelRegistry
import io.lionweb.lioncore.kotlin.MetamodelRegistry.registerMapping
import io.lionweb.lioncore.kotlin.Multiplicity
import io.lionweb.lioncore.kotlin.createConcept
import io.lionweb.lioncore.kotlin.createContainment
import io.lionweb.lioncore.kotlin.createProperty
import io.lionweb.lioncore.kotlin.lwLanguage
import kotlin.test.assertTrue

fun main(args: Array<String>) {
    val tenantConcept: Concept
    val userConcept: Concept
    val fileConcept: Concept
    val directoryConcept: Concept
    val textFileConcept: Concept
    val organizationLanguage =
        lwLanguage("Organization").apply {
            tenantConcept = createConcept("Tenant").apply {
                isPartition = true
                addImplementedInterface(LionCoreBuiltins.getINamed())
            }
            userConcept = createConcept("User").apply {
                addImplementedInterface(LionCoreBuiltins.getINamed())
                createProperty("password", LionCoreBuiltins.getString())
            }
            fileConcept = createConcept("File").apply {
                isAbstract = true
                addImplementedInterface(LionCoreBuiltins.getINamed())
            }
            directoryConcept = createConcept("Directory").apply {
                extendedConcept = fileConcept
            }
            textFileConcept = createConcept("TextFile").apply {
                extendedConcept = fileConcept
                createProperty("contents", LionCoreBuiltins.getString())
            }

            tenantConcept.createContainment("users", userConcept, Multiplicity.ZERO_TO_MANY)
            tenantConcept.createContainment("directories", directoryConcept, Multiplicity.ZERO_TO_MANY)

            directoryConcept.createContainment("files", fileConcept, Multiplicity.ZERO_TO_MANY)
        }

    registerMapping(Tenant::class, tenantConcept)
    registerMapping(User::class, userConcept)
    registerMapping(File::class, fileConcept)
    registerMapping(Directory::class, directoryConcept)
    registerMapping(TextFile::class, textFileConcept)

    val tenant1 = Tenant().apply {
        name = "My Tenant"
        users.add(User().apply {
            name = "Gino"
            password = "FerraraBiciclette87"
        })
        directories.add(Directory().apply {
            name = "root"
            files.add(TextFile().apply {
                name = "foo.json"
                contents = "{}"
            })
        })
    }

    val jsonSerialization = SerializationProvider.getStandardJsonSerialization()
    val tenant1Serialized = jsonSerialization.serializeTreeToJsonString(tenant1)

    jsonSerialization.enableDynamicNodes()
    var deserializedTenant1 = jsonSerialization.deserializeToNodes(tenant1Serialized).first()
    assertTrue(deserializedTenant1 is DynamicNode)

    MetamodelRegistry.prepareJsonSerialization(jsonSerialization)

    deserializedTenant1 = jsonSerialization.deserializeToNodes(tenant1Serialized).first()
    assertTrue(deserializedTenant1 is Tenant)
}
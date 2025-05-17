import io.lionweb.lioncore.java.model.impl.DynamicNode
import io.lionweb.lioncore.java.serialization.SerializationProvider
import io.lionweb.lioncore.kotlin.MetamodelRegistry
import io.lionweb.lioncore.kotlin.lwLanguage
import kotlin.test.assertTrue

fun main(args: Array<String>) {
    val organizationLanguage =
        lwLanguage(
            "Organization",
            Tenant::class,
            User::class,
            File::class,
            Directory::class,
            TextFile::class,
        )
    val tenantConcept = organizationLanguage.getConceptByName("Tenant")!!
    val userConcept = organizationLanguage.getConceptByName("User")!!
    val fileConcept = organizationLanguage.getConceptByName("File")!!
    val directoryConcept = organizationLanguage.getConceptByName("Directory")!!
    val textFileConcept = organizationLanguage.getConceptByName("TextFile")!!

    val tenant1 =
        Tenant().apply {
            name = "My Tenant"
            users.add(
                User().apply {
                    name = "Gino"
                    password = "FerraraBiciclette87"
                },
            )
            directories.add(
                Directory().apply {
                    name = "root"
                    files.add(
                        TextFile().apply {
                            name = "foo.json"
                            contents = "{}"
                        },
                    )
                },
            )
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

import io.lionweb.lioncore.kotlin.BaseNode
import io.lionweb.lioncore.kotlin.Implementation

interface Named {
    val name: String?
}

class Tenant : BaseNode(), Named {
    override var name: String? by property("name")
    val users = multipleContainment<User>("users")
    val directories = multipleContainment<Directory>("directories")

    override fun calculateID(): String? = "tenant-${name!!}"
}

class User : BaseNode(), Named {
    // Note that this means users should be unique across all tenants
    override fun calculateID(): String? = "user-${name!!}"

    override var name: String? by property("name")

    var password: String? by property("password")
}

abstract class File : BaseNode(), Named {
    override var name: String? by property("name")

    override fun calculateID(): String {
        val base =
            if (parent == null) {
                "ROOT_"
            } else {
                parent.id!!
            }
        return "${base}___${(name ?: throw IllegalStateException("Cannot calculate ID if name is not set")).replace('.', '_')}"
    }

    @Implementation
    val path: String
        get() {
            return if (this.parent is File) {
                "${(parent as File).path}/$name!!"
            } else {
                name!!
            }
        }
}

class Directory(id: String? = null) : File() {
    init {
        this.id = id
    }

    val files = multipleContainment<File>("files")
}

class TextFile() : File() {
    var contents: String? by property("contents")

    @Implementation
    val numberOfLines: Int?
        get() = contents?.lines()?.size
}

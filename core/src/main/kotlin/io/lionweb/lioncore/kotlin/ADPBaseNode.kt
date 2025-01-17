package io.lionweb.lioncore.kotlin

import io.lionweb.lioncore.java.language.Annotation
import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.Containment
import io.lionweb.lioncore.java.language.Property
import io.lionweb.lioncore.java.language.Reference
import io.lionweb.lioncore.java.model.AnnotationInstance
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.ReferenceValue
import io.lionweb.lioncore.java.model.impl.ProxyNode
import kotlin.reflect.KClass

/**
 * Auto Deproxifiable BaseNode
 */
abstract class ADPBaseNode : Node {
    private sealed class State
    private data class Proxying(val id: String) : State()
    private class Actual() : State() {
        val propertyValues = mutableMapOf<String, Any?>()
        val containmentValues = mutableMapOf<String, List<Node>>()
        val referenceValues = mutableMapOf<String, List<ReferenceValue>>()
    }

    private lateinit var state : State

    companion object {
        val threadLocalContext = ThreadLocal<DataRetriever>()
        var dataRetriever: DataRetriever
            set(value) {
                threadLocalContext.set(value)
            }
            get() {
                return threadLocalContext.get() ?: throw IllegalStateException("DataRetriever not set")
            }
    }

    constructor(id: String) {
        state = Proxying(id)
    }


    constructor() {
        // Auto derive concept
        state = Actual()
    }

    override fun getPropertyValue(p0: Property): Any? {
        return ensureIsDeprofixied {
            require(it.propertyValues.containsKey(p0.name)) {
                "Value for property ${p0.name} not found"
            }
            it.propertyValues[p0.name]
        }
    }

    override fun setPropertyValue(p0: Property, value: Any?) {
        ensureIsDeprofixied {
            require(it.propertyValues.containsKey(p0.name)) {
                "Value for property ${p0.name} not found"
            }
            it.propertyValues[p0.name!!] = value
        }
    }

    override fun getChildren(p0: Containment): MutableList<out Node> {
        TODO("Not yet implemented")
    }

    override fun addChild(p0: Containment, p1: Node) {
        TODO("Not yet implemented")
    }

    override fun removeChild(p0: Node) {
        TODO("Not yet implemented")
    }

    override fun removeChild(p0: Containment, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun getReferenceValues(p0: Reference): MutableList<ReferenceValue> {
        TODO("Not yet implemented")
    }

    override fun addReferenceValue(p0: Reference, p1: ReferenceValue?) {
        TODO("Not yet implemented")
    }

    override fun removeReferenceValue(p0: Reference, p1: ReferenceValue?) {
        TODO("Not yet implemented")
    }

    override fun removeReferenceValue(p0: Reference, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun setReferenceValues(p0: Reference, p1: MutableList<out ReferenceValue>) {
        TODO("Not yet implemented")
    }

    override fun getAnnotations(): MutableList<AnnotationInstance> {
        TODO("Not yet implemented")
    }

    override fun getAnnotations(p0: Annotation): MutableList<AnnotationInstance> {
        TODO("Not yet implemented")
    }

    override fun addAnnotation(p0: AnnotationInstance) {
        TODO("Not yet implemented")
    }

    override fun removeAnnotation(p0: AnnotationInstance) {
        TODO("Not yet implemented")
    }

    override fun getID(): String? {
        TODO("Not yet implemented")
    }

    override fun getClassifier(): Concept {
        return MetamodelRegistry.getConcept(this.javaClass.kotlin) ?: throw IllegalStateException("Concept not registered")
    }

    override fun getParent(): Node {
        TODO("Not yet implemented")
    }

    override fun getContainmentFeature(): Containment {
        TODO("Not yet implemented")
    }

    private fun <E>ensureIsDeprofixied(actualProcessor:(actual: Actual) -> E) : E{
        if (this.state is Proxying) {
            dataRetriever.populate((this.state as Proxying).id) { propertyValues ->
                this.state = Actual().apply {
                    this.propertyValues.putAll(propertyValues)
                }
            }
        }
        return actualProcessor.invoke(this.state as Actual)
    }
}

interface DataRetriever {
    fun populate(id:String, receiver:(propertyValues:Map<String, Any?>)->Unit)
}

class MyNode : ADPBaseNode {
    var value: Int? by property("value")

    companion object {
        fun createProxy(id: String) : MyNode {
            return MyNode(id)
        }
    }

    private constructor(id: String) : super(id) {

    }
}

fun <E : ADPBaseNode>KClass<E>.createProxy(id: String) : E {
    val constructor = this.constructors.find { it.parameters.size == 1 && it.parameters.first().name == "id" }
        ?: throw IllegalStateException("constructor not found")
    return constructor.callBy(mapOf(constructor.parameters.find { it.name == "id" }!! to id))
}

val myLanguage = lwLanguage("MyLanguage", MyNode::class)

fun main(args: Array<String>) {
    ADPBaseNode.dataRetriever = object : DataRetriever {
        override fun populate(id: String, receiver: (properties: Map<String, Any?>) -> Unit) {
            when (id) {
                "id-123" -> receiver.invoke(mapOf(
                    "value" to 1
                ))
                else -> TODO("Not yet implemented")
            }
        }
    }
    val n1 = MyNode.createProxy("id-123")
    println(n1.value)
    n1.value = 3
    println(n1.value)
}


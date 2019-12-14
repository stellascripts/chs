package chs.mesh

import chs.*
import org.joml.Matrix4f
import org.joml.Matrix4fStack
import org.joml.Matrix4fc
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class Node: Renderable {
    companion object {
        private val EMPTY_PRIMITIVES = emptyList<Primitive>()
        private val EMPTY_PATH = IntArray(0)
        var MAX_DEPTH = 32
            set(v) {
                field = v
                MATRIX_STACK = Matrix4fStack(v)
            }
        private var MATRIX_STACK = Matrix4fStack(32)
    }

    var name: String = "<unnamed node>"
    val transform = Transform()
    private val parentRef = NodeRef()

    val parent: Node? by parentRef
    val children: List<Node> = ArrayList()

    private var root: Node = this
    private var path = EMPTY_PATH

    private var primitives: List<Primitive> = emptyList()

    var material: RenderMaterial? = null

    private fun setParent(parent: Node?) {
        if(parent == null) {
            this.root = this
            this.path = EMPTY_PATH
        } else {
            this.root = parent.root
            this.path = parent.path.copyOf(parent.path.size+1)
            this.path[path.lastIndex] = parent.children.size-1
        }
        parentRef.set(parent)
    }

    fun add(node: Node) {
        children as MutableList<Node>
        children.add(node)
        node.setParent(this)
    }

    fun add(primitive: Primitive) {
        if(primitives === EMPTY_PRIMITIVES) primitives = ArrayList()
        val p = primitives as MutableList<Primitive>
        p.add(primitive)
    }

    fun remove(node: Node) {
        children as MutableList<Node>
        children.remove(node)
        node.setParent(null)
    }

    fun removeFromParent() {
        parent?.remove(this)
    }

    fun checkForCircularDependency(set: MutableSet<Node> = HashSet()) {
        if(!set.add(this)) error("Circular dependency in node tree: ${set.joinToString("->")}->$this")
        for(child in children) {
            child.checkForCircularDependency(set)
        }
    }

    private fun draw(lights: Lighting, camera: Camera, phase: RenderPhase, stack: Matrix4fStack, matIn: RenderMaterial?) {
        val mat = material?:matIn?: error("No material for $name")
        stack.pushMatrix()
        transform.appendTo(stack)
        for(pass in mat.passes(phase)) {
            pass.use(stack, camera)
            lights.use(pass) {
                for (prim in primitives) {
                    prim.draw()
                }
            }
        }
        for(child in children) {
            child.draw(lights, camera, phase, stack, mat)
        }
        stack.popMatrix()
    }

    override fun draw(lights: Lighting, camera: Camera, phase: RenderPhase) = draw(lights, camera, phase, MATRIX_STACK, null)

    override fun toString(): String = name

    private class NodeRef {
        private var ref: Node? = null
        operator fun getValue(owner: Any, prop: KProperty<*>) = ref
        fun set(node: Node?) { ref = node }
    }
}
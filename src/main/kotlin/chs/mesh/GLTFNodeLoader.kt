package chs.mesh

import chs.Assets
import chs.Transform
import chs.V_ONE
import chs.V_ZERO
import chs.gltf.GLTF
import chs.gltf.JSONUtils
import org.joml.Matrix4f
import org.joml.Vector4f
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.lwjgl.BufferUtils
import java.io.InputStream
import java.nio.ByteBuffer

class GLTFNodeLoader: Assets.ComplexLoader {
    override fun handles(location: String): Boolean = location.endsWith(".gltf")

    private fun loadBufferView(json: JSONObject, buf: ByteBuffer): BufferView = with(JSONUtils) {
        BufferView(
            buf,
            offset = json["byteOffset"]?.toInt()?:0,
            length = json["byteLength"]?.toInt()?:0,
            stride = json["byteStride"]?.toInt()?:0
        )
    }

    private fun loadAccessor(json: JSONObject, views: List<BufferView>): Accessor = with(JSONUtils) {
        Accessor(
            views[json["bufferView"]?.toInt()?: error("No buffer view for accessor")],
            offset = json["byteOffset"]?.toInt()?:0,
            componentType = json["componentType"]?.toIntEnum<GLTF.ComponentType>()?:required("componentType"),
            normalized = (json["normalized"] as Boolean?)?:false,
            count = json["count"]?.toInt()?:required("count"),
            type = json["type"]?.toTextEnum<GLTF.Type>()?:required("type")
        )
    }

    private fun loadMesh(json: JSONObject, accessors: List<Accessor>): List<Primitive> = with(JSONUtils) {
        return json["primitives"]!!.toArray().collect { prim ->
            Primitive(
                prim["attributes"]!!.toObject().toMap().mapValues { accessors[it.value!!.toInt()] } as Map<String, Accessor>,
                prim["indices"]?.toInt()?.let { accessors[it] },
                prim["mode"]?.toIntEnum<GLTF.RenderMode>() ?: GLTF.RenderMode.TRIANGLES
            )
        }
    }

    val Q_ID = Vector4f(0f,0f,0f,1f)

    private class NodeData(val node: Node, val mesh: List<Primitive>?, val children: IntArray?)

    private fun loadNode(json: JSONObject, meshes: List<List<Primitive>>) = with(JSONUtils) {
        val marray = json["matrix"]?.toFloats()
        val children = json["children"]?.toInts()
        val mesh = json["mesh"]?.toInt()?.let { meshes[it] }
        val node = Node()
        node.name = json["name"]?.toString()?:"unnamed node"
        if(marray != null) {
            val mat4 = Matrix4f()
            mat4.set(marray)
            mat4.getTranslation(node.transform.position)
            mat4.getNormalizedRotation(node.transform.rotation)
            mat4.getScale(node.transform.anisotropicScale)
        } else {
            val translation = json["translation"]?.toVector3()?: V_ZERO
            val rotation = json["rotation"]?.toVector4()?: Q_ID
            val scale = json["scale"]?.toVector3()?: V_ONE
            node.transform.position.set(translation)
            node.transform.rotation.set(rotation.x(), rotation.y(), rotation.z(), rotation.w())
            node.transform.anisotropicScale.set(scale)
        }
        NodeData(node, mesh, children)
    }

    override fun load(location: String): Any {
        return with(JSONUtils) {
            val buf = Assets.stream(location.replace(".gltf",".bin")).use {
                val arr = it.readAllBytes()
                BufferUtils.createByteBuffer(arr.size).put(arr)
            }
            val json = Assets.read(location).use { reader ->
                JSONParser().parse(reader) as JSONObject
            }
            val views = json["bufferViews"]?.toArray()?.collect { loadBufferView(it, buf) }?: emptyList()
            val accessors = json["accessors"]?.toArray()?.collect { loadAccessor(it, views) }?: emptyList()
            val meshes = json["meshes"]?.toArray()?.collect { loadMesh(it, accessors) }?: emptyList()
            val nodeData = json["nodes"]?.toArray()?.collect { loadNode(it, meshes) } ?: emptyList()
            nodeData.map { data ->
                if(data.mesh != null) {
                    for(p in data.mesh) data.node.add(p)
                }
                if(data.children != null) {
                    for(c in data.children) {
                        val child = nodeData[c].node
                        data.node.add(child)
                    }
                }
                data.node
            }
        }
    }
}
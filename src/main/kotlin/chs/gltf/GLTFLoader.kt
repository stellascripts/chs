package chs.gltf

import chs.Assets
import chs.V4_ONE
import chs.V_ONE
import chs.V_ZERO
import org.joml.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.InputStream

class GLTFLoader: Assets.Loader {
    val SUPPORTED_MAJOR = 2
    val SUPPORTED_MINOR = 0

    private val Q_ID = Vector4f(0f,0f,0f,1f)

    private fun checkVersion(version: String) {
        when(version) {
            "2.0" -> Unit
            else -> error("Unsupported glTF version: $version")
        }
    }

    private fun <T: GLTF.Data> T.data(json: JSONObject): T {
        name = json["name"]?.toString()?:""
        extensions = json["extensions"]
        extras = json["extras"]
        return this
    }

    private fun readAccessor(json: JSONObject): GLTF.Accessor = with(JSONUtils) {
        val comp = GLTF.ComponentType.fromCode(
            json["componentType"]?.toInt()?:required("componentType")
        )
        if(json["sparse"] != null) TODO("Sparse accessors not yet supported.")
        GLTF.Accessor(
            bufferView = json["bufferView"]?.toInt()?:-1,
            byteOffset = json["byteOffset"]?.toInt()?:0,
            componentType = comp,
            count = json["count"]?.toInt()?:required("count"),
            type = GLTF.Type.valueOf(json["type"]?.toString()?:required("type")),
            min = json["min"]?.toNumArray(comp),
            max = json["max"]?.toNumArray(comp),
            sparse = null
        ).data(json)
    }

    private fun readAnimSampler(json: JSONObject) : GLTF.Animation.Sampler = with(JSONUtils) {

        GLTF.Animation.Sampler(
            input = json["input"]?.toInt()?:required("input"),
            interpolation = json["interpolation"]?.toTextEnum<GLTF.Interpolation>()
                ?:GLTF.Interpolation.Linear,
            output = json["output"]?.toInt()?:required("output")
        ).data(json)
    }

    private fun readBuffer(json: JSONObject): GLTF.Buffer = with(JSONUtils) {
        GLTF.Buffer(
            uri = json["uri"]?.toString(),
            byteLength = json["byteLength"]?.toInt()?:required("byteLength")
        ).data(json)
    }

    private fun readBufferView(json: JSONObject) : GLTF.BufferView = with(JSONUtils) {
        GLTF.BufferView(
            buffer = json["buffer"]?.toInt()?:required("buffer") ,
            byteOffset = json["byteOffset"]?.toInt()?:0,
            byteLength = json["byteLength"]?.toInt()?:required("byteLength"),
            byteStride = json["byteStride"]?.toInt()?:0,
            target = json["target"]?.toIntEnum<GLTF.BufferTarget>()
        ).data(json)
    }

    private fun readChannel(json: JSONObject) : GLTF.Channel = with(JSONUtils) {
        val target = json["target"]?.toObject()?:required("target")
        GLTF.Channel(
            sampler = json["sampler"]?.toInt()?:required("sampler"),
            targetNode = target["node"]?.toInt()?:required("target.node"),
            targetPath = target["path"]?.toTextEnum<GLTF.TargetPath>()?:required("target.path")
        ).data(json)
    }

    private fun readImage(json: JSONObject): GLTF.Image = with(JSONUtils) {
        GLTF.Image(
            uri = json["uri"]?.toString(),
            mimeType = json["mimeType"]?.toTextEnum<GLTF.ImageType>(),
            bufferView = json["bufferView"]?.toInt()
        ).data(json)
    }

    private fun readAnimation(json: JSONObject): GLTF.Animation = with(JSONUtils) {
        val channels = json["channels"]?.toArray()?.collect { readChannel(it) }?:required("channels")
        val samplers = json["samplers"]?.toArray()?.collect { readAnimSampler(it) }?:required("samplers")
        GLTF.Animation(channels, samplers).data(json)
    }

    private fun readPBR(json: JSONObject): GLTF.PBR = with(JSONUtils) {
        GLTF.PBR(
            baseColorFactor = json["baseColorFactor"]?.toVector4()?: V4_ONE,
            baseColorTexture = json["baseColorTexture"]?.toObject()?.let { readTextureInfo(it) },
            metallicFactor = json["metallicFactor"]?.toFloat()?:1.0f,
            roughnessFactor = json["roughnessFactor"]?.toFloat()?:1.0f,
            metallicRoughnessTexture = json["metallicRoughnessTexture"]?.toObject()?.let { readTextureInfo(it) }
        ).data(json)
    }

    private fun readTextureInfo(json: JSONObject, scaleName: String? = null): GLTF.TextureInfo = with(JSONUtils) {
        GLTF.TextureInfo(
            index = json["index"]?.toInt()?:required("index"),
            texCoord = json["texCoord"]?.toInt()?:0,
            scale = if(scaleName != null) json[scaleName]?.toFloat()?:1.0f else 1.0f
        ).data(json)
    }

    private fun readMaterial(json: JSONObject): GLTF.Material = with(JSONUtils) {
        GLTF.Material(
            pbr = json["pbrMetallicRoughness"]?.toObject()?.let { readPBR(it) },
            normalTexture = json["normalTexture"]?.toObject()?.let { readTextureInfo(it,"scale") },
            occlusionTexture = json["occlusionTexture"]?.toObject()?.let { readTextureInfo(it, "strength") },
            emissiveTexture = json["emissiveTexture"]?.toObject()?.let { readTextureInfo(it) },
            emissiveFactor = json["emissiveFactor"]?.toVector3()?: V_ZERO,
            alphaMode = json["alphaMode"]?.toTextEnum<GLTF.AlphaMode>()?:GLTF.AlphaMode.OPAQUE,
            alphaCutoff = json["alphaCutoff"]?.toFloat()?:0.5f,
            doubleSided = (json["doubleSided"] as Boolean?)?:false
        ).data(json)
    }

    private fun readPrimitive(json: JSONObject): GLTF.Primitive = with(JSONUtils) {
        GLTF.Primitive(
            attributes = json["attributes"]?.toObject()?.toMap() as Map<String, Ref<GLTF.Accessor>>,
            indices = json["indices"]?.toInt(),
            material = json["material"]?.toInt(),
            mode = json["mode"]?.toIntEnum<GLTF.RenderMode>()?:GLTF.RenderMode.TRIANGLES,
            targets = json["targets"]?.toArray()?.collect { it.toMap() as Map<String, Ref<GLTF.Accessor>> }
        ).data(json)
    }

    private fun readMesh(json: JSONObject): GLTF.Mesh = with(JSONUtils) {
        GLTF.Mesh(
            primitives = json["primitives"]?.toArray()?.collect { readPrimitive(it) }?:required("primitives"),
            weights = json["weights"]?.toFloats()
        ).data(json)
    }

    private fun readNode(json: JSONObject): GLTF.Node = with(JSONUtils) {
        val marray = json["matrix"]?.toFloats()
        val node = GLTF.Node(
            children = json["children"]?.toInts(),
            skin = json["skin"]?.toInt(),
            mesh = json["mesh"]?.toInt(),
            weights = json["weights"]?.toFloats()
        )
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
        node.data(json)
    }

    private fun readSampler(json: JSONObject): GLTF.Sampler = with(JSONUtils) {
        GLTF.Sampler(
            magFilter = json["magFilter"]?.toIntEnum<GLTF.Filter>(),
            minFilter = json["minFilter"]?.toIntEnum<GLTF.Filter>(),
            wrapS = json["wrapS"]?.toIntEnum<GLTF.WrapMode>()?:GLTF.WrapMode.REPEAT,
            wrapT = json["wrapT"]?.toIntEnum<GLTF.WrapMode>()?:GLTF.WrapMode.REPEAT
        ).data(json)
    }

    private fun readScene(json: JSONObject): GLTF.Scene = with(JSONUtils) {
        return GLTF.Scene(json["nodes"]?.toInts()).data(json)
    }

    private fun readSkin(json: JSONObject): GLTF.Skin = with(JSONUtils) {
        GLTF.Skin(
            inverseBindMatrices = json["inverseBindMatrices"]?.toInt(),
            skeleton = json["skeleton"]?.toInt(),
            joints = json["joints"]?.toInts()?: required("joints")
        ).data(json)
    }

    private fun readTexture(json: JSONObject): GLTF.Texture = with(JSONUtils) {
        GLTF.Texture(
            sampler = json["sampler"]?.toInt(),
            source = json["source"]?.toInt()
        ).data(json)
    }

    private fun read(json: JSONObject): GLTF = with(JSONUtils) {
        val asset = json["asset"] as? JSONObject ?: error("Invalid glTF file.")
        val version = asset["minVersion"]?.toString()
            ?: asset["version"]?.toString()
            ?: error("No version specified.")
        checkVersion(version)

        GLTF(
            accessors = json["accessors"]?.toArray()?.collect { readAccessor(it) } ?: emptyList(),
            animations = json["animations"]?.toArray()?.collect { readAnimation(it) } ?: emptyList(),
            buffers = json["buffers"]?.toArray()?.collect { readBuffer(it) } ?: emptyList(),
            bufferViews = json["bufferViews"]?.toArray()?.collect { readBufferView(it) } ?: emptyList(),
            images = json["images"]?.toArray()?.collect { readImage(it) } ?: emptyList(),
            materials = json["materials"]?.toArray()?.collect { readMaterial(it) } ?: emptyList(),
            meshes = json["meshes"]?.toArray()?.collect { readMesh(it) } ?: emptyList(),
            nodes = json["nodes"]?.toArray()?.collect { readNode(it) } ?: emptyList(),
            samplers = json["samplers"]?.toArray()?.collect { readSampler(it) } ?: emptyList(),
            scenes = json["scenes"]?.toArray()?.collect { readScene(it) } ?: emptyList(),
            skins = json["skins"]?.toArray()?.collect { readSkin(it) } ?: emptyList(),
            textures = json["textures"]?.toArray()?.collect { readTexture(it) } ?: emptyList(),

            extensions = json["extensions"],
            extensionsUsed = json["extensionsUsed"]?.toArray()?.collect { it.toString() }?: emptyList(),
            extensionsRequired = json["extensionsRequired"]?.toArray()?.collect { it.toString() }?: emptyList(),
            extras = json["extras"],
            scene = json["scene"]?.toInt()
        )
    }

    override fun handles(location: String): Boolean = location.endsWith(".gltf")

    override fun load(stream: InputStream): Any = with(JSONUtils) {
        val json = JSONParser().parse(stream.reader())?.toObject()
            ?: error("There does not seem to be any json in this file.")
        read(json)
    }
}
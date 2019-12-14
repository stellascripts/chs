package chs.gltf

import chs.Transform
import org.joml.Matrix4fc
import org.joml.Vector3fc
import org.joml.Vector4fc

typealias RefArray<T> = IntArray
typealias Ref<T> = Int
typealias NumArray = Any

data class GLTF(
    val accessors: List<Accessor>,
    val animations: List<Animation>,
    val buffers: List<Buffer>,
    val bufferViews: List<BufferView>,
    val images: List<Image>,
    val materials: List<Material>,
    val meshes: List<Mesh>,
    val nodes: List<Node>,
    val samplers: List<Sampler>,
    val scenes: List<Scene>,
    val skins: List<Skin>,
    val textures: List<Texture>,

    val extensions: Any?,
    val extensionsUsed: List<String>,
    val extensionsRequired: List<String>,
    val extras: Any?,
    val scene: Ref<Scene>?
) {
    companion object {
        const val UNDEFINED = -1
    }

    interface IntEnum {
        val code: Int
    }

    interface TextEnum {
        val text: String
    }

    enum class ComponentType(override val code: Int, val bytes: Int): IntEnum {
        BYTE(5120, 1),
        UNSIGNED_BYTE(5121, 1),
        SHORT(5122, 2),
        UNSIGNED_SHORT(5123, 2),
        UNSIGNED_INT(5125, 4),
        FLOAT(5126, 4);

        companion object {
            fun fromCode(code: Int) = when(code) {
                5120 -> BYTE
                5121 -> UNSIGNED_BYTE
                5122 -> SHORT
                5123 -> UNSIGNED_SHORT
                5125 -> UNSIGNED_INT
                5126 -> FLOAT
                else -> error("Unsupported component type: $code")
            }
        }
    }

    enum class Type(val numComponents: Int): TextEnum {
        SCALAR(1),
        VEC2(2),
        VEC3(3),
        VEC4(4),
        MAT2(4),
        MAT3(9),
        MAT4(16);
        override val text: String get() = name
    }

    enum class Interpolation: TextEnum {
        Linear,
        Step,
        CubicSpline;
        override val text: String get() = name.toUpperCase()
    }

    enum class BufferTarget(override val code: Int): IntEnum {
        ARRAY_BUFFER(34962),
        ELEMENT_ARRAY_BUFFER(34963)
    }

    enum class TargetPath: TextEnum {
        Translation,
        Rotation,
        Scale,
        Weights;
        override val text: String get() = name.toLowerCase()
    }

    enum class ImageType(override val text: String): TextEnum {
        JPEG("image/jpeg"),
        PNG("image/png");
    }

    enum class AlphaMode: TextEnum {
        OPAQUE,
        MASK,
        BLEND;
        override val text: String get() = name
    }

    enum class RenderMode(override val code: Int): IntEnum {
        POINTS(0),
        LINES(1),
        LINE_LOOP(2),
        LINE_STRIP(3),
        TRIANGLES(4),
        TRIANGLE_STRIP(5),
        TRIANGLE_FAN(6)
    }

    enum class Filter(override val code: Int): IntEnum {
        NEAREST(9728),
        LINEAR(9729),
        NEAREST_MIPMAP_NEAREST(9984),
        LINEAR_MIPMAP_NEAREST(9985),
        NEAREST_MIPMAP_LINEAR(9986),
        LINEAR_MIPMAP_LINEAR(9987)
    }

    enum class WrapMode(override val code: Int): IntEnum  {
        CLAMP_TO_EDGE(33071),
        MIRRORED_REPEAT(33648),
        REPEAT(10497)
    }

    abstract class Data {
        var name: String = ""
        var extensions: Any? = null
        var extras: Any? = null
    }

    data class Accessor(
        val bufferView: Ref<BufferView>,
        val byteOffset: Int,
        val componentType: ComponentType,
        val count: Int,
        val type: Type,
        val min: NumArray?,
        val max: NumArray?,
        val sparse: Any?
    ) : Data()

    data class Animation(
        val channels: List<Channel>,
        val samplers: List<Sampler>
    ): Data() {
        class Sampler(
            val input: Ref<Accessor>,
            val interpolation: Interpolation,
            val output: Ref<Accessor>
        ): Data()
    }

    data class Buffer(
        val uri: String?,
        val byteLength: Int
    ): Data()

    data class BufferView(
        val buffer: Int,
        val byteOffset: Int,
        val byteLength: Int,
        val byteStride: Int,
        val target: BufferTarget?
    ): Data()

    data class Channel (
        val sampler: Ref<Animation.Sampler>,
        val targetNode: Ref<Node>,
        val targetPath: TargetPath
    ): Data()

    data class Image(
        val uri: String?,
        val mimeType: ImageType?,
        val bufferView: Ref<BufferView>?
    ): Data()

    data class SparseIndices(
        val bufferView: Ref<BufferView>,
        val byteOffset: Int,
        val componentType: ComponentType
    ): Data()

    data class Material(
        val pbr: PBR?,
        val normalTexture: TextureInfo?,
        val occlusionTexture: TextureInfo?,
        val emissiveTexture: TextureInfo?,
        val emissiveFactor: Vector3fc?,
        val alphaMode: AlphaMode,
        val alphaCutoff: Float,
        val doubleSided: Boolean
    ): Data()

    data class TextureInfo(
        val index: Ref<Texture>,
        val texCoord: Int,
        val scale: Float = 1.0f
    ): Data()

    data class PBR(
        val baseColorFactor: Vector4fc,
        val baseColorTexture: TextureInfo?,
        val metallicFactor: Float,
        val roughnessFactor: Float,
        val metallicRoughnessTexture: TextureInfo?
    ): Data()

    data class Mesh(
        val primitives: List<Primitive>,
        val weights: FloatArray?
    ): Data()

    data class Primitive(
        val attributes: Map<String, Ref<Accessor>>,
        val indices: Ref<Accessor>?,
        val material: Ref<Material>?,
        val mode: RenderMode,
        val targets: List<Map<String, Ref<Accessor>>>?
    ): Data()

    data class Node(
        val children: RefArray<Node>?,
        val skin: Ref<Skin>?,
        val mesh: Ref<Mesh>?,
        val weights: FloatArray?
    ): Data() {
        val transform = Transform()
    }

    data class Sampler(
        val magFilter: Filter?,
        val minFilter: Filter?,
        val wrapS: WrapMode,
        val wrapT: WrapMode
    ): Data()

    data class Scene(
        val nodes: RefArray<Node>?
    ): Data()

    data class Skin(
        val inverseBindMatrices: Ref<Accessor>?,
        val skeleton: Ref<Node>?,
        val joints: RefArray<Node>
    ): Data()

    data class Texture(
        val sampler: Ref<Sampler>?,
        val source: Ref<Image>?
    ): Data()
}
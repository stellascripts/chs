package chs

import org.joml.Matrix3f
import org.joml.Matrix4fc
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14.*
import java.util.*

/**
 * Blending mode used in rendering contexts.
 */
@Suppress("unused")
enum class BlendMode(
    private val mode: Int,
    private val src: Int,
    private val dst: Int
) {
    /**
     * Draws new pixels directly over old pixels.
     */
    Off(GL_FUNC_ADD, GL_ONE, GL_ZERO),
    /**
     * Adds new pixels to old pixels, giving a light or hologram effect.
     */
    Additive(GL_FUNC_ADD, GL_SRC_ALPHA, GL_ONE),
    /**
     * Adds new pixels to old pixels in a way simulating transparency.
     */
    Alpha(GL_FUNC_ADD, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    fun use() {
        glBlendEquation(mode)
        glBlendFunc(src, dst)
    }

    companion object{
        internal val parse = values().map { it.name.toLowerCase() to it }.toMap()
    }
}

/**
 * A specific ordering in which rendering takes place. Depth is drawn, then Solid, then Transparent.
 */
@Suppress("unused")
enum class RenderPhase {
    /**
     * Pre-lighting phase used to paint depth buffers and do preliminary setup.
     */
    Depth,
    /**
     * Lighting phase for solid objects.
     */
    Solid,
    /**
     * Lighting phase for partially transparent objects.
     */
    Transparent;

    companion object {
        internal val all = values().toList()
        internal val parse = all.map { it.name.toLowerCase() to it }.toMap()
    }
}

enum class DepthMode(val const: Int) {
    Less(GL_LESS),
    Less_Or_Equal(GL_LEQUAL),
    Equal(GL_EQUAL),
    Not_Equal(GL_NOTEQUAL),
    Greater_Or_Equal(GL_GEQUAL),
    Greater(GL_GREATER),
    Never(GL_NEVER),
    Always(GL_ALWAYS);

    fun set() {
        GL11.glDepthFunc(const)
    }

    companion object {
        internal val parse = values().map { it.name.toLowerCase() to it }.toMap()
    }
}

/**
 * Contains the list of shaders and rendering settings used for a particular set of objects.
 */
class RenderMaterial {
    private val passes = Array<ArrayList<RenderPass>>(RenderPhase.all.size) { ArrayList() }
    private var deleted = false

    /**
     * Obtains the list of all passes in the specified phase.
     */
    fun passes(phase: RenderPhase): List<RenderPass> =
        if(!deleted) passes[phase.ordinal]
        else throw IllegalStateException("Render Material cannot be used after deletion.")
}

/**
 * A single draw pass, made by either a single render call or a render call for each light.
 */
class RenderPass(
    /**
     * The shader used in this pass.
     */
    val shader: Shader,
    /**
     * The [BlendMode] which will be set for this pass.
     */
    var blendMode: BlendMode = BlendMode.Off,
    /**
     * True if the depth buffer is tested against when drawing, false otherwise.
     */
    var testDepth: DepthMode? = null,
    /**
     * True if the pass writes depth information.
     */
    var writeDepth: Boolean = true,

    /**
     * True if the pass writes color information.
     */
    var writeColor: Boolean = true,
    /**
     * True if the object is rendered once for each light in the scene.
     */
    var perLight: Boolean = false
) {
    /**
     * Called by the [Scene] when rendering the object.
     */
    @PublishedApi internal fun use(worldMatrix: Matrix4fc, camera: Camera) {
        blendMode.use()
        if (testDepth != null) testDepth!!.set()
        else glDepthFunc(GL_ALWAYS)
        glDepthMask(writeDepth)
        glColorMask(writeColor, writeColor, writeColor, writeColor)

        val normals = Matrix3f().set(worldMatrix).transpose().invert()

        shader.set("W", Uniforms.Matrix4f, worldMatrix)
        shader.set("V", Uniforms.Matrix4f, camera.viewMatrix)
        shader.set("P", Uniforms.Matrix4f, camera.projectionMatrix)
        shader.set("W_N", Uniforms.Matrix3f, normals)
        shader.set("in_view_position", Uniforms.Vector3f, camera.getPosition())
    }
}
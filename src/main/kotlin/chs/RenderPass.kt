package chs

import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Matrix4fc
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

    internal fun use() {
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

    /**
     * Deletes the RenderMaterial, freeing its resources.
     */
    fun delete() {
        for(phase in passes) {
            for(pass in phase) {
                pass.shader.delete()
            }
        }
        deleted = true
    }
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
    var testDepth: Boolean = true,
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
    internal fun use(world: Transform, view: Matrix4fc, projection: Matrix4fc) {
        blendMode.use()
        if (testDepth) glEnable(GL_DEPTH_TEST)
        else glDisable(GL_DEPTH_TEST)
        glDepthMask(writeDepth)
        glColorMask(writeColor, writeColor, writeColor, writeColor)

        val w = world.writeTo(Matrix4f())
        val normals = Matrix3f().set(w).transpose().invert()

        shader.set("W", Uniforms.Matrix4f, w)
        shader.set("V", Uniforms.Matrix4f, view)
        shader.set("P", Uniforms.Matrix4f, projection)
        shader.set("W_N", Uniforms.Matrix3f, normals)
    }
}
package chs

import org.joml.Vector3fc
import org.joml.Vector4f
import org.lwjgl.opengl.GL11.*

/**
 * A renderable scene containing objects and lights.
 */
class Scene {

    /**
     * An object in a scene.
     * @property render The renderable which is used with this object.
     * @property shader The shader used to render this object.
     * @property transform The spatial transformation (position, rotation and scale) of the object.
     */
    class Object(val render: Renderable, val shader: Shader, val transform: Transform)

    private val ambientColor = Vector4f(0.0f, 0.0f, 0.0f, 1f)
    private val lights = ArrayList<Light>()
    private val objects = ArrayList<Object>()

    /**
     * Set the ambient color of the scene, the color that objects receive from indirect light.
     */
    fun setAmbientColor(r: Float, g: Float, b: Float, intensity: Float) {
        ambientColor.set(r*intensity,g*intensity,b*intensity, intensity)
    }

    /**
     * Adds a [directional][Light.Type.Directional] [Light] to the scene.
     * @param direction The direction the light will shine in.
     * @param color The color of the light.
     * @param intensity The intensity of the light.
     * @return The created [Light].
     */
    fun addDirectionalLight(direction: Vector3fc, color: Color3c, intensity: Float): Light {
        val light = Light(Light.Type.Directional, direction, color, intensity)
        lights.add(light)
        return light
    }

    /**
     * Adds a [point][Light.Type.Point] [Light] to the scene.
     * @param origin The origin of the light.
     * @param color The color of the light.
     * @param intensity The intensity of the light.
     * @return The created [Light].
     */
    fun addPointLight(origin: Vector3fc, color: Color3c, intensity: Float): Light {
        val light = Light(Light.Type.Point, origin, color, intensity)
        lights.add(light)
        return light
    }

    /**
     * Adds an [Object] to the scene, by default at (0.0, 0.0, 0.0) with no rotation and 1.0 scale.
     * @param render A [Renderable] used for this object, usually a [Mesh].
     * @param shader The [Shader] to use for drawing the object.
     * @return The created [Object].
     */
    fun addObject(render: Renderable, shader: Shader): Object {
        val obj = Object(render, shader, Transform())
        objects.add(obj)
        return obj
    }

    //pass for depth info. uses zeroShader for simple rendering.
    private fun depthPass(camera: Camera) {
        glBlendFunc(GL_ONE, GL_ZERO)
        glColorMask(false, false, false, false)
        //glClear(GL_DEPTH_BUFFER_BIT)
        glDepthMask(true)
        //glDepthFunc(GL_ALWAYS)
        for(obj in objects) {
            val shader = zeroShader
            camera.computeUniforms(shader, obj.transform)
            obj.render.draw()
        }
        glColorMask(true, true, true, true)
        glDepthMask(false)
        glDepthFunc(GL_LEQUAL)
    }

    //pass drawing ambient light
    private fun ambientPass(camera: Camera) {
        for(obj in objects) {
            val shader = obj.shader
            camera.computeUniforms(shader, obj.transform)
            shader.set("in_light_color", Uniforms.Vector4f, V4_ZERO)
            shader.set("in_ambient_color", Uniforms.Vector4f, ambientColor)
            obj.render.draw()
            shader.set("in_ambient_color", Uniforms.Vector4f, V4_ZERO)
        }
    }

    //pass for each light in the scene
    private fun lightPass(camera: Camera) {
        glBlendFunc(GL_ONE, GL_ONE)
        for(light in lights) {
            val lightColor = light.getShaderColor()
            val lightPosition = light.getShaderPosition()
            for(obj in objects) {
                val shader = obj.shader
                camera.computeUniforms(shader, obj.transform)
                shader.set("in_light_color", Uniforms.Vector4f, lightColor)
                shader.set("in_light_position", Uniforms.Vector4f, lightPosition)
                obj.render.draw()
            }
        }
    }

    /**
     * Renders the scene from the perspective of the specified [Camera].
     * @param camera The camera to render the scene from.
     */
    fun render(camera: Camera) {
        depthPass(camera)
        ambientPass(camera)
        lightPass(camera)
    }

    /**
     * Deletes all the OpenGL objects associated with this scene. Calls [Renderable.delete] and [Shader.delete]
     * on objects still in the scene and clears its objects list. Lights will still remain in the scene.
     */
    @API
    fun delete() {
        for(obj in objects) {
            obj.shader.delete()
            obj.render.delete()
        }
        objects.clear()
    }

    companion object {
        //simple shader which renders objects as white silhouettes for use in depth rendering.
        //lazy initialization ensures that shader will be loaded after rendering context starts.
        private val zeroShader: Shader by lazy {
            Shader("""
                #version 130
                uniform mat4 W;
                uniform mat4 V;
                uniform mat4 P;

                in vec3 in_position;

                void main() {
                    gl_Position = P * V * W * vec4(in_position, 1.0);
                }
            """.trimIndent(), """
                #version 130
                void main() {
                    gl_FragColor = vec4(1.0);
                }
            """.trimIndent())
        }
    }
}
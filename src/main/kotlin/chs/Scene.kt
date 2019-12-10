package chs

import org.joml.Vector3fc
import org.joml.Vector4f

/**
 * A renderable scene containing objects and lights.
 */
class Scene {

    /**
     * An object in a scene.
     * @property render The renderable which is used with this object.
     * @property material The material used to render this object.
     * @property transform The spatial transformation (position, rotation and scale) of the object.
     */
    class Object(val render: Renderable, val material: RenderMaterial, val transform: Transform)

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
     * @param material The [RenderMaterial] to use for drawing the object.
     * @return The created [Object].
     */
    fun addObject(render: Renderable, material: RenderMaterial): Object {
        val obj = Object(render, material, Transform())
        objects.add(obj)
        return obj
    }

    private fun renderPhase(camera: Camera, phase: RenderPhase, obj: Object) {
        val lightPosition = Vector4f()
        val lightColor = Vector4f()
        val passes = obj.material.passes(phase)
        for(pass in passes) {
            pass.use(obj.transform, camera.viewMatrix, camera.projectionMatrix)
            if(pass.perLight) {
                var ambient = true
                for(light in lights) {
                    if(ambient) {
                        pass.shader.set("in_ambient_color", Uniforms.Vector4f, ambientColor)
                        ambient = false
                    } else {
                        pass.shader.set("in_ambient_color", Uniforms.Vector4f, V4_ZERO)
                    }
                    pass.shader.set("in_light_color", Uniforms.Vector4f,
                        light.getShaderColor(lightColor))
                    pass.shader.set("in_light_position", Uniforms.Vector4f,
                        light.getShaderPosition(lightPosition))
                    obj.render.draw()
                }
            } else {
                obj.render.draw()
            }
        }
    }

    /**
     * Renders the scene using the given camera.
     */
    fun render(camera: Camera) {
        for(phase in RenderPhase.all) {
            for (obj in objects) {
                renderPhase(camera, phase, obj)
            }
        }
    }

    /**
     * Deletes all the OpenGL objects associated with this scene. Calls [Renderable.delete] and [Shader.delete]
     * on objects still in the scene and clears its objects list. Lights will still remain in the scene.
     */
    @API
    fun delete() {
        for(obj in objects) {
            obj.material.delete()
            obj.render.delete()
        }
        objects.clear()
    }
}
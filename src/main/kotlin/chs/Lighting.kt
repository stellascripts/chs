package chs

import org.joml.Vector3fc
import org.joml.Vector4f

class Lighting: Iterable<Light> {
    @PublishedApi internal val ambientColor = Vector4f(0.0f, 0.0f, 0.0f, 1f)
    private val lights = ArrayList<Light>()

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

    @PublishedApi internal fun bindAmbient(pass: RenderPass) {
        pass.shader.set("in_ambient_color", Uniforms.Vector4f, ambientColor)
    }

    @PublishedApi internal fun bindLight(pass: RenderPass, light: Light) {
        pass.shader.set("in_light_color", Uniforms.Vector4f,
            light.getShaderColor())
        pass.shader.set("in_light_position", Uniforms.Vector4f,
            light.getShaderPosition())
    }

    inline fun use(pass: RenderPass, draw: ()->Unit) {
        if(pass.perLight) {
            var ambient = true
            for(light in this) {
                if(ambient) {
                    bindAmbient(pass)
                    ambient = false
                    BlendMode.Off.use()
                } else {
                    pass.shader.set("in_ambient_color", Uniforms.Vector4f, V4_ZERO)
                    pass.blendMode.use()
                }
                bindLight(pass, light)
                draw()
            }
        } else {
            draw()
        }
    }

    override fun iterator(): Iterator<Light> = lights.iterator()
}
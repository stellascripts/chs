package chs

import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f

/**
 * A light which illuminates a scene.
 * @property type The [Type] of light.
 * @property vector Either the position of a [Type.Point] light, or the direction of a [Type.Directional] light.
 * @property color The color of the light emitted.
 * @property intensity The intensity of the emitted light.
 */
class Light(@API var type: Type, vector: Vector3fc, color: Color3c, @API var intensity: Float) {
    /**
     * A type of light.
     */
    enum class Type {
        /**
         * Point lights emit light from a particular point in space in every direction.
         * The [Light.vector] property is used to indicate position.
         */
        Point,
        /**
         * Directional lights emit light in a particular direction, and are assumed to be
         * infinitely distant, like the sun. The [Light.vector] property is used to indicate
         * direction.
         */
        Directional
    }

    @API
    val vector: Vector3f = Vector3f().set(vector)
    @API
    val color: Color3 = Color3(color.r, color.g, color.b)

    /**
     * Acquires the in_light_color uniform property for use in [Shader]s.
     * The r, g and b components are the [color], while the alpha component is the [intensity].
     */
    fun getShaderColor(v: Vector4f = Vector4f()): Vector4f {
        return v.set(color.r, color.g, color.b, intensity)
    }

    /**
     * Acquires the in_light_position uniform property for use in [Shader]s.
     * The x, y and z components are the [vector], while the w component is 1.0 for [Type.Point] types,
     * and 0.0 for [Type.Directional] types.
     */
    fun getShaderPosition(v: Vector4f = Vector4f()): Vector4f {
        return v.set(vector, if(type == Type.Directional) 0.0f else 1.0f)
    }
}
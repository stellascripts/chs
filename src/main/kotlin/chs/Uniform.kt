package chs

import org.joml.*
import org.lwjgl.opengl.GL20.*

/**
 * A function which writes a given object to the bound shader program in LWJGL.
 */
typealias UniformType<T> = (Int, T) -> Unit

/**
 * A collection of parameters which may be used as uniform shader parameters.
 */
object Uniforms {
    /**
     * A boolean value.
     */
    @API
    val Bool: UniformType<Boolean> = { loc, v -> glUniform1i(loc, if(v) 1 else 0) }
    /**
     * A 32-bit int value.
     */
    @API
    val Int: UniformType<Int> = { loc, v -> glUniform1i(loc, v) }
    /**
     * A 32-bit floating point value.
     */
    @API
    val Float: UniformType<Float> = { loc, v -> glUniform1f(loc, v) }
    /**
     * A 3-component [vector][org.joml.Vector3f].
     */
    @API
    val Vector3f: UniformType<Vector3fc> = { loc, v -> glUniform3f(loc, v.x(), v.y(), v.z()) }
    /**
     * A 4-component [vector][org.joml.Vector4f].
     */
    @API
    val Vector4f: UniformType<Vector4fc> = { loc, v -> glUniform4f(loc, v.x(), v.y(), v.z(), v.w()) }
    /**
     * A 4x4 [matrix][org.joml.Matrix4f].
     */
    @API
    val Matrix4f: UniformType<Matrix4fc> = { loc, v ->
        withStack { stack ->
            val fb = v.get(stack.mallocFloat(16))
            glUniformMatrix4fv(loc, false, fb)
        }
    }
    /**
     * A 3x3 [matrix][org.joml.Matrix3f].
     */
    @API
    val Matrix3f: UniformType<Matrix3fc> = { loc, v ->
        withStack { stack ->
            val fb = v.get(stack.mallocFloat(9))
            glUniformMatrix3fv(loc, false, fb)
        }
    }
}


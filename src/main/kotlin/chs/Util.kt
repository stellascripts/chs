package chs

import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f
import org.joml.Vector4fc
import org.lwjgl.system.MemoryStack
import kotlin.math.abs

/**
 * Annotation for externally facing methods and properties.
 */
internal annotation class API

//null value for opengl pointers
internal const val NULL: Long = 0L

/**
 * Floating-point representation of pi for 32-bit math
 */
const val PIf: Float = kotlin.math.PI.toFloat()
/**
 * Degree-to-radian conversion factor.
 */
const val DEG_TO_RAD = PIf / 180f

//memory stack usage shortcut
/*internal inline fun <T> withStack(f: (MemoryStack)->T): T {
    return MemoryStack.stackPush().use { stack ->
        f(stack)
    }
}*/

internal inline fun <T> withStack(f: (MemoryStack)->T):T {
    val stack = MemoryStack.stackGet()
    val ptr = stack.pointer
    try {
        return f(stack)
    } finally {
        stack.pointer = ptr
    }
}

/**
 * Vector (1.0, 0.0, 0.0) corresponding to +X on the X axis.
 */
@API
val X_AXIS: Vector3fc = Vector3f(1f,0f,0f)
/**
 * Vector (0.0, 1.0, 0.0) corresponding to +Y on the Y axis.
 */
@API
val Y_AXIS: Vector3fc = Vector3f(0f,1f,0f)
/**
 * Vector (0.0, 0.0, 1.0) corresponding to +Z on the Z axis.
 */
@API
val Z_AXIS: Vector3fc = Vector3f(0f,0f,1f)
/**
 * Vector (-1.0, 0.0, 0.0) corresponding to -X on the X axis.
 */
@API
val NX_AXIS: Vector3fc = Vector3f(-1f,0f,0f)
/**
 * Vector (0.0, -1.0, 0.0) corresponding to -Y on the Y axis.
 */
@API
val NY_AXIS: Vector3fc = Vector3f(0f,-1f,0f)
/**
 * Vector (0.0, 0.0, -1.0) corresponding to -Z on the Z axis.
 */
@API
val NZ_AXIS: Vector3fc = Vector3f(0f,0f,-1f)

/**
 * Vector (1.0, 1.0, 1.0).
 */
@API
val V_ONE: Vector3fc = Vector3f(1f,1f,1f)
/**
 * Vector (0.0, 0.0, 0.0)
 */
@API
val V_ZERO: Vector3fc = Vector3f(0f,0f,0f)
/***
 * Vector (0.0, 0.0, 0.0, 0.0)
 */
@API
val V4_ZERO: Vector4fc = Vector4f(0f,0f,0f,0f)

@API
val V4_ONE: Vector4f = Vector4f(1f,1f,1f,1f)

/**
 * A very small float value, used in approximation functions.
 */
val EPSILON = Math.ulp(2.0f)

/**
 * Checks for approximate equality (difference <= EPSILON)
 */
infix fun Float.approx(f: Float) = abs(this - f) <= EPSILON

/**
 * Checks for approximate equality, componentwise (difference <= EPSILON)
 */
infix fun Vector3fc.approx(v: Vector3fc) = this.x() approx v.x() && this.y() approx v.y() && this.z() approx v.z()
package chs

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.max

/**
 * Represents a transformation in space as a position, rotation and scale.
 * @property position A [Vector3f] representing translation from the origin.
 * @property rotation A [Quaternionf] representing rotation about the translated center.
 * @property scale A floating point value representing overall scale.
 * @property anisotropicScale A [Vector3f] representing scale along each axis.
 */
class Transform {
    val position: Vector3f = Vector3f()
    val rotation: Quaternionf = Quaternionf().identity()
    var scale
        get() = max(anisotropicScale.x, max(anisotropicScale.y, anisotropicScale.z))
        set(value) { anisotropicScale.set(value) }
    val anisotropicScale: Vector3f = Vector3f(1f)

    /**
     * Writes to a [Matrix4f] such that transforming a vector by the matrix applies this transformation.
     */
    @API
    fun writeTo(m: Matrix4f): Matrix4f {
        m.translationRotateScale(position,rotation,anisotropicScale)
        return m
    }

    fun appendTo(m: Matrix4f): Matrix4f {
        m.translate(position)
        m.rotate(rotation)
        m.scale(anisotropicScale)
        return m
    }

    /**
     * Writes the inverse of this transformation to a [Matrix4f] such that transforming a vector by the matrix
     * undoes this transformation.
     */
    @API
    fun writeInverse(m: Matrix4f): Matrix4f {
        m.translationRotateScaleInvert(position,rotation,anisotropicScale)
        return m
    }

    /**
     * Applies this transform to the given vector.
     */
    @API
    fun applyPoint(v: Vector3f): Vector3f {
        return v.mul(anisotropicScale).rotate(rotation).add(position)
    }

    /**
     * Applies this transform, excluding translation, to the given vector.
     */
    @API
    fun applyDirection(v: Vector3f): Vector3f {
        return v.mul(anisotropicScale).rotate(rotation)
    }

    override fun toString(): String = "T: $position R: $rotation S: $anisotropicScale"
}

/**
 * Multiplies this matrix in place with the given [Transform] as if it were another 4x4 matrix.
 * @receiver The matrix to multiply.
 * @param t The transform to apply.
 * @return The multiplied matrix.
 */
@API
fun Matrix4f.mul(t: Transform): Matrix4f {
    return this.scale(t.anisotropicScale).rotate(t.rotation).translate(t.position)
}
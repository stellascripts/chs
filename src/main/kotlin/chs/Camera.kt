package chs

import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3fc
import java.lang.IllegalArgumentException

/**
 * Object responsible for the projection and view matrix of a scene.
 */
class Camera {
    private val projection = Matrix4f().identity()
    private val view: Matrix4f = Matrix4f().identity()
    private val position = Vector3f()
    private val aim = Vector3f(NZ_AXIS)
    private var aimIsDirection = true
    private val upAxis = Vector3f().set(Y_AXIS)
    //private lateinit var complete: FloatBuffer

    /**
     * Aims the camera along a particular direction.
     */
    @API
    fun setAimDirection(dir: Vector3fc) {
        aim.set(dir).normalize().add(position)
        aimIsDirection = true
        computeView()
    }

    /**
     * Aims the camera along a particular direction.
     */
    @API
    fun setAimDirection(x: Float, y: Float, z: Float) {
        aim.set(x,y,z).normalize().add(position)
        aimIsDirection = true
        computeView()
    }

    /**
     * Aims the camera at a particular position in space.
     */
    @API
    fun setAimPosition(pos: Vector3fc) {
        aim.set(pos)
        aimIsDirection = false
        computeView()
    }

    /**
     * Aims the camera at a particular position in space.
     */
    @API
    fun setAimPosition(x: Float, y: Float, z: Float) {
        aim.set(x,y,z)
        aimIsDirection = false
        computeView()
    }

    /**
     * Moves the camera to a position, leaving its aim unchanged. If it was aimed at a position, it will aim at that
     * position. If it was aimed in a direction, it will aim along that direction.
     */
    @API
    fun setPosition(pos: Vector3fc) {
        if(aimIsDirection) aim.sub(position)
        position.set(pos)
        if(aimIsDirection) aim.add(position)
        computeView()
    }

    /**
     * Moves the camera to a position, leaving its aim unchanged. If it was aimed at a position, it will aim at that
     * position. If it was aimed in a direction, it will aim along that direction.
     */
    @API
    fun setPosition(x: Float, y: Float, z: Float) {
        if(aimIsDirection) aim.sub(position)
        position.set(x,y,z)
        if(aimIsDirection) aim.add(position)
        computeView()
    }

    /**
     * Sets the camera's position and aim position simultaneously.
     */
    @API
    fun setPositionAndAimPosition(pos: Vector3fc, aim: Vector3fc) {
        this.position.set(pos)
        this.aim.set(aim)
        aimIsDirection = false
        computeView()
    }

    /**
     * Sets the camera's position and aim direction simultaneously.
     */
    @API
    fun setPositionAndAimDirection(pos: Vector3fc, aim: Vector3fc) {
        this.position.set(pos)
        this.aim.set(aim).add(pos)
        aimIsDirection = true
        computeView()
    }

    /**
     * Sets the camera's upward-facing axis. This defaults to Y_AXIS.
     */
    @API
    fun setUpAxis(axis: Vector3fc) {
        upAxis.set(axis).normalize()
    }

    private fun computeView() {
        view.setLookAt(position, aim, upAxis)
    }

    /**
     * Creates a projection with the specified field of view, aspect ratio and clip planes.
     * @param fieldOfView the angle from the center of the camera to the edge, in degrees.
     * @param aspectRatio the aspect ratio of the camera, usually the same as that of the screen.
     * @param nearPlane the nearest distance to the camera. Must be > 0.
     * @param farPlane the farthest distance from the camera. Must be >0.
     */
    @API
    fun createProjection(fieldOfView: Float, aspectRatio: Float, nearPlane: Float, farPlane: Float) {
        if(nearPlane <= 0f) throw IllegalArgumentException("Near plane may not be set to zero or a negative value.")
        if(farPlane <= 0f) throw IllegalArgumentException("Far plane may not be set to zero or a negative value.")
        projection.setPerspective(fieldOfView * DEG_TO_RAD, aspectRatio, nearPlane, farPlane)
    }

    /**
     * Computes the matrices for a particular shader and sets them as uniforms.
     * A preliminary step in drawing objects in a scene. Usually should not be called externally.
     */
    fun computeUniforms(shader: Shader, world: Transform) {
        val w = world.writeTo(Matrix4f())
        val normals = Matrix3f().set(w).transpose().invert()

        shader.set("W", Uniforms.Matrix4f, w)
        shader.set("V", Uniforms.Matrix4f, view)
        shader.set("P", Uniforms.Matrix4f, projection)
        shader.set("W_N", Uniforms.Matrix3f, normals)
    }
}
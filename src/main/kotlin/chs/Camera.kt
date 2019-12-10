package chs

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * Object responsible for the projection and view matrix of a scene.
 * @param fieldOfView the angle from the center of the camera to the edge, in degrees. Defaults to 35.
 * @param aspectRatio the aspect ratio of the camera, usually the same as that of the screen. Defaults to 4/3.
 * @param nearPlane the nearest distance to the camera. Must be > 0. Defaults to 0.1.
 * @param farPlane the farthest distance from the camera. Must be >0. Defaults to 100.
 */
class Camera(fieldOfView: Float = 35f, aspectRatio: Float = 4f/3f, nearPlane: Float = 0.1f, farPlane: Float = 100f) {
    private val projection = Matrix4f().setPerspective(fieldOfView, aspectRatio, nearPlane, farPlane)
    private val view: Matrix4f = Matrix4f().setLookAt(V_ZERO, NZ_AXIS, Y_AXIS)
    private val position = Vector3f(0f)
    private val aim = Vector3f(NZ_AXIS)
    private var aimIsDirection = true
    private val upAxis = Vector3f().set(Y_AXIS)

    /**
     * Projection matrix used by this camera.
     */
    val projectionMatrix: Matrix4fc get() = projection
    /**
     * View matrix used by this camera.
     */
    val viewMatrix: Matrix4fc get() = view

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
}
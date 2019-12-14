package chs

import org.joml.Matrix4f
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
    class Object(val render: ()->Unit, val material: RenderMaterial, val transform: Transform): Renderable {
        override fun draw(lights: Lighting, camera: Camera, phase: RenderPhase) {
            val world = Matrix4f()
            transform.writeTo(world)
            for(pass in material.passes(phase)) {
                pass.use(world, camera)
                lights.use(pass) { render() }
            }
        }
    }

    private val objects = ArrayList<Renderable>()
    val lighting = Lighting()

    /**
     * Adds an [Object] to the scene, by default at (0.0, 0.0, 0.0) with no rotation and 1.0 scale.
     * @param render A [Renderable] used for this object, usually a [Mesh].
     * @param material The [RenderMaterial] to use for drawing the object.
     * @return The created [Object].
     */
    fun addObject(render: ()->Unit, material: RenderMaterial): Object {
        val obj = Object(render, material, Transform())
        objects.add(obj)
        return obj
    }

    /**
     * Renders the scene using the given camera.
     */
    fun render(camera: Camera) {
        for(phase in RenderPhase.all) {
            for (obj in objects) {
                obj.draw(lighting, camera, phase)
            }
        }
    }

}
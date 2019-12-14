package chs

/**
 * Any object which may be rendered to a [Scene].
 */
interface Renderable {
    /**
     * Binds this renderable to the LWJGL draw state and draws primitives. Should usually not be called externally.
     */
    fun draw(lights: Lighting, camera: Camera, phase: RenderPhase)
}
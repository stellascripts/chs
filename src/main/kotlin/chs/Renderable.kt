package chs

/**
 * Any object which may be rendered to a [Scene].
 */
interface Renderable {
    /**
     * Binds this renderable to the LWJGL draw state and draws primitives. Should usually not be called externally.
     */
    fun draw()

    /**
     * Deletes this object's OpenGL resources. The renderable may not be used to [draw] after this operation.
     */
    fun delete()
}
package chs

import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.memFree
import org.lwjgl.system.MemoryUtil.memUTF8

/**
 * Object representing the game window.
 * @param width The width of the window, in pixels.
 * @param height The height of the window, in pixels.
 * @param title The title of the window.
 * @throws InternalGLException if LWJGL encounters a problem when setting up the window.
 */
//todo: make windows resizable
class Window(width: Int, height: Int, title: String): AutoCloseable {

    /**
     * A pointer to the window OpenGL object.
     */
    var ptr: Long = NULL; private set

    /**
     * The width of the window, in pixels.
     */
    @API
    var width: Int = width; private set
    /**
     * The height of the window, in pixels.
     */
    @API
    var height: Int = height; private set
    /**
     * The title of the window.
     */
    @API
    var title: String = title; private set

    private fun getError(): String? {
        val errorBuffer = MemoryUtil.memAllocPointer(1)
        if (glfwGetError(errorBuffer) == GLFW_NO_ERROR) {
            memFree(errorBuffer)
            return null
        }
        val errorText = memUTF8(errorBuffer.get(0))
        memFree(errorBuffer)
        return errorText
    }

    init {
        glfwSetErrorCallback { error, description ->
            throw InternalGLException("GLFW Error $error: $description")
        }

        if (!glfwInit()) {
            val errorText = getError()
            throw InternalGLException("Could not start GLFW3: $errorText")
        }
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

        ptr = glfwCreateWindow(
            width, height, title,
            NULL, NULL
        )
        if (ptr == NULL) {
            val errorText = getError()
            glfwTerminate()
            throw InternalGLException("Could not open GLFW3 window: $errorText")
        }

        //todo: reroute to input handling mechanisms
        glfwSetKeyCallback(ptr) { _, key, _, action, _ ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(ptr, true)
        }

        glfwMakeContextCurrent(ptr)
        glfwSwapInterval(1)
        glfwShowWindow(ptr)

        GL.createCapabilities()
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val renderer = glGetString(GL_RENDERER)
        val version = glGetString(GL_VERSION)
        println("Renderer: $renderer Version: $version")

        glEnable(GL_DEPTH_TEST) //for depth-testing
        glEnable(GL_BLEND)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glDepthFunc(GL_LEQUAL)
    }

    /**
     * Starts the rendering loop.
     * @param render A function containing all the rendering operations to be done on each frame.
     */
    inline fun loop(render: () -> Unit) {
        while (!glfwWindowShouldClose(ptr)) {
            glDepthMask(true)
            glColorMask(true, true, true, true)
            glClear(GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)

            render()

            glfwPollEvents()
            glfwSwapBuffers(ptr)
        }
    }

    /**
     * Terminates the game window.
     */
    override fun close() {
        glfwFreeCallbacks(ptr)
        glfwDestroyWindow(ptr)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }
}
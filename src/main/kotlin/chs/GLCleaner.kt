package chs

import org.lwjgl.opengl.GL15.glDeleteBuffers
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import java.lang.ref.Cleaner

val GLCLEANER = Cleaner.create()

class GLDeleteBuffers(val idx: Int): Runnable {
    override fun run() {
        glDeleteBuffers(idx)
    }
}

class GLDeleteVertexArrays(val idx: Int) : Runnable {
    override fun run() {
        glDeleteVertexArrays(idx)
    }
}
package chs

import org.joml.Vector3fc
import org.lwjgl.opengl.GL30.*
import java.lang.IllegalStateException

@API
private class PointCluster(vararg positions: Vector3fc): Renderable {
    private var vbo: Int = -1
    private var vao: Int = -1
    private var size = positions.size
    private var deleted = false

    init {
        withStack {stack ->
            val positionBuf = stack.mallocFloat(positions.size*3)
            for(v in positions) {
                v.get(positionBuf)
            }
            vbo = glGenBuffers()
            glBindBuffer(GL_ARRAY_BUFFER, vbo)
            glBufferData(GL_ARRAY_BUFFER, positionBuf, GL_STATIC_DRAW)
        }

        vao = glGenVertexArrays()
        glBindVertexArray(vao)
        glEnableVertexAttribArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glVertexAttribPointer(0,3,
            GL_FLOAT,false,0,NULL)
    }

    override fun draw() {
        if(deleted) throw IllegalStateException("Cannot render deleted points.")
        glBindVertexArray(vao)
        glDrawArrays(GL_POINTS, 0, size)
    }

    override fun delete() {
        synchronized(this) {
            if (deleted) return
            deleted = true
        }
        glDeleteBuffers(vbo)
        glDeleteVertexArrays(vao)
    }
}
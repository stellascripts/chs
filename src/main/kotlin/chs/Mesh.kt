package chs

import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import java.lang.IllegalStateException
import java.nio.IntBuffer

/**
 * OpenGL drawable mesh data.
 */
class Mesh(vertex: FloatArray, intArray: IntArray, texCoord: FloatArray,
           normal: FloatArray): Renderable {
    private val index: IntBuffer
    private var vao: Int = 0
    private var vboIndex = 0
    private var vboPosition = 0
    private var vboTexCoords = 0
    private var vboNormal = 0

    private var deleted = false

    init {
        vboPosition = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboPosition)
        glBufferData(GL_ARRAY_BUFFER, vertex, GL_STATIC_DRAW)

        vboTexCoords = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboTexCoords)
        glBufferData(GL_ARRAY_BUFFER, texCoord, GL_STATIC_DRAW)

        vboNormal = glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboNormal)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normal, GL_STATIC_DRAW)

        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        glEnableVertexAttribArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, vboPosition)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, NULL)

        glEnableVertexAttribArray(1)
        glBindBuffer(GL_ARRAY_BUFFER, vboTexCoords)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, NULL)

        glEnableVertexAttribArray(2)
        glBindBuffer(GL15.GL_ARRAY_BUFFER, vboNormal)
        glVertexAttribPointer(2,3, GL_FLOAT, false, 0, NULL)

        index = MemoryUtil.memCallocInt(intArray.size).put(intArray)
        index.flip()

        vboIndex = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndex)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, index, GL_STATIC_DRAW)
    }

    override fun draw() {
        if(deleted) throw IllegalStateException("Cannot render deleted mesh.")
        //glDrawArrays(GL_TRIANGLES, 0, vertex.size/3)
        glBindVertexArray(vao)
        glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndex)
        glDrawElements(GL_TRIANGLES, index.limit(), GL_UNSIGNED_INT, 0)
    }


    override fun delete() {
        synchronized(this) {
            if (deleted) return
            deleted = true
        }
        glDeleteBuffers(vboPosition)
        glDeleteBuffers(vboTexCoords)
        glDeleteBuffers(vboNormal)
        glDeleteBuffers(vboIndex)
        glDeleteVertexArrays(vao)
    }
}
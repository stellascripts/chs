package chs

import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import java.lang.IllegalStateException
import java.nio.IntBuffer

/**
 * OpenGL drawable mesh data.
 * @param points An array of floating point values representing 3-component vertex positions.
 * @param indices An array of integer index values.
 * @param texCoords An array of floating point values representing 2-component texture coordinates.
 * @param normals An array of floating point values representing 3-component normal vectors.
 */
class Mesh(points: FloatArray, indices: IntArray, texCoords: FloatArray,
           normals: FloatArray) {
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
        glBufferData(GL_ARRAY_BUFFER, points, GL_STATIC_DRAW)

        vboTexCoords = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboTexCoords)
        glBufferData(GL_ARRAY_BUFFER, texCoords, GL_STATIC_DRAW)

        vboNormal = glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboNormal)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW)

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

        index = MemoryUtil.memCallocInt(indices.size).put(indices)
        index.flip()

        vboIndex = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndex)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, index, GL_STATIC_DRAW)
    }

    fun draw() {
        if(deleted) throw IllegalStateException("Cannot render deleted mesh.")
        //glDrawArrays(GL_TRIANGLES, 0, vertex.size/3)
        glBindVertexArray(vao)
        glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndex)
        glDrawElements(GL_TRIANGLES, index.limit(), GL_UNSIGNED_INT, 0)
    }


    fun delete() {
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
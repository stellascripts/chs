package chs.mesh

import chs.*
import chs.gltf.GLTF
import com.chiaroscuro.chiaroscuro.checkGL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

class BufferView(val buffer: ByteBuffer,
                 val offset: Int,
                 val length: Int,
                 val stride: Int
) {
    var vbo: Int = -1
        get() = if(!hasVBO) error("VBO not yet created") else field
        private set
    var hasVBO = false; private set

    fun createVBO(target: GLTF.BufferTarget) {
        vbo = glGenBuffers()
        hasVBO = true
        //GLCLEANER.register(this, GLDeleteBuffers(vbo))
        glBindBuffer(target.code, vbo).checkGL()
        buffer.position(offset)
        val view = buffer.slice()
        view.limit(length)
        if(!view.isDirect) error("Not a direct buffer.")
        glBufferData(target.code, view, GL_STATIC_DRAW).checkGL()
    }

    fun bind(target: GLTF.BufferTarget) {
        glBindBuffer(target.code, vbo).checkGL()
    }
}

class Accessor(
    val view: BufferView,
    val offset: Int,
    val componentType: GLTF.ComponentType,
    val normalized: Boolean,
    val count: Int,
    val type: GLTF.Type
) {
    fun bindAttribute(i: Int) {
        glVertexAttribPointer(i, type.numComponents, componentType.code, normalized, view.stride, offset.toLong()).checkGL()
    }
}

class Primitive(
    val attributes: Map<String, Accessor>,
    val indices: Accessor?,
    val mode: GLTF.RenderMode
) {
    val vao: Int

    init {
        vao = glGenVertexArrays()
        GLCLEANER.register(this, GLDeleteVertexArrays(vao))
        glBindVertexArray(vao)
        for((name,acc) in attributes) {
            val loc = Shader.ATTRIBUTE_LOCATIONS[name]?:error("$name is not a defined attribute.")
            glEnableVertexAttribArray(loc)
            val target = GLTF.BufferTarget.ARRAY_BUFFER
            if(!acc.view.hasVBO) acc.view.createVBO(target)
            acc.view.bind(target)
            acc.bindAttribute(loc)
        }
        if(indices != null && !indices.view.hasVBO) indices.view.createVBO(GLTF.BufferTarget.ELEMENT_ARRAY_BUFFER)
    }

    fun draw() {
        val pos = attributes["POSITION"]?:return
        glBindVertexArray(vao)
        if(indices != null) {
            indices.view.bind(GLTF.BufferTarget.ELEMENT_ARRAY_BUFFER)
            glDrawElements(mode.code, indices.count, indices.componentType.code, indices.offset.toLong()).checkGL()
        } else {
            GL11.glDrawArrays(mode.code, 0, pos.count).checkGL()
        }
    }
}
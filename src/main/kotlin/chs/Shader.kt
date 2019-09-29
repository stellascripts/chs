package chs

import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30.*
import java.lang.IllegalStateException

/**
 * Shader used for rendering objects. May be loaded by [Assets.load] using [chs.assets.ShaderLoader] and a location
 * path containing either .vert or .frag extensions.
 * @param vertexProgram A string containing complete GLSL vertex source code.
 * @param fragmentProgram A string containing complete GLSL fragment source code.
 * @throws AssetLoadingException if the provided shader code contains GLSL errors.
 */
class Shader(vertexProgram: String, fragmentProgram: String) {
    private var program: Int = 0
    private var deleted = false

    init {
        val vs = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vs, vertexProgram)
        glCompileShader(vs)
        if (glGetShaderi(vs, GL_COMPILE_STATUS) != GL_TRUE) {
            val log = glGetShaderInfoLog(vs)
            throw AssetLoadingException("Error in Vertex Shader: $log")
        }

        val fs = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fs, fragmentProgram)
        glCompileShader(fs)
        if (glGetShaderi(fs, GL_COMPILE_STATUS) != GL_TRUE) {
            val log = glGetShaderInfoLog(fs)
            throw AssetLoadingException("Error in Fragment Shader: $log")
        }

        program = glCreateProgram()
        glAttachShader(program, fs)
        glAttachShader(program, vs)

        //bind locations
        glBindAttribLocation(program, 0, "in_position")
        glBindAttribLocation(program, 1, "in_tex_coords")
        glBindAttribLocation(program, 2, "in_normal")

        glLinkProgram(program)
        if (glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE) {
            val log = glGetProgramInfoLog(program)
            throw AssetLoadingException("Error during Shader Link: $log")
        }

        //validate()
    }

    /**
     * Deletes the associated OpenGL program object. The shader may not be used after this call.
     */
    fun delete() {
        synchronized(this) {
            if (deleted) return
            deleted = true
        }
        glDeleteProgram(program)
    }

    /**
     * Validates the shader using glValidateProgram() and throws an [AssetLoadingException] if it fails.
     */
    @API
    fun validate() {
        glValidateProgram(program)
        if (glGetProgrami(program, GL_VALIDATE_STATUS) != GL_TRUE) {
            val log = GL20.glGetProgramInfoLog(program)
            throw AssetLoadingException(log)
        }
    }

    private fun use() {
        if(deleted) throw IllegalStateException("Cannot use deleted shader.")
        glUseProgram(program)
    }

    private val uniforms = HashMap<String, Int>()

    private fun locateUniform(s: String) = glGetUniformLocation(program ,s)

    /**
     * Sets a particular uniform parameter for this shader.
     * @param name The name of the uniform parameter in GLSL code.
     * @param type The type of uniform parameter. See [Uniforms].
     * @param value The value to set the uniform parameter to.
     * @param T The type of value as determined by the uniform type.
     */
    fun <T> set(name: String, type: UniformType<T>, value: T) {
        use()
        val location = uniforms.computeIfAbsent(name, this::locateUniform)
        type(location, value)
    }
}
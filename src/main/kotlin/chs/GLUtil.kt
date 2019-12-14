package com.chiaroscuro.chiaroscuro

import chs.ChsException
import chs.InternalGLException
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION

fun <T> T.checkGL(): T {
    val error = glGetError()
    if(error == GL_NO_ERROR) return this
    throw InternalGLException(when(error) {
        GL_INVALID_ENUM ->
            "An unacceptable value is specified for an enumerated argument."
        GL_INVALID_VALUE ->
            "A numeric argument is out of range."
        GL_INVALID_OPERATION ->
            "The specified operation is not allowed in the current state."
        GL_INVALID_FRAMEBUFFER_OPERATION ->
            "The framebuffer object is not complete."
        GL_OUT_OF_MEMORY ->
            "There is not enough memory left to execute the command. The state of the GL is undefined."
        GL_STACK_UNDERFLOW ->
            "An attempt has been made to perform an operation that would cause an internal stack to underflow."
        GL_STACK_OVERFLOW ->
            "An attempt has been made to perform an operation that would cause an internal stack to overflow."
        else ->
            "Unknown error occurred: $error"
    })
}
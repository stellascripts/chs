package chs.gltf

import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f
import org.joml.Vector4fc
import org.json.simple.JSONArray
import org.json.simple.JSONObject

object JSONUtils {
    fun <T> getEnumConstants(cls: Class<T>): List<T> = cls.enumConstants.toList()

    fun Any.toInt() = (this as Long).toInt()
    fun Any.toFloat() = (this as Number).toFloat()
    fun Any.toArray() = this as JSONArray
    fun Any.toObject() = this as JSONObject

    fun Any.toVector3(): Vector3fc {
        val (x,y,z) = (this as JSONArray)
        return Vector3f(x!!.toFloat(), y!!.toFloat(), z!!.toFloat())
    }

    fun Any.toVector4(): Vector4fc {
        val (x,y,z,w) = (this as JSONArray)
        return Vector4f(x!!.toFloat(), y!!.toFloat(), z!!.toFloat(), w!!.toFloat())
    }

    inline fun <T> JSONArray.collect(f: (JSONObject)->T): List<T> = this.map { f(it as JSONObject) }

    inline fun <reified T : Enum<T>> Any.toTextEnum(): T {
        val text = this.toString()
        val enums = getEnumConstants(T::class.java)
        for (e in enums) {
            if ((e as GLTF.TextEnum).text == text) return e
        }
        error("No such ${T::class.java.simpleName}: $text")
    }

    inline fun <reified T : Enum<T>> Any.toIntEnum(): T {
        val int = this.toInt()
        val enums = getEnumConstants(T::class.java)
        for (e in enums) {
            if ((e as GLTF.IntEnum).code == int) return e
        }
        error("No such ${T::class.java.simpleName}: $int")
    }

    fun required(field: String): Nothing = error("Required field: $field")

    fun Any.toInts(): IntArray = (this as JSONArray).map { it!!.toInt() }.toIntArray()
    fun Any.toFloats(): FloatArray = (this as JSONArray).map { it!!.toFloat() }.toFloatArray()
    fun Any.toNumArray(type: GLTF.ComponentType): NumArray {
        return if (type == GLTF.ComponentType.FLOAT) toFloats()
        else toInts()
    }
}
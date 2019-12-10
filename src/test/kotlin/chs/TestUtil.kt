package chs

import io.kotlintest.Matcher
import io.kotlintest.Result
import org.joml.Vector3f
import org.joml.Vector3fc
import java.lang.reflect.InvocationTargetException
import kotlin.math.abs
import kotlin.math.max

@API
fun <T> Any.getInternal(listName: String): T {
    val field = this.javaClass.getDeclaredField(listName)
    field.isAccessible = true
    return field.get(this) as T
}

@API
fun <T> Any.callInternal(name: String, vararg args: Any): T {
    val params = args.map { it::class.java }.toTypedArray()
    val method = this.javaClass.getDeclaredMethod(name, *params)
    method.isAccessible = true
    try {
        return method.invoke(this, *args) as T
    } catch(ex: InvocationTargetException) {
        throw ex.cause!!
    } catch(throwable: Throwable) {
        throw throwable
    }
}

fun beApprox(x: Float, y: Float, z: Float): Matcher<Vector3fc> {
    return object : Matcher<Vector3fc>{
        val v = Vector3f(x,y,z)
        override fun test(value: Vector3fc): Result {
            return Result(
                value approx v,
                "expected: $v but was: $value (${chebyshev(value,v)})",
                "expected: NOT $v but was. (${chebyshev(value,v)})"
            )
        }
    }
}

fun chebyshev(v: Vector3fc, w: Vector3fc) = max(max(abs(v.x()-w.x()), abs(v.y()-w.y())), abs(v.z()-w.z()))
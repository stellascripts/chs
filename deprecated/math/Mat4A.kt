package com.chiaroscuro.chiaroscuro.math

import java.util.concurrent.atomic.AtomicInteger

fun FloatArray.swap(i: Int, j: Int) {
    val f = this[i]
    this[i] = this[j]
    this[j] = f
}

const val M00 = 0
const val M01 = 1
const val M02 = 2
const val M03 = 3
const val M10 = 4
const val M11 = 5
const val M12 = 6
const val M13 = 7
const val M20 = 8
const val M21 = 9
const val M22 = 10
const val M23 = 11
const val M30 = 12
const val M31 = 13
const val M32 = 14
const val M33 = 15

object Mat4A {
    private val zeroM = FloatArray(16) { 0f }
    private val oneM = FloatArray(16) { 1f }
    private val identityM = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    fun loadIdentity(f: FloatArray = FloatArray(16)): FloatArray {
        return identityM.copyInto(f)
    }

    fun loadZero(f: FloatArray = FloatArray(16)): FloatArray {
        return zeroM.copyInto(f)
    }

    fun loadOne(f: FloatArray = FloatArray(16)): FloatArray {
        return oneM.copyInto(f)
    }

    val names = listOf(
        "x->x", "y->x", "z->x", "1->x",
        "x->y", "y->y", "z->y", "1->y",
        "x->z", "y->z", "z->z", "1->z",
        "x->w", "y->w", "z->w", "1->w"
    )

    fun toString(f: FloatArray) = buildString {
        val s = f.map { it.toString() }
        val r = s.maxBy { it.length }?.length ?: 0
        for (i in 0 until 16) {
            val m = i % 4
            if (m != 0) append(" | ")
            append(names[i])
            append(s[i].padStart(r, ' '))
            if (m == 3) append("\n")
        }
    }

    fun transpose(f: FloatArray) {
        f.swap(M01, M10)
        f.swap(M02, M20)
        f.swap(M03, M30)
        f.swap(M12, M21)
        f.swap(M13, M31)
        f.swap(M23, M32)
    }

    fun multiply4x4(a: FloatArray, b: FloatArray, store: FloatArray) {
        dealias(store, store === a || store === b) { s ->
            for (i in 0..3) {
                for (j in 0..3) {
                    s[4 * i + j] = b[4 * 0 + j] * a[4 * i + 0] +
                            b[4 * 1 + j] * a[4 * i + 1] +
                            b[4 * 2 + j] * a[4 * i + 2] +
                            b[4 * 3 + j] * a[4 * i + 3]
                }
            }
        }
    }

    fun invert(a: FloatArray) {
        dealias(a, true) { s ->
            val a0 = a[M00] * a[M11] - a[M01] * a[M10]
            val a1 = a[M00] * a[M12] - a[M02] * a[M10]
            val a2 = a[M00] * a[M13] - a[M03] * a[M10]
            val a3 = a[M01] * a[M12] - a[M02] * a[M11]
            val a4 = a[M01] * a[M13] - a[M03] * a[M11]
            val a5 = a[M02] * a[M13] - a[M03] * a[M12]
            val b0 = a[M20] * a[M31] - a[M21] * a[M30]
            val b1 = a[M20] * a[M32] - a[M22] * a[M30]
            val b2 = a[M20] * a[M33] - a[M23] * a[M30]
            val b3 = a[M21] * a[M32] - a[M22] * a[M31]
            val b4 = a[M21] * a[M33] - a[M23] * a[M31]
            val b5 = a[M22] * a[M33] - a[M23] * a[M32]
            val det = a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0

            if (det == 0f) {
                throw ArithmeticException("This matrix cannot be inverted")
            }

            val inv = 1.0f / det

            s[M00] = (+a[M11] * b5 - a[M12] * b4 + a[M13] * b3) * inv
            s[M10] = (-a[M10] * b5 + a[M12] * b2 - a[M13] * b1) * inv
            s[M20] = (+a[M10] * b4 - a[M11] * b2 + a[M13] * b0) * inv
            s[M30] = (-a[M10] * b3 + a[M11] * b1 - a[M12] * b0) * inv
            s[M01] = (-a[M01] * b5 + a[M02] * b4 - a[M03] * b3) * inv
            s[M11] = (+a[M00] * b5 - a[M02] * b2 + a[M03] * b1) * inv
            s[M21] = (-a[M00] * b4 + a[M01] * b2 - a[M03] * b0) * inv
            s[M31] = (+a[M00] * b3 - a[M01] * b1 + a[M02] * b0) * inv
            s[M02] = (+a[M31] * a5 - a[M32] * a4 + a[M33] * a3) * inv
            s[M12] = (-a[M30] * a5 + a[M32] * a2 - a[M33] * a1) * inv
            s[M22] = (+a[M30] * a4 - a[M31] * a2 + a[M33] * a0) * inv
            s[M32] = (-a[M30] * a3 + a[M31] * a1 - a[M32] * a0) * inv
            s[M03] = (-a[M21] * a5 + a[M22] * a4 - a[M23] * a3) * inv
            s[M13] = (+a[M20] * a5 - a[M22] * a2 + a[M23] * a1) * inv
            s[M23] = (-a[M20] * a4 + a[M21] * a2 - a[M23] * a0) * inv
            s[M33] = (+a[M20] * a3 - a[M21] * a1 + a[M22] * a0) * inv
        }
    }

    fun transform(a: FloatArray, v: Vec3): Vec3 {
        val w = a[M30] * v.x + a[M31] * v.y + a[M32] * v.z + a[M33]
        val x = a[M00] * v.x + a[M01] * v.y + a[M02] * v.z + a[M03]
        val y = a[M10] * v.x + a[M11] * v.y + a[M12] * v.z + a[M13]
        val z = a[M20] * v.x + a[M21] * v.y + a[M22] * v.z + a[M23]
        return if (w == 1f) Vec3(
            x = x,
            y = y,
            z = z
        ) else Vec3(
            x = x / w,
            y = y / w,
            z = z / w
        )
    }

    //val spareIdx = AtomicInteger(0)
    private val spareIdx = AtomicInteger(0)
    private val spares = Array(6) { FloatArray(16) }

    fun acquire(): FloatArray {
        val i = spareIdx.getAndIncrement()
        return if (i >= 6) FloatArray(16)
        else spares[i]
    }

    fun release() {
        spareIdx.decrementAndGet()
    }

    inline fun scratch(copyTo: FloatArray? = null, f: (FloatArray) -> Unit) {
        try {
            val m = acquire()
            f(m)
            if (copyTo != null) m.copyInto(copyTo)
        } finally {
            release()
        }
    }

    inline fun dealias(a: FloatArray, isAliased: Boolean, f: (FloatArray) -> Unit) {
        if (isAliased) {
            scratch(a, f)
        } else f(a)
    }
}
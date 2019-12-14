package com.chiaroscuro.chiaroscuro.math

import kotlin.math.absoluteValue

class Math4(val a: FloatArray) {
    constructor(): this(FloatArray(16))

    private var constant = false
    fun constant(): Math4 {
        constant = true
        return this
    }

    operator fun get(row: Int, col: Int): Float {
        val i = (col shl 2) or row and 0b1111
        return a[i]
    }

    operator fun set(row: Int, col: Int, v: Float) {
        if(constant) error("Attempt to modify constant matrix with set[$row,$col] = $v")
        val i = (col shl 2) or row and 0b1111
        a[i] = v
    }

    fun copy() = Math4(a.copyOf())

    fun load(m: Math4) {
        m.a.copyInto(a)
    }

    fun loadParallelFrustum(
        near: Float, far: Float,
        left: Float, right: Float,
        top: Float, bottom: Float
    ) {
        load(ZERO)
        a[0] = 2.0f / (right - left)
        a[4] = 2.0f / (top - bottom)
        a[8] = -2.0f / (far - near)
        a[12] = 1f

        a[3] = -(right+left)/(right-left)
        a[7] = -(top+bottom)/(top-bottom)
        a[11] = -(far+near)/(far-near)
    }

    fun loadFrustum(
        near: Float, far: Float,
        left: Float, right: Float,
        top: Float, bottom: Float
    ) {
        load(IDENTITY)
        a[0] = 2f*near / (right-left)
        a[4] = 2f*near / (top-bottom)
        a[14] = -1f
        a[15] = 0f

        a[2] = (right + left) / (right - left)
        a[6] = (top + bottom) / (top - bottom)
        a[10] = -(far + near) / (far - near)
        a[11] = -(2 * far * near) / (far - near)
    }

    fun transpose() {
        if(constant) error("Attempt to modify constant matrix with transpose().")
        a.swap(1, 4)
        a.swap(2,8)
        a.swap(3, 12)
        a.swap(6, 9)
        a.swap(7, 13)
        a.swap(11, 14)

        /*
        00 01 02 03
        04 05 06 07
        08 09 10 11
        12 13 14 15
         ->
        00 04 08 12
        01 05 09 13
        02 06 10 14
        03 07 11 15
         */
        //a.swap(0, 0)
        //a.swap(1, 4)
        //a.swap(2,8)
        //a.swap(3, 12)
        //a.swap(4, 1)
        //a.swap(5, 5)
        //a.swap(6, 9)
        //a.swap(7, 13)
        //a.swap(8,2)
        //a.swap(9, 6)
        //a.swap(10, 10)
        //a.swap(11, 14)
        //a.swap(12, 3)
        //a.swap(13, 7)
        //a.swap(14, 11)
        //a.swap(15, 15)
    }

    fun invert() {
        if(constant) error("Attempt to modify constant matrix with invert().")
        val a0 = a[0] * a[5] - a[1] * a[4]
        val a1 = a[0] * a[6] - a[2] * a[4]
        val a2 = a[0] * a[7] - a[3] * a[4]
        val a3 = a[1] * a[6] - a[2] * a[5]
        val a4 = a[1] * a[7] - a[3] * a[5]
        val a5 = a[2] * a[7] - a[3] * a[6]
        val b0 = a[8] * a[13] - a[9] * a[12]
        val b1 = a[8] * a[14] - a[10] * a[12]
        val b2 = a[8] * a[15] - a[11] * a[12]
        val b3 = a[9] * a[14] - a[10] * a[13]
        val b4 = a[9] * a[15] - a[11] * a[13]
        val b5 = a[10] * a[15] - a[11] * a[14]
        val det = a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0

        if (det.absoluteValue <= 0f) {
            throw ArithmeticException("This matrix cannot be inverted")
        }

        val inv = 1f/det

        a[0] = (+a[5] * b5 - a[6] * b4 + a[7] * b3)*inv
        a[4] = (-a[4] * b5 + a[6] * b2 - a[7] * b1)*inv
        a[8] = (+a[4] * b4 - a[5] * b2 + a[7] * b0)*inv
        a[12] = (-a[4] * b3 + a[5] * b1 - a[6] * b0)*inv
        a[1] = (-a[1] * b5 + a[2] * b4 - a[3] * b3)*inv
        a[5] = (+a[0] * b5 - a[2] * b2 + a[3] * b1)*inv
        a[9] = (-a[0] * b4 + a[1] * b2 - a[3] * b0)*inv
        a[13] = (+a[0] * b3 - a[1] * b1 + a[2] * b0)*inv
        a[2] = (+a[13] * a5 - a[14] * a4 + a[15] * a3)*inv
        a[6] = (-a[12] * a5 + a[14] * a2 - a[15] * a1)*inv
        a[10] = (+a[12] * a4 - a[13] * a2 + a[15] * a0)*inv
        a[14] = (-a[12] * a3 + a[13] * a1 - a[14] * a0)*inv
        a[3] = (-a[9] * a5 + a[10] * a4 - a[11] * a3)*inv
        a[7] = (+a[8] * a5 - a[10] * a2 + a[11] * a1)*inv
        a[11] = (-a[8] * a4 + a[9] * a2 - a[11] * a0)*inv
        a[15] = (+a[8] * a3 - a[9] * a1 + a[10] * a0)*inv
    }

    fun invertTranspose() {
        if(constant) error("Attempt to modify constant matrix with invert().")
        val a0 = a[0] * a[5] - a[1] * a[4]
        val a1 = a[0] * a[6] - a[2] * a[4]
        val a2 = a[0] * a[7] - a[3] * a[4]
        val a3 = a[1] * a[6] - a[2] * a[5]
        val a4 = a[1] * a[7] - a[3] * a[5]
        val a5 = a[2] * a[7] - a[3] * a[6]
        val b0 = a[8] * a[13] - a[9] * a[12]
        val b1 = a[8] * a[14] - a[10] * a[12]
        val b2 = a[8] * a[15] - a[11] * a[12]
        val b3 = a[9] * a[14] - a[10] * a[13]
        val b4 = a[9] * a[15] - a[11] * a[13]
        val b5 = a[10] * a[15] - a[11] * a[14]
        val det = a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0

        if (det.absoluteValue <= 0f) {
            throw ArithmeticException("This matrix cannot be inverted")
        }

        val inv = 1f/det

        a[0] = (+a[5] * b5 - a[6] * b4 + a[7] * b3)*inv
        a[1] = (-a[4] * b5 + a[6] * b2 - a[7] * b1)*inv
        a[2] = (+a[4] * b4 - a[5] * b2 + a[7] * b0)*inv
        a[3] = (-a[4] * b3 + a[5] * b1 - a[6] * b0)*inv
        a[4] = (-a[1] * b5 + a[2] * b4 - a[3] * b3)*inv
        a[5] = (+a[0] * b5 - a[2] * b2 + a[3] * b1)*inv
        a[6] = (-a[0] * b4 + a[1] * b2 - a[3] * b0)*inv
        a[7] = (+a[0] * b3 - a[1] * b1 + a[2] * b0)*inv
        a[8] = (+a[13] * a5 - a[14] * a4 + a[15] * a3)*inv
        a[9] = (-a[12] * a5 + a[14] * a2 - a[15] * a1)*inv
        a[10] = (+a[12] * a4 - a[13] * a2 + a[15] * a0)*inv
        a[11] = (-a[12] * a3 + a[13] * a1 - a[14] * a0)*inv
        a[12] = (-a[9] * a5 + a[10] * a4 - a[11] * a3)*inv
        a[13] = (+a[8] * a5 - a[10] * a2 + a[11] * a1)*inv
        a[14] = (-a[8] * a4 + a[9] * a2 - a[11] * a0)*inv
        a[15] = (+a[8] * a3 - a[9] * a1 + a[10] * a0)*inv
    }

    fun scale(s: Float) {
        if(constant) error("Attempt to modify constant matrix with scale($s).")
        for(i in 0 until a.size) {
            a[i] *= s
        }
    }

    fun multiplyBy(m: Math4) {
        if(constant) error("Attempt to modify constant matrix with multiplyBy($m).")
        for(i in 0 until a.size) {
            a[i] *= m.a[i]
        }
    }

    fun transform(v: Vec3): Vec3 {
        val w = a[12] * v.x + a[13] * v.y + a[14] * v.z + a[15]
        return if (w == 1f) Vec3(
            x = a[0] * v.x + a[1] * v.y + a[2] * v.z + a[3],
            y = a[4] * v.x + a[5] * v.y + a[6] * v.z + a[7],
            z = a[8] * v.x + a[9] * v.y + a[10] * v.z + a[11]
        ) else Vec3(
            x = (a[0] * v.x + a[1] * v.y + a[2] * v.z + a[3]) / w,
            y = (a[4] * v.x + a[5] * v.y + a[6] * v.z + a[7]) / w,
            z = (a[8] * v.x + a[9] * v.y + a[10] * v.z + a[11]) / w
        )
    }

    fun transform(v: Vec4) = Vec4(
        x = a[0] * v.x + a[1] * v.y + a[2] * v.z + a[3] * v.w,
        y = a[4] * v.x + a[5] * v.y + a[6] * v.z + a[7] * v.w,
        z = a[8] * v.x + a[9] * v.y + a[10] * v.z + a[11] * v.w,
        w = a[12] * v.x + a[13] * v.y + a[14] * v.z + a[15] * v.w
    )

    fun transform(b: BiVec3) = BiVec3(
        xy = a[0] * b.xy + a[1] * b.xz + a[2] * b.yz,
        xz = a[4] * b.xy + a[5] * b.xz + a[6] * b.yz,
        yz = a[8] * b.xy + a[9] * b.xz + a[10] * b.xz
    )

    companion object {
        val ZERO = Math4(FloatArray(16)).constant()
        val IDENTITY = Math4(floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)).constant()
        val ONE = Math4(floatArrayOf(1f,1f,1f,1f, 1f,1f,1f,1f, 1f,1f,1f,1f, 1f,1f,1f,1f)).constant()
    }
}

fun FloatArray.swap(i: Int, j: Int) {
    val f = this[i]
    this[i] = this[j]
    this[j] = f
}
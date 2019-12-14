package com.chiaroscuro.chiaroscuro.math

import kotlin.math.sqrt

data class Vec3(val x: Float, val y: Float, val z: Float) {
    val isNormal: Boolean get() = (x * x + y * y + z * z) approx  1f

    operator fun plus(v: Vec3): Vec3 =
        Vec3(x + v.x, y + v.y, z + v.z)

    operator fun minus(v: Vec3): Vec3 =
        Vec3(x - v.x, y - v.y, z - v.z)

    operator fun unaryMinus(): Vec3 = Vec3(-x + 0.0f, -y + 0.0f, -z + 0.0f)
    operator fun times(n: Number): Vec3 =
        Vec3(x * n.toFloat(), y * n.toFloat(), z * n.toFloat())

    operator fun div(n: Number): Vec3 =
        Vec3(x / n.toFloat(), y / n.toFloat(), z / n.toFloat())

    infix fun scaleBy(v: Vec3): Vec3 =
        Vec3(x * v.x, y * v.y, z * v.z)

    infix fun scaleOver(v: Vec3): Vec3 =
        Vec3(x / v.x, y / v.y, z / v.z)

    infix fun dot(v: Vec3): Float = x * v.x + y * v.y + z * v.z
    infix fun cross(v: Vec3): Vec3 = Vec3(
        y * v.z - z * v.y,
        z * v.x - x * v.z,
        x * v.y - y * v.x
    )

    fun plus(x: Float, y: Float, z: Float): Vec3 = Vec3(this.x + x, this.y + y, this.z + z)
    fun minus(x: Float, y: Float, z: Float): Vec3 = Vec3(this.x - x, this.y - y, this.z - z)
    fun scaleBy(x: Float, y: Float, z: Float): Vec3 =
        Vec3(this.x * x, this.y * y, this.z * z)

    fun scaleOver(x: Float, y: Float, z: Float): Vec3 =
        Vec3(this.x / x, this.y / y, this.z / z)

    fun dot(x: Float, y: Float, z: Float): Float = this.x * x + this.y * y + this.z * z
    fun cross(x: Float, y: Float, z: Float): Vec3 = Vec3(
        this.y * z - this.z * y,
        this.z * x - this.x * z,
        this.x * y - this.y * x
    )

    private var cachedMag = Float.NaN

    val magSquared: Float get() = x * x + y * y + z * z
    val magnitude: Float
        get() {
            if (cachedMag.isNaN()) {
                cachedMag = sqrt(magSquared)
            }
            return cachedMag
        }

    fun distSq(v: Vec3): Float = sq(x - v.x) + sq(
        y - v.y
    ) + sq(z - v.z)

    val normal: Vec3
        get() {
            val d = this dot this
            return if (d approx 1f) this
            else this / sqrt(d)
        }

    //infix fun wedge(v: Vec3): BiVec3 = BiVec3(this, v)
    //infix fun geo(v: Vec3): Rotor = Rotor(this, v)

    companion object {
        val X_AXIS: Vec3 = Vec3(1f, 0f, 0f)
        val Y_AXIS: Vec3 = Vec3(0f, 1f, 0f)
        val Z_AXIS: Vec3 = Vec3(0f, 0f, 1f)
        val NX_AXIS: Vec3 = Vec3(-1f, 0f, 0f)
        val NY_AXIS: Vec3 = Vec3(0f, -1f, 0f)
        val NZ_AXIS: Vec3 = Vec3(0f, 0f, -1f)
        val ONE: Vec3 = Vec3(1f, 1f, 1f)
        val ZERO: Vec3 = Vec3(0f, 0f, 0f)
    }
}

fun Float.times(v: Vec3): Vec3 =
    Vec3(v.x * this, v.y * this, v.z * this)
package com.chiaroscuro.chiaroscuro.math

data class Vec4(val x: Float, val y: Float, val z: Float, val w: Float) {
    fun toVec3() = Vec3(x,y,z)
}

fun Vec3.toVec4(w: Float = 0f) = Vec4(this.x, this.y, this.z, w)
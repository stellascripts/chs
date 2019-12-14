package com.chiaroscuro.chiaroscuro.math

data class Tri(val a: Vec3, val b: Vec3, val c: Vec3) {
    val normal: Vec3 = (b - a).cross(c.x - a.x, c.y - a.y, c.z - a.z).normal
    val center: Vec3 = (a + b + c) * 0.33333334f
}
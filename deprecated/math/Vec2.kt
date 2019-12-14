package com.chiaroscuro.chiaroscuro.math

import kotlin.math.sqrt

data class Vec2(val x: Float, val y: Float) {
    operator fun plus(v: Vec2): Vec2 =
        Vec2(x + v.x, y + v.y)

    operator fun minus(v: Vec2): Vec2 =
        Vec2(x - v.x, y - v.y)

    operator fun unaryMinus(): Vec2 = Vec2(-x, -y)
    operator fun times(n: Number): Vec2 = Vec2(x * n.toFloat(), y * n.toFloat())
    operator fun div(n: Number): Vec2 = Vec2(x / n.toFloat(), y / n.toFloat())
    infix fun scaleBy(v: Vec2): Vec2 =
        Vec2(x * v.x, y * v.y)

    infix fun scaleOver(v: Vec2): Vec2 =
        Vec2(x / v.x, y / v.y)

    infix fun dot(v: Vec2): Float = x * v.x + y * v.y

    fun plus(x: Float, y: Float): Vec2 = Vec2(this.x + x, this.y + y)

    fun distSq(v: Vec2): Float = sq(x - v.x) + sq(
        y - v.y
    )

    fun distSq(x: Float, y: Float): Float = sq(this.x - x) + sq(
        this.y - y
    )

    override fun toString(): String {
        return "($x, $y)"
    }

    val magSquared: Float get() = x * x + y * y
    val magnitude: Float get() = sqrt(magSquared)

    val normal: Vec2 get() = this / magnitude

    fun approx(x: Float, y: Float): Boolean = this.x approx x && this.y approx y

    companion object {
        val ZERO: Vec2 = Vec2(0f, 0f)
        val UNIT_X: Vec2 = Vec2(1f, 0f)
        val UNIT_Y: Vec2 = Vec2(0f, 1f)
        val ONE: Vec2 = Vec2(1f, 1f)
    }
}
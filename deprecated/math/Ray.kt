package com.chiaroscuro.chiaroscuro.math

class Ray2 constructor(val origin: Vec2, dir: Vec2) {
    val limit: Float = dir.magnitude
    val dir: Vec2 = dir / if (limit > 0f) limit else 1f

    fun pointAt(t: Float): Vec2 = Vec2(origin.x + dir.x * t, origin.y + dir.y * t)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Ray2

        if (origin != other.origin) return false
        if (dir != other.dir) return false

        return true
    }

    override fun hashCode(): Int {
        var result = origin.hashCode()
        result = 31 * result + dir.hashCode()
        return result
    }

    override fun toString(): String {
        return "Ray: $origin -> $dir"
    }

    operator fun component1(): Vec2 = origin
    operator fun component2(): Vec2 = dir
    operator fun component3(): Float = limit
}
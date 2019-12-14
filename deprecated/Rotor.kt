package com.chiaroscuro.chiaroscuro.math

import kotlin.math.*

data class Rotor(val a: Float, val xy: Float, val xz: Float, val yz: Float) {
    constructor(a: Float, b: BiVec3) : this(a, b.xy, b.xz, b.yz)

    operator fun times(q: Rotor) = Rotor(
        a = a * q.a - xy * q.xy - xz * q.xz - yz * q.yz,
        xy = xy * q.a + a * q.xy + yz * q.xz - xz * q.yz,
        xz = xz * q.a + a * q.xz - yz * q.xy + xy * q.yz,
        yz = yz * q.a + a * q.yz + xz * q.xy - xy * q.xz
    )

    fun rotate(v: Vec3): Vec3 {
        val tx = a * v.x + v.y * xy + v.z * xz
        val ty = a * v.y - v.x * xy + v.z * yz
        val tz = a * v.z - v.x * xz - v.y * yz
        val txyz = v.x * yz + v.y * xz - v.z * xy

        return Vec3(
            x = a * tx + ty * xy + tz * xz - txyz * yz,
            y = a * ty - tx * xy + txyz * xz + tz * yz,
            z = a * tz - txyz * xy - tx * xz - ty * yz
        )
    }

    fun rotate(r: Rotor) = this * r * this.reverse

    val reverse get() = Rotor(a,-xy,-xz,-yz)
    val magSquared get() = sq(a) + sq(xy) + sq(xz) + sq(yz)
    val mag get() = sqrt(magSquared)
    val normal get(): Rotor {
        val m = mag
        return Rotor(a/m, xy/m, xz/m, yz/m)
    }

    /*fun toMat3(): Mat3 {
        val vx = rotate(Vec3(1f, 0f, 0f))
        val vy = rotate(Vec3(0f, 1f, 0f))
        val vz = rotate(Vec3(0f,0f,1f))
        return Mat3(
            vx.x, vx.y, vx.z,
            vy.x, vy.y, vy.z,
            vz.x, vz.y, vz.z
        )
    }*/

    fun write(m: FloatArray) {
        val vx = rotate(Vec3(1f, 0f, 0f))
        val vy = rotate(Vec3(0f, 1f, 0f))
        val vz = rotate(Vec3(0f,0f,1f))

        m[ 0] = vx.x; m[ 1] = vx.y; m[ 2] = vx.z
        m[ 4] = vy.x; m[ 5] = vy.y; m[ 6] = vy.z
        m[ 8] = vz.x; m[ 9] = vz.y; m[10] = vz.z
    }

    companion object {
        fun rotation(from: Vec3, to: Vec3): Rotor {
            val a = 1f + (to dot from)
            val xy = to.x * from.y - to.y * from.x
            val xz = to.x * from.z - to.z * from.x
            val yz = to.y * from.z - to.z * from.y

            return normalized(a, xy, xz, yz)
        }

        fun anglePlane(angle: Float, pl: BiVec3): Rotor {
            val n = -sin(angle / 2f)
            return normalized(
                a=cos(angle/2f),
                xy = n*pl.xy,
                xz = n*pl.xz,
                yz = n*pl.yz
            )

        }

        fun normalized(a: Float, xy: Float, xz: Float, yz: Float): Rotor {
            val m = sqrt(a*a + xy*xy + xz*xz + yz*yz)
            return Rotor(a/m, xy/m, xz/m, yz/m)
        }
    }
}
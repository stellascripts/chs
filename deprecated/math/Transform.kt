package com.chiaroscuro.chiaroscuro.math

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Transform {
    private var tX = 0f
    private var tY = 0f
    private var tZ = 0f

    private var rA = 1f
    private var rXY = 0f
    private var rXZ = 0f
    private var rYZ = 0f

    private var sX = 1f
    private var sY = 1f
    private var sZ = 1f

    fun setTranslation(x: Float, y: Float, z: Float) {
        this.tX = x
        this.tY = y
        this.tZ = z
    }
    fun setTranslation(v: Vec3) = setTranslation(v.x, v.y, v.z)

    fun move(x: Float, y: Float, z: Float) = setTranslation(tX + x, tY + y, tZ + z)

    fun setRotation(a: Float, xy: Float, xz: Float, yz: Float) {
        val msq = a*a+xy*xy+xz*xz+yz*yz
        if(msq approx 1f) {
            rA = a
            rXY = xy
            rXZ = xz
            rYZ = yz
        } else {
            val m = sqrt(msq)
            rA = a/m
            rXY = xy/m
            rXZ = xz/m
            rYZ = yz/m
        }
    }
    //fun setRotation(r: Rotor) = setRotation(r.a, r.xy, r.yz, r.xz)
    fun rotate(a: Float, xy: Float, xz: Float, yz: Float) {
        val na = a * rA - xy * rXY - xz * rXZ - yz * rYZ
        val nxy = xy * rA + a * rXY + yz * rXZ - xz * rYZ
        val nxz = xz * rA + a * rXZ - yz * rXY + xy * rYZ
        val nyz = yz * rA + a * rYZ + xz * rXY - xy * rXZ
        rA = na
        rXY = nxy
        rXZ = nxz
        rYZ = nyz
    }

    fun setRotationAngle(angle: Float, pxy: Float, pxz: Float, pyz: Float) {
        val n = -sin(angle / 2f)
        setRotation(
            a = cos(angle / 2f),
            xy = n * pxy,
            xz = n * pxz,
            yz = n * pyz
        )
    }

    fun setRotation(from: Vec3, to: Vec3) {
        val a = 1f + (to dot from)
        val xy = to.x * from.y - to.y * from.x
        val xz = to.x * from.z - to.z * from.x
        val yz = to.y * from.z - to.z * from.y
        setRotation(a, xy, xz, yz)
    }

    fun setScale(x: Float, y: Float, z: Float) {
        sX = x
        sY = y
        sZ = z
    }
    fun setScale(s: Float) = setScale(s,s,s)
    fun setScale(v: Vec3) = setScale(v.x, v.y, v.z)

    fun scaleBy(x: Float, y: Float, z: Float) = setScale(sX*x, sY*y, sZ*z)
    fun scaleBy(s: Float) = scaleBy(s,s,s)

    fun invertInPlace() {
        tX = -tX
        tY = -tY
        tZ = -tZ
        rA = -rA
        sX = 1f/sX
        sY = 1f/sY
        sZ = 1f/sZ
    }

    fun doRotation(vx: Float, vy: Float, vz: Float, m: FloatArray, off: Int) {
        val a = rA
        val xy = rXY
        val xz = rXZ
        val yz = rYZ

        val tx = a * vx + vy * xy + vz * xz
        val ty = a * vy - vx * xy + vz * yz
        val tz = a * vz - vx * xz - vy * yz
        val txyz = vx * yz + vy * xz - vz * xy

        m[off+M00] = a * tx + ty * xy + tz * xz - txyz * yz
        m[off+M10] = a * ty - tx * xy + txyz * xz + tz * yz
        m[off+M20] = a * tz - txyz * xy - tx * xz - ty * yz
    }

    fun write(inverse: Boolean, m: FloatArray) {
        doRotation(sX,0f,0f, m,0)
        doRotation(0f,sY,0f, m, 1)
        doRotation(0f, 0f, sZ, m, 2)
        m[M03] = tX
        m[M13] = tY
        m[M23] = tZ
        m[M30] = 0f
        m[M31] = 0f
        m[M32] = 0f
        m[M33] = 1f
        if(inverse) Mat4A.invert(m)
    }

    fun multiply(inverse: Boolean, m: FloatArray, store: FloatArray) {
        Mat4A.dealias(store, m===store) { s ->
            write(inverse, s)
            Mat4A.multiply4x4(m, s, s)
        }
    }

    fun apply(v: Vec3): Vec3 {
        val vsX = v.x * sX
        val vsY = v.y * sY
        val vsZ = v.z * sZ
        val x = rA * vsX + vsY * rXY + vsZ * rXZ
        val y = rA * vsY - vsX * rXY + vsZ * rYZ
        val z = rA * vsZ - vsX * rXZ - vsY * rYZ
        val xyz = vsX * rYZ + vsY * rXZ - vsZ * rXY

        return Vec3(
            x = rA * x + y * rXY + z * rXZ - xyz * rYZ + tX,
            y = rA * y - x * rXY + xyz * rXZ + z * rYZ + tY,
            z = rA * z - xyz * rXY - x * rXZ - y * rYZ + tZ
        )
    }
}
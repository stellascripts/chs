package chs

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.joml.Matrix4f
import org.joml.Vector3f

private fun vec(x: Float, y: Float, z: Float) = Vector3f(x,y,z)

class TransformSpec : StringSpec({
    "Transform: Translation" {
        val transform = Transform()
        transform.position.set(5f, 3f, 1f)
        val v = vec(1f, 1f, 1f)
        transform.applyPoint(v) shouldBe vec(6f, 4f, 2f)
    }

    "Transform: Rotation" {
        val transform = Transform()
        transform.rotation.fromAxisAngleDeg(0f, 0f, 1f, 90f)
        transform.applyPoint(vec(1f,0f,0f)) should beApprox(0f,1f,0f)
    }

    "Transform: Scale" {
        val transform = Transform()
        transform.scale = 5f
        transform.applyPoint(vec(3f,2f,1f)) should beApprox(15f, 10f, 5f)
    }

    "Rotation + Translation" {
        val transform = Transform()
        transform.position.set(0f,5f,0f)
        transform.rotation.fromAxisAngleDeg(0f,1f,0f, 90f)
        transform.applyPoint(vec(1f,0f,0f)) should beApprox(0f,5f,-1f)
    }

    "Matrix Write" {
        val m = Matrix4f().zero()
        val transform = Transform()
        transform.position.set(0f,5f,0f)
        transform.rotation.fromAxisAngleDeg(0f, 1f, 0f, 90f)
        transform.scale = 2f
        transform.writeTo(m)
        m.transformPosition(vec(1f,0f,0f)) should beApprox(0f,5f,-2f)
    }

    "Matrix Multiply" {
        val angleA = 65f
        val angleB = 25f

        val m = Matrix4f().zero()
        val mTrans = Transform()
        mTrans.position.set(0f, 5f, 0f)
        mTrans.rotation.fromAxisAngleDeg(0f, 1f, 0f, angleA)
        mTrans.writeTo(m)

        val transform = Transform()
        transform.rotation.fromAxisAngleDeg(0f, 1f, 0f, angleB)
        transform.scale = 2f

        val n = Matrix4f().zero()
        transform.writeTo(n)

        m.mul(n)

        m.transformPosition(vec(1f,0f,0f)) should beApprox(0f,5f,-2f)
    }

    "Matrix Identity" {
        val a = Transform()
        val b = Transform()
        a.scale = 2f
        b.scale = 2f
        a.rotation.fromAxisAngleDeg(1f, 0f, 0f, 45f)
        b.rotation.fromAxisAngleDeg(1f, 0f, 0f, 45f)
        a.position.set(2f, 0f, 0f)
        b.position.set(2f, 0f, 0f)
        val r = Matrix4f()
        val s = Matrix4f()
        a.writeInverse(s)
        b.writeTo(r)
        r.mul(s)
        r.determineProperties()
        (r.properties() and Matrix4f.PROPERTY_IDENTITY.toInt() > 0) shouldBe true
    }
})
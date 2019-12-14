package com.chiaroscuro.chiaroscuro

import com.chiaroscuro.chiaroscuro.math.Mat4A
import io.kotlintest.specs.StringSpec
import org.opentest4j.AssertionFailedError
import java.lang.Math

infix fun FloatArray.shouldBe(q: FloatArray) {
    for(i in 0..3) {
        for(j in 0..3) {
            if(this[j*4+i] != q[j*4+i]) {
                val pS = Mat4A.toString(this)
                val rS = Mat4A.toString(q)
                val msg = buildString {
                    append("Matrices are not equivalent.\n")
                    append("Actual\n")
                    append(pS)
                    append("Expected\n")
                    append(rS)
                }
                throw AssertionFailedError(msg,
                    pS,
                    rS
                )
            }
        }
    }
}

class Mat4ATest : StringSpec({
    "Identity Property" {
        val I = Mat4A.loadIdentity()
        val Q = FloatArray(16) { Math.random().toFloat() }
        val R = FloatArray(16)

        Mat4A.multiply4x4(Q, I, R)
        R shouldBe Q

        Mat4A.multiply4x4(I, Q, R)
        R shouldBe Q
    }
    "AB != BA" {
        val P = floatArrayOf(
            0f, 1f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f
        )
        val Q = floatArrayOf(
            0f, 0f, 0f, 0f,
            1f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f
        )
        val R = FloatArray(16)
        Mat4A.multiply4x4(P, Q, R)
        R shouldBe floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f
        )
        Mat4A.multiply4x4(Q, P, R)
        R shouldBe floatArrayOf(
            0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f
        )
    }
})
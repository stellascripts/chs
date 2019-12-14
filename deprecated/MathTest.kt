package com.chiaroscuro.chiaroscuro

import com.chiaroscuro.chiaroscuro.math.approx
import io.kotlintest.specs.StringSpec

class MathTest : StringSpec({
    "Approximation" {
        1f approx 1f shouldBe true
        0.999999999999999999999999999999999999999f approx 1f shouldBe true
        0.99f approx 1f shouldBe false
        5f approx 5f shouldBe true
    }
})
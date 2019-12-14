package com.chiaroscuro.chiaroscuro.mathpages

val rotateEq = """
        tx = a * v.x + v.y * xy + v.z * xz
        ty = a * v.y - v.x * xy + v.z * yz
        tz = a * v.z - v.x * xz - v.y * yz
        txyz = v.x * yz + v.y * xz - v.z * xy

        R.x = a * tx + ty * xy + tz * xz - txyz * yz
        R.y = a * ty - tx * xy + txyz * xz + tz * yz
        R.z = a * tz - txyz * xy - tx * xz - ty * yz
    """.trimIndent().toFormula()

fun rotationEq(x: String, y: String, z: String): Formula {
    val f = rotateEq.copy()
    f.substitute("v.x" to x, "v.y" to y, "v.z" to z)
    f.simplify("tx", "ty", "tz", "txyz")
    f.substitute("txyz","tx", "ty", "tz")
    f.simplify("R.x", "R.y", "R.z")
    return f
}

val matrixEq = """
        m[0] = vx.x
        m[1] = vy.x
        m[2] = vz.x
        m[3] = tx

        m[4] = vx.y
        m[5] = vy.y
        m[6] = vz.y
        m[7] = ty

        m[8] = vx.z
        m[9] = vy.z
        m[10] = vz.z
        m[11] = tz

        m[12] = 0
        m[13] = 0
        m[14] = 0
        m[15] = 1
    """.trimIndent().toFormula()
    .apply {
        val rotateX = rotationEq("sX", "0", "0")
        val rotateY = rotationEq("0", "sY", "0")
        val rotateZ = rotationEq("0", "0", "sZ")

        substituteFrom(rotateX, "R.x" to "vx.x", "R.y" to "vx.y", "R.z" to "vx.z")
        substituteFrom(rotateY, "R.x" to "vy.x", "R.y" to "vy.y", "R.z" to "vy.z")
        substituteFrom(rotateZ, "R.x" to "vz.x", "R.y" to "vz.y", "R.z" to "vz.z")
    }

fun main() {
    println("Resulting Matrix: ")
    /*println(Regex("m\\[([0-9]+)\\] = ").replace(matrixEq.toString()) {
        "${it.groupValues[1]} -> "
    })*/
    matrixEq.substitute(
        "a" to "rA",
        "xy" to "rXY",
        "xz" to "rXZ",
        "yz" to "rYZ",
        "sx" to "sX",
        "sy" to "sY",
        "sz" to "sZ",
        "tx" to "tX",
        "ty" to "tY",
        "tz" to "tZ"
    )
    matrixEq.simplify()

    println(Regex("m\\[([0-9]+)\\] = ").replace(matrixEq.toString()) {
        "${it.groupValues[1]} -> "
    })

    /*val repls = listOf(
        "m\\\\[([0-9]+)\\\\] = " to "~ -> ",
        "a" to "rA",
    )*/
}
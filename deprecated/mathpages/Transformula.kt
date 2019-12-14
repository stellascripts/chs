package com.chiaroscuro.chiaroscuro.mathpages

fun main() {
    /*val eq = """
        vsX = v.x * sX
        vsY = v.y * sY
        vsZ = v.z * sZ
    """.trimIndent().toFormula()
    eq.compose(rotateEq)
    eq.rename(
        "tx" to "x",
        "ty" to "y",
        "tz" to "z",
        "txyz" to "xyz",
        "R.x" to "rx",
        "R.y" to "ry",
        "R.z" to "rz",
        "xy" to "rXY",
        "xz" to "rXZ",
        "yz" to "rYZ",
        "a" to "rA"
    )
    eq.substitute(
        "v.x" to "v.x * sX",
        "v.y" to "v.y * sY",
        "v.z" to "v.z * sZ"
    )
    eq.append(
        " + tX" to "rx",
        " + tY" to "ry",
        " + tZ" to "rz"
    )8
    eq.simplify()*/
    val eq = rotateEq
    eq.substitute("tx", "ty", "tz", "txyz")
    eq.simplify()
    println(eq)
}
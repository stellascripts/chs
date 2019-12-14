package com.chiaroscuro.chiaroscuro.mathpages



fun main() {
    val singleMultiply = "s[ji] = m[aj] * n[ia] + m[bj] * n[ib] + m[cj] * n[ic] + m[dj] * n[id]"
    val f = Formula()
    for(i in 0..3) {
        for(j in 0..3) {
            //val sm = singleMultiply.copy()
            val sm = Formula(singleMultiply.substitute(
                "s[ji]" to "s[${4*j+i}]",
                "m[aj]" to "m[${4*0+j}]",
                "m[bj]" to "m[${4*1+j}]",
                "m[cj]" to "m[${4*2+j}]",
                "m[dj]" to "m[${4*3+j}]",
                "n[ia]" to "n[${4*i+0}]",
                "n[ib]" to "n[${4*i+1}]",
                "n[ic]" to "n[${4*i+2}]",
                "n[id]" to "n[${4*i+3}]"
            ))
            sm.substituteFrom(matrixEq)
            sm.simplify()
            f.compose(sm)
        }
    }
    //f.substituteFrom(matrixEq)
    println("Multiplication: ")
    println(f)
}
package com.chiaroscuro.chiaroscuro.mathpages

import java.lang.StringBuilder
import java.nio.CharBuffer
import java.util.*

class Formula() {
    private val equations = ArrayList<Equation>()

    data class Equation(var name: String, var expansion: String)

    constructor(formulas: String): this() {
        append(formulas)
    }

    fun copy(): Formula {
        val f = Formula()
        for((v, x) in equations) {
            f.equations.add(Equation(v,x))
        }
        return f
    }

    fun compose(f: Formula) {
        for((v,x) in f.equations) {
            equations.add(Equation(v,x))
        }
    }

    fun append(f: String) {
        val matches = EQUATION_PATTERN.findAll(f)
        for(m in matches) {
            val name = m.groupValues[1]
            val exp = m.groupValues[2]
            equations.add(Equation(name, exp))
        }
    }

    fun append(vararg mods: Pair<String, String>) {
        for(eq in equations) {
            for(m in mods) {
                if (eq.name == m.second) eq.expansion += m.first
            }
        }
    }

    fun substitute(vararg variables: String) {
        val matchedEquations = equations.filter { variables.isEmpty() or (it.name in variables) }
        for(e in equations) {
            if(e.name in variables) continue
            for(m in matchedEquations) {
                e.expansion = VARIABLE_PATTERN.replace(e.expansion) {
                    if(it.value == m.name) "(${m.expansion})"
                    else it.value
                }
            }
        }
        equations.removeAll(matchedEquations)
    }

    fun substitute(vararg variables: Pair<String, String>) {
        for(e in equations) {
            for((a, b) in variables) {
                e.expansion = e.expansion.replace(a,"($b)")
            }
        }
    }

    fun substituteFrom(other: Formula, vararg conversions: Pair<String, String>) {
        for(oeq in other.equations) {
            var converted = oeq.name
            for(c in conversions) {
                if(converted == c.first) converted = c.second
            }
            for(eq in equations) {
                eq.expansion = eq.expansion.replace(converted, "(${oeq.expansion})")
            }
            //equations.add(Equation(converted, eq.expansion))
        }
    }

    fun rename(vararg conversions: Pair<String, String>) {
        for(c in conversions) {
            for(eq in equations) {
                eq.expansion = VARIABLE_PATTERN.replace(eq.expansion) {
                    if(it.value == c.first) c.second
                    else it.value
                }
                if(eq.name == c.first) {
                    eq.name = c.second
                }
            }
        }
    }

    fun simplify(vararg variables: String) {
        val matchedEquations = equations.filter { variables.isEmpty() or (it.name in variables) }
        for(m in matchedEquations) {
            m.expansion = Companion.simplify(m.expansion)
        }
    }

    override fun toString(): String = buildString{
        for(eq in equations) {
            append(eq.name)
            append(" = ")
            append(eq.expansion)
            append("\n")
        }
    }

    companion object {
        val VAR_PATTERN_STR = "[^=+\\-*/()\\s0-9]\\S*"
        val ELE_PATTERN_STR = "[^=+\\-*/()\\s]\\S*"
        val VARIABLE_PATTERN = Regex(VAR_PATTERN_STR) //any variable
        val EQUATION_PATTERN = Regex("($VAR_PATTERN_STR)\\s*=\\s*([^\\n]+)") //V = formula

        val MULTIPLY_PATTERN = Regex("($ELE_PATTERN_STR)\\s*\\*\\s*($ELE_PATTERN_STR)") //A * B
        val ADD_PATTERN = Regex("($ELE_PATTERN_STR)\\s*[+-]\\s*($ELE_PATTERN_STR)")//A + B
        val PARENS_PATTERN = Regex("\\((-?$ELE_PATTERN_STR)\\)")
        val NUMBER_PATTERN = Regex("[0-9]+\\.?[0-9]*")
        val ADD_NEGATIVE_PATTERN = Regex("[+\\-]\\s*-")

        fun simplify(s: String): String {
            var r = s
            r = applyUntilIdentical(
                r,
                MULTIPLY_PATTERN,
                this::multiplicative
            )
            r = PARENS_PATTERN.replace(r) { it.groupValues[1] }
            r = ADD_NEGATIVE_PATTERN.replace(r) {
                if(it.value[0] == '-') "+"
                else "-"
            }
            r = applyUntilIdentical(
                r,
                ADD_PATTERN,
                this::additive
            )
            r = PARENS_PATTERN.replace(r) { it.groupValues[1] }
            r = ADD_NEGATIVE_PATTERN.replace(r) {
                if(it.value[0] == '-') "+"
                else "-"
            }

            return r
        }

        fun applyUntilIdentical(s: String, r: Regex, tf: (MatchResult)->String): String {
            var result = s
            while(true) {
                val q = r.replace(result, tf)
                if(q == result) break
                result = q
            }
            return result
        }

        fun multiplicative(match: MatchResult): String {
            val a = match.groupValues[1]
            val b = match.groupValues[2]
            if(a == "0" || b == "0"){
                return "0"
            } //cancellation
            if(a == "1") {
                return b
            }
            if(b == "1") {
                return a
            }
            return match.value
        }
        fun additive(match: MatchResult): String {
            val a = match.groupValues[1]
            val b = match.groupValues[2]
            val sub = match.value.contains('-')
            if(a == "0") {
                if(sub)  {
                    return "-$b"
                } else {
                    return b
                }
            }
            if(b == "0") {
                return a
            }
            return match.value
        }
    }
}

class Replacement(ex: String, val repl: String) {
    val rex = Regex(ex)
}

fun StringBuilder.appendSub(str: String, start: Int = 0, end: Int = str.length): Int {
    for(i in start until end) {
        append(str[i])
    }
    return end-start
}

fun MatchResult.putWithGroups(replacement: String, buf: StringBuilder = StringBuilder()) {
    //replace ~ in replacement string with groups
    var prev = 0
    var groupNum = 1
    while(true) {
        val group = replacement.indexOf('~', prev)
        if(group < 0) break
        buf.appendSub(replacement, prev, group)
        buf.appendSub(groupValues[groupNum++])
        prev = group + 1
    }
    buf.appendSub(replacement, prev)
}

fun Replacement.replace(input: String) {
    Collections.singletonList(this).replace(input)
}

fun List<Replacement>.replace(input: String): String {
    val buf = StringBuilder()
    val repls = Array(this.size){ this[it].rex.find(input,0) }
    var off = 0

    while(true) {
        //find the match closest to the origin
        var leastIdx = -1
        for(i in 0 until size) {
            val m = repls[i]?:continue
            if(leastIdx < 0 || m.range.first < repls[leastIdx]!!.range.first) leastIdx = i
        }
        if(leastIdx < 0) break

        val m = repls[leastIdx]!!
        //append from last point up to beginning of match
        buf.appendSub(input, off, m.range.first)
        //replacement string
        val rs = this[leastIdx].repl

        m.putWithGroups(rs, buf)

        //advance offset to end of match, reset replacement function
        off = m.range.last+1
        repls[leastIdx] = this[leastIdx].rex.find(input, off)
    }
    return buf.toString()
}

fun String.toFormula() = Formula(this)

fun String.substitute(vararg pairs: Pair<String, String>): String {
    var r = this
    for((k,v) in pairs) {
        r = r.replace(k,v)
    }
    return r
}
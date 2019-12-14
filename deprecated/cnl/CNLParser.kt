package chs.cnl

import chs.API
import chs.Lookahead
import java.io.Reader

/**
 * Parses Ciel Notation Language files.
 */
@API
class CNLParser(reader: Reader) {
    private companion object {
        fun Char.isWordChar() = this.isLetter() || this in "-_"
        val FLOAT = "[+\\-]?[0-9]+[.][0-9]+(?:[eE][+\\-]?[0-9]+)".toRegex()
        val DEC2 = "1[xX][01]+".toRegex()
        val DEC16 = "0[xX][0-9A-Fa-f]+".toRegex()
        val DEC10 = "[+\\-][0-9]+".toRegex()
    }

    private val cnl = CNL()
    private val unnamed = HashMap<String, Int>()
    private var module: String = ""

    private val chars = Lookahead(reader, " \r\t")

    /**
     * Parses a readable Ciel Notation Language structure.
     */
    fun parse(): ReadableCNL {
        while(true) {
            if(!parseModuleName()) if(!parseTable()) break
        }
        return cnl
    }

    private fun String.expect(string: String) {
        if(this != string) error("Expected $string, got $this.")
    }

    private fun expectNewlines() {
        val next = chars.readWhile { it in chars.skip || it == '\n' }
        if('\n' !in next) next.expect("\n")
    }

    private fun parseModuleName(): Boolean {
        (chars.tryReadWhile { it == '=' }?:return false).expect("==")
        val name = chars.readWhile { it.isWordChar() }
        if(name.isBlank()) error("Invalid module name.")
        chars.readWhile { it == '=' }.expect("==")
        expectNewlines()
        module = name
        return true
    }

    private fun parseLiteral(): Any {
        val value = chars.readWhile { it != '\n' }.trim()
        return when {
            value.startsWith('\"') -> value.substring(1,value.length-1)
            value == "+inf" || value == "inf" -> Double.POSITIVE_INFINITY
            value == "nan" -> Double.NaN
            value matches FLOAT -> value.toDouble()
            value matches DEC10 -> value.toLong()
            value matches DEC16 -> value.substring(2).toLong(16)
            value matches DEC2 -> value.substring(2).toLong(2)
            value == "false" -> false
            value == "true" -> true
            value.startsWith('@') -> CNL.Atom(value.substring(1))
            else -> error("Invalid literal: $value")
        }
    }

    private fun parseField(): Pair<String, Any>? {
        val name = chars.tryReadWhile { it.isWordChar() }?:return null
        chars.readWhile { it == ':' }.expect(":")
        return name to parseLiteral()
    }

    private fun parseTable(): Boolean {
        (chars.tryReadWhile { it == ':' }?:return false).expect("::")
        val type = chars.readWhile { it.isWordChar() }

        var name: String

        chars.skipWhile { it in chars.skip }
        val n = chars.tryReadWhile { it == ':' }?:error("Invalid table name.")
        if(n.length == 1) {
            name = chars.tryReadWhile { it.isWordChar() }?:""
            chars.readWhile { it == ':' }.expect("::")
        } else {
            name = ""
            n.expect("::")
        }
        if(name.isBlank()) {
            name = unnamed.getOrDefault(type, -1).inc().toString()
        }
        expectNewlines()
        while(true) {
            val field = parseField()?:break
            cnl.put("$module.$type.$name.${field.first}", field.second)
        }
        return true
    }
}
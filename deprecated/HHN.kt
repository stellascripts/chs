package chs.assets

import java.io.Reader

class HHNParser(val reader: Reader) {
    companion object {
        private const val whitespace = " \t\r"
        private const val newline = '\n'
        private val p_integer = """[+\-]?[0-9_]+""".toRegex()
        private val p_hexint = """16r[0-9a-fA-F_]+""".toRegex()
        private val p_binint = """2r[01_]+""".toRegex()
        private val p_float = """[+\-]?[0-9_]*[.][0-9_]+(?:[Ee][+\-]?[0-9_]+)?""".toRegex()
        private val p_float2 = """[+\-]?[0-9_]*[Ee][+\-]?[0-9]+""".toRegex()
        private val p_special = """[+\-]?(inf|nan)""".toRegex()
        private val p_boolean = """(true|false)""".toRegex()
    }
    private var pushed: Char? = null
    private var eofError = "Unexpected EoF"

    private fun pushback(ch: Char?) {
        if(pushed != null) error("Pushback overwrites existing character: $ch -> $pushed")
        pushed = ch
    }

    private fun nextOrEoF(): Char? {
        if(pushed != null) {
            val p = pushed
            pushed = null
            return p
        } else {
            val i = reader.read()
            return if(i >= 0) i.toChar() else null
        }
    }

    private fun next() = nextOrEoF()?:error(eofError)

    private fun skipWhitespace(withNewlines: Boolean = false) {
        while(true) {
            val next = nextOrEoF()?:break
            if(next == newline && withNewlines) continue
            if(next !in whitespace) {
                pushback(next)
                break
            }
        }
    }

    private enum class QuoteType(val character: Char) {
        Normal('\"'),
        Literal('/')
    }

    private fun readEscapeCharacter(): Char {
        val esc = next()
        return when(esc) {
            '\\' -> '\\'
            'r' -> '\r'
            'n' -> '\n'
            't' -> '\t'
            'f' -> '\u000C'
            'b' -> '\b'
            '\"' -> '\"'
            '/' -> '/'
            'u' -> {
                var offset = 0
                for(i in 0 until 8) {
                    val ch = next()
                    val value = if(!ch.isDigit()) {
                        if(ch in 'a'..'f') ch - 'a' + 10
                        else if(ch in 'A'..'F') ch - 'A' + 10
                        else {
                            if(i == 4) {
                                pushback(ch)
                                break
                            }
                            else
                                error("Invalid unicode escape sequence: " +
                                        "\\u${offset.toString(16).padStart(4,'0')}$ch")
                        }
                    } else ch - '0'
                    offset = offset shl 4 or value
                }
                if(offset !in 0..0xD7FF && offset !in 0xE000..0x10FFFF) error("Invalid unicode escape sequence: " +
                            "\\u${offset.toString(16).padStart(4, '0')}")
                return offset.toChar()
            }
            in whitespace -> {
                skipWhitespace(true)
                return next()
            }
            else -> error("Unknown escape sequence: \\$esc")
        }
    }

    private fun parseKeychain() {
        skipWhitespace(false)
        eofError = "Unexpected EoF in Keychain"

        val keychain = ArrayList<String>()
        var mode: QuoteType? = null
        var key = StringBuilder()
        var empty = true
        while(true) {
            val n = next()
            if(mode != null) {
                empty = false
                when (n) {
                    '\\' -> key.append(readEscapeCharacter())
                    in " :" -> error("End of key reached inside <${mode.character}>")
                    mode.character -> mode = null
                    else -> key.append(n)
                }
            } else {
                if(n in " :") {
                    pushback(n)
                    if(empty) error("Empty literal key in keychain.")
                    break
                } else if(n == '.') {
                    if(empty) error("Empty literal key in keychain.")
                    keychain.add(key.toString())
                    key = StringBuilder()
                    empty = true
                } else {
                    empty = false
                    key.append(n)
                }
            }
        }
    }

    private fun parseValue() {

    }

    fun parseKeyValue() {
        val keychain = parseKeychain()
        val value = parseValue()
        return keychain to value
    }
}
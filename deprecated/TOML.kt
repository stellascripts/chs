package chs.assets

import java.io.InputStream
import java.lang.RuntimeException

typealias TOMLTable = HashMap<String, Any>
typealias ErasedTOMLTable = Map<*,*>
typealias TOMLArray = ArrayList<Any>
typealias ErasedTOMLArray = List<*>
typealias TOMLKeychain = List<String>

/**
 * Reads TOML files.
 */
class TOMLParser(stream: InputStream) {
    companion object {
        const val whitespace = "\t \r"
        const val newline = "\n"
        const val whitespaceOrNewline = "\t \r\n"
        const val quotes = "\"\'"

        fun isAllowedInBareKey(ch: Char) =
            ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9' || ch in "-_"

        private val p_integer = """[+\-]?[0-9_]+""".toRegex()
        private val p_hexint = """0x[0-9a-fA-F_]+""".toRegex()
        private val p_octint = """0o[0-7_]+""".toRegex()
        private val p_binint = """0b[01_]+""".toRegex()
        private val p_float = """[+\-]?[0-9_]*[.][0-9_]+(?:[Ee][+\-]?[0-9_]+)?""".toRegex()
        private val p_float2 = """[+\-]?[0-9_]*[Ee][+\-]?[0-9]+""".toRegex()
        private val p_special = """[+\-]?(inf|nan)""".toRegex()
        private val p_boolean = """(true|false)""".toRegex()
        private val p_datetime =
            """[0-9]{4}-[0-9]{2}-[0-9]{2}[T ][0-9]{2}:[0-9]{2}:[0-9]{2}(?:[.][0-9]+)?(?:Z|[+\-][0-9]{2}:0{2})""".toRegex()
        private val p_local_datetime =
            """[0-9]{4}-[0-9]{2}-[0-9]{2}[T ][0-9]{2}:[0-9]{2}:[0-9]{2}(?:[.][0-9]+)?""".toRegex()
        private val p_local_date = """[0-9]{4}-[0-9]{2}-[0-9]{2}""".toRegex()
        private val p_local_time = """[0-9]{2}:[0-9]{2}:[0-9]{2}(?:[.][0-9]+)?""".toRegex()

        private val EMPTY_KEYCHAIN = emptyList<String>()
    }
    private val reader = stream.reader(Charsets.UTF_8)
    private var pushed: Char? = null

    private val rootTable: TOMLTable = TOMLTable()

    private fun pushback(char: Char) { pushed = char }
    private fun read(): Char? {
        if(pushed != null) {
            val p = pushed
            pushed = null
            return p
        } else {
            val i = reader.read()
            if(i < 0) return null
            else return i.toChar()
        }
    }

    private fun skipComment() {
        var ch: Char?
        do {
            ch = read()
        } while(ch != null && ch !in newline)
        if(ch == null) return
        pushback(ch)
    }

    private fun parseNewline() {
        while(true) {
            val ch = read()?:break
            if(ch == '#') {
                skipComment()
                continue
            }
            if(ch in whitespace) continue
            if(ch in newline) break
            error("Expected new line, got $ch")
        }
    }

    private fun parseKey(): String {
        val key = StringBuilder()
        var quote: Char? = null
        while(true) {
            val ch = read()?: error("End of file before completion of key-value pair.")
            if(ch in newline) error("Newline before completion of key.")
            if(ch == quote) {
                //key.append(ch)
                break
            } else if(ch in quotes && quote == null) {
                quote = ch
                //key.append(ch)
                continue
            } else {
                if(quote != null) key.append(ch)
                else {
                    if(ch == '#') {
                        skipComment()
                        continue
                    }
                    if(ch in whitespace) continue
                    if(ch in "=.]") {
                        pushback(ch)
                        break
                    }
                    if(!isAllowedInBareKey(ch)) error("Character $ch is not allowed in bare keys.")
                    key.append(ch)
                }
            }
        }
        if(key.isBlank()) error("Blank key.")
        return key.toString()
    }

    private fun parseKeychain(): TOMLKeychain {
        val keychain = ArrayList<String>()
        while(true) {
            val k = this.parseKey()
            keychain.add(k)
            val next = read()
            if(next == '=' || next == ']') {
                pushback(next)
                break
            }
            else if(next != '.') error("Character $next is not allowed in bare keys.")

        }
        return keychain
    }

    private enum class StringType(val literal: String) {
        Normal("\""),
        Empty("\"\""),
        MultiNormal("\"\"\""),
        Literal("\'"),
        EmptyLiteral("\'\'"),
        MultiLiteral("\'\'\'");

        fun isMultiline() = this == MultiNormal || this == MultiLiteral
        fun isEmpty() = this == Empty || this == EmptyLiteral
        fun canEscape() = this == Normal || this == MultiNormal
    }

    private fun parseQuote(): StringType {
        val q = read()
        if(q == '\'') {
            val r = read()
            if(r == '\'') {
                val s = read()
                if(s == '\'') return StringType.MultiLiteral
                else {
                    if(s != null) pushback(s)
                    return StringType.EmptyLiteral
                }
            } else {
                if(r != null) pushback(r)
                return StringType.Literal
            }
        } else if(q == '\"') {
            val r = read()
            if(r == '\"') {
                val s = read()
                if(s == '\"') return StringType.MultiNormal
                else {
                    if(s != null) pushback(s)
                    return StringType.Empty
                }
            } else {
                if(r != null) pushback(r)
                return StringType.Normal
            }
        } else {
            error("Expected: \" or \', got $q")
        }
    }

    private fun parseUnicodeEscapeCharacters(): Char {
        var offset = 0
        for(i in 0 until 8) {
            val ch = read()?:error("End of file reached inside string.")
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
        if(offset !in 0..0xD7FF && offset !in 0xE000..0x10FFFF)
            error("Invalid unicode escape sequence: \\u${offset.toString(16)}")
        return offset.toChar()
    }

    private fun parseString(): String {
        val contents = StringBuilder()
        val type = parseQuote()
        if(type.isEmpty()) return ""
        if(type.isMultiline()) {
            while(true) {
                val ch = read()?:error("End of file reached inside string.")
                if(ch !in whitespace) {
                    if(ch !in newline) pushback(ch)
                    break
                }
            }
        }
        while(true) {
            val ch = read()?:error("End of file reached inside string.")
            if(!type.isMultiline() && ch in newline) {
                error("Newline inside single-line string.")
            }
            if(ch == '\\' && type.canEscape()) {
                val next = read()?:error("End of file reached inside string.")
                val literal = when(next) {
                    'b' -> '\b'
                    't' -> '\t'
                    'n' -> '\n'
                    'f' -> '\u000C'
                    'r' -> '\r'
                    '\"' -> '\"'
                    '\\' -> '\\'
                    'u' -> parseUnicodeEscapeCharacters()
                    in whitespaceOrNewline -> {
                        while(true) {
                            val ws = read()?:error("End of file reached inside string.")
                            if(ws !in whitespaceOrNewline) {
                                pushback(ws)
                                break
                            }
                        }
                        read()
                    }
                    else -> error("Unsupported escape sequence: \\$next")
                }
                contents.append(literal)
                continue
            }
            if (ch in quotes) {
                pushback(ch)
                val endType = parseQuote()
                if (endType == type) {
                    break
                } else {
                    contents.append(endType.literal)
                }
            } else {
                contents.append(ch)
            }
        }
        return contents.toString()
    }

    private fun skipWhitespace(andNewlines: Boolean = false) {
        while(true) {
            val r = read()?:break
            if(r !in whitespace && (r !in newline || !andNewlines)) {
                pushback(r)
                break
            }
        }
    }

    private fun parseArray(): List<Any> {
        val array = TOMLArray()
        var valueType: Class<*>? = null
        while(true) {
            skipWhitespace(true)
            val r = read()?:error("End of file reached inside array.")
            if(r == ']') break
            if(r == ',') continue
            pushback(r)
            val v = parseValue()
            if (valueType == null) {
                valueType = v::class.java
            } else if (v::class.java != valueType) {
                error("Arrays may not contain different types.")
            }
            array.add(v)
        }
        return array
    }

    private fun TOMLTable.putValue(keychain: TOMLKeychain, value: Any) {
        var map = this
        for(k in 0 until keychain.size - 1) {
            val next = map.getOrPut(keychain[k]) { TOMLTable() }
            map = next as TOMLTable
        }
        map[keychain.last()] = value
    }

    private fun TOMLTable.getValue(keychain: TOMLKeychain): Any? {
        var map = this
        for(k in 0 until keychain.size - 1) {
            val next = map.getOrPut(keychain[k]) { TOMLTable() }
            map = next as TOMLTable
        }
        return map[keychain.last()]
    }

    private fun parseInlineTable(): TOMLTable {
        val table = TOMLTable()
        while(true) {
            skipWhitespace(false)
            val r = read()?:error("End of file reached inside inline table.")
            if(r == '\n') error("Newline inside inline table.")
            if(r == '}') break
            if(r == ',') continue
            pushback(r)
            val (keychain, value) = parseKeyValue()
            table.putValue(keychain,value)
        }
        return table
    }

    private var currentTable: TOMLTable = rootTable
    private var currentInstance: Pair<TOMLKeychain, TOMLTable>? = null

    private fun TOMLKeychain.startsWith(other: TOMLKeychain): Boolean {
        for(i in 0 until other.size) {
            if(this[i] != other[i]) return false
        }
        return true
    }

    private fun parseAndSetCurrentTable() {
        var tableArray = false
        skipWhitespace(false)
        var r = read() ?: error("End of file reached inside table name.")
        if(r == '[') {
            tableArray = true
        } else {
            pushback(r)
        }
        val keychain = parseKeychain()
        skipWhitespace(false)
        r = read() ?: error("End of file reached inside table name.")
        if(r != ']') error("Expected ], got $r")
        if(tableArray) {
            skipWhitespace(false)
            r = read() ?: error("End of file reached inside table name.")
            if(r != ']') error("Expected ], got $r")
        }

        val instance = currentInstance
        val root = if(instance == null) rootTable else {
            if(keychain.startsWith(instance.first)) instance.second
            else rootTable
        }

        if(tableArray) {
            var array = rootTable.getValue(keychain)
            if(array !is ErasedTOMLArray) {
                array = TOMLArray()
                root.putValue(keychain, array)
            }
            array as TOMLArray
            val table = TOMLTable()
            currentTable = table
            currentInstance = keychain to table
            array.add(table)
        } else {
            val table = TOMLTable()
            currentTable = table
            currentInstance = null
            root.putValue(keychain, table)
        }
    }

    private fun parseValue(): Any {
        var r = read()?:error("End of file reached before value.")
        while(r in whitespace) {
            r = read()?:error("End of file reached before value.")
        }
        if(r in quotes) {
            pushback(r)
            return parseString()
        }
        if(r == '[') {
            return parseArray()
        }
        if(r == '{')
            return parseInlineTable()
        pushback(r)
        val builder = StringBuilder()
        while(true) {
            val ch = read()?:break
            if(ch == '#') skipComment()
            if(ch in whitespace) continue
            if(ch in newline || ch in "[]{},") {
                pushback(ch)
                break
            }
            builder.append(ch)
        }
        val literal = builder.toString()
        print(literal)
        return when {
            p_integer matches literal -> {
                println("Int")
                literal.replace("_", "").toLong()
            }
            p_float matches literal -> {
                println("Float")
                literal.replace("_", "").toDouble()
            }
            p_special matches literal -> {
                println("Special Float")
                if (literal.endsWith("nan")) Double.NaN
                else {
                    if (literal[0] == '-') Double.NEGATIVE_INFINITY
                    else Double.POSITIVE_INFINITY
                }
            }
            p_hexint matches literal -> {
                print("Hex Int ")
                val clipped = literal.substring(2).replace("_", "").toLowerCase()
                println(clipped)
                clipped.toLong(16)
            }
            p_float2 matches literal -> {
                println("Float2")
                literal.replace("_", "").toDouble()
            }
            p_binint matches literal -> {
                println("Binary Int")
                literal.substring(2).replace("_", "")
                    .toLong(2)
            }
            p_octint matches literal -> {
                println("Octal Int")
                literal.substring(2).replace("_", "")
                    .toLong(8)
            }
            p_local_time matches literal -> literal
            p_local_date matches literal -> literal
            p_local_datetime matches literal -> literal
            p_datetime matches literal -> literal
            else -> error("Invalid character: $literal")
        }
    }

    private fun parseKeyValue(): Pair<TOMLKeychain, Any> {
        val key = parseKeychain()
        skipWhitespace(false)
        val equals = read()
        if(equals != '=') error("End of file before key-value pair.")
        skipWhitespace(false)
        val value = parseValue()
        return key to value
    }

    fun parse(): TOML {
        while(true) {
            skipWhitespace(true)
            val k = read()?:break
            if(k == '[') {
                parseAndSetCurrentTable()
            } else {
                pushback(k)
                val (keychain, value) = parseKeyValue()
                parseNewline()
                currentTable.putValue(keychain,value)
            }
        }
        return TOML(rootTable)
    }

    class Exception(msg: String): RuntimeException(msg)
    private fun error(msg: String): Nothing = throw TOMLParser.Exception(msg)
}

class TOML(val value: Any) {
    private val type: Type = type(value)?:error("Created TOML object with bad value: $value")

    private fun getValue(keychain: String): Any? {
        if(type != Type.Table) error("Cannot get $keychain")

        val keychainList = toKeychain(keychain)
        var map = value as TOMLTable
        for(k in 0 until keychainList.size - 1) {
            val next = map[keychainList[k]]
            if(next == null)
            map = next as TOMLTable
        }
        return map[keychainList.last()]
    }

    operator fun get(keychain: String): TOML? {
        val v = getValue(keychain)
        return if(v != null) TOML(v) else null
    }

    fun getI(keychain: String): Long? = getValue(keychain) as? Long
    fun getN(keychain: String): Double? = getValue(keychain) as? Double
    fun getS(keychain: String): String? = getValue(keychain) as? String
    fun getB(keychain: String): Boolean? = getValue(keychain) as? Boolean
    fun getA(keychain: String): List<*>? = getValue(keychain) as? List<*>
    fun getM(keychain: String): Map<*,*>? = getValue(keychain) as? Map<*,*>

    fun getI(i: Int): Long? = this.asList()[i] as? Long
    fun getN(i: Int): Double? = this.asList()[i] as? Double
    fun getS(i: Int): String? = this.asList()[i] as? String
    fun getB(i: Int): Boolean? = this.asList()[i] as? Boolean
    fun getA(i: Int): List<*>? = this.asList()[i] as? List<*>
    fun getM(i: Int): Map<*,*>? = this.asList()[i] as? Map<*,*>

    operator fun get(i: Int) : TOML? {
        if(type != Type.Array) error("Cannot get [$i]")
        value as TOMLArray
        val v = value[i]
        return if(v != null) TOML(v) else null
    }

    fun asDouble() = value as Double
    fun asLong() = value as Long
    fun asString() = value as String
    fun asList() = value as TOMLArray
    fun asMap() = value as TOMLTable
    fun asBoolean() = value as Boolean

    override fun toString(): String = when(type) {
            Type.Array -> {
                "TOML${TOML.toString(value)}"
            }
            Type.Table -> {
                "TOML${TOML.toString(value)}"
            }
            else -> "TOML($value)"
    }

    enum class Type {
        Array,
        Table,
        String,
        Integer,
        Float,
        Boolean
    }

    companion object {
        fun type(v: Any): Type? = when(v) {
            is Boolean -> Type.Boolean
            is Double -> Type.Float
            is Long -> Type.Integer
            is String -> Type.String
            is ErasedTOMLArray -> Type.Array
            is ErasedTOMLTable -> Type.Table
            else -> null
        }
        fun toString(v: Any): String = when(type(v)) {
            Type.Array -> (v as TOMLArray).joinToString(", ", "[","]") {
                TOML.toString(it)
            }
            Type.Table -> (v as TOMLTable).entries.joinToString(", ", "{", "}") {
                "${it.key} = ${toString(it.value)}"
            }
            else -> v.toString()
        }

        private fun parseKey(text: String): String {
            val key = StringBuilder()
            var quote: Char? = null
            var i = 0
            while(i < text.length) {
                val ch = text[i++]
                if(ch == '\n') error("Newline before completion of key.")
                if(ch == quote) {
                    break
                } else if(ch in TOMLParser.quotes && quote == null) {
                    quote = ch
                    continue
                } else {
                    if(quote != null) key.append(ch)
                    else {
                        if(ch in TOMLParser.whitespace) continue
                        if(ch == '.') {
                            break
                        }
                        if(!TOMLParser.isAllowedInBareKey(ch))
                            throw TOMLParser.Exception("Character $ch is not allowed in bare keys.")
                        key.append(ch)
                    }
                }
            }
            if(key.isBlank()) throw TOMLParser.Exception("Blank key.")
            return key.toString()
        }

        fun toKeychain(text: String): TOMLKeychain {
            val keychain = ArrayList<String>()
            var i = 0
            while(i < text.length) {
                val k = this.parseKey(text.substring(i))
                i += k.length
                keychain.add(k)
                if(i >= text.length) break
                val next = text[i++]
                if(next != '.') error("Character $next is not allowed in bare keys.")
            }
            return keychain
        }
    }
}
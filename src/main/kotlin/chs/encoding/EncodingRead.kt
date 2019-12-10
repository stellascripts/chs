package chs.encoding

import chs.API
import java.io.Reader

/**
 * Reads a message table using the provided text reader. Caller is responsible for closing the reader.
 */
@API
fun Reader.readTable(): MessageTable = MessageReader(this).readTable()

private class MessageReader(private val reader: Reader, private val format: MessageFormat = DefaultFormat) {

    private var push: Char = NUL
    private val textBuffer = StringBuilder()

    private fun next(): Char {
        return if(push != NUL) {
            val p = push
            push = NUL
            p
        } else {
            val i = reader.read()
            if(i <= 0) NUL
            else i.toChar()
        }
    }

    private fun text(): String {
        textBuffer.setLength(0)
        loop@while(true) {
            when(val n = next()) {
                NUL -> break@loop
                format.escape -> {
                    val escaped = next()
                    textBuffer.append(escaped)
                }
                format.keyStart, format.valueStart,
                format.tableStart, format.tableEnd -> {
                    push = n
                    break@loop
                }
                else -> textBuffer.append(n)
            }
        }
        return textBuffer.toString()
    }

    fun readTable() : MessageTable {
        val table = MessageTable()
        loop@while (true) {
            var mode = next()
            when (mode) {
                NUL, format.tableEnd -> break@loop
                format.keyStart -> {
                    val key = text()
                    mode = next()
                    when (mode) {
                        NUL -> break@loop
                        format.valueStart -> table[key] = text()
                        format.tableStart -> table[key] = readTable()
                        else -> continue@loop
                    }
                }
            }
        }
        return table
    }
}
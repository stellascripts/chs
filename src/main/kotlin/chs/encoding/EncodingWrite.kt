package chs.encoding

import chs.API
import java.io.Writer

/**
 * Writes a message table to the provided writer, using the provided format, or a default format if none is provided.
 * Caller is responsible for closing.
 */
@API
fun Writer.writeTable(table: Map<*,*>, format: MessageFormat = DefaultFormat) {
    for((k,v) in table.entries) {
        append(format.keyStart)
        appendEscaped(k.toString(), format)
        if(v is Map<*,*>) {
            append(format.tableStart)
            writeTable(v)
            append(format.tableEnd)
        } else {
            append(format.valueStart)
            appendEscaped(v.toString(), format)
        }
    }
}

private fun Writer.appendEscaped(string: String, format: MessageFormat) {
    for(i in string.indices) {
        val ch = string[i]
        when(ch) {
            NUL, format.tableStart, format.tableEnd, format.keyStart,
            format.valueStart, format.escape -> append(CompactFormat.escape)
        }
        append(ch)
    }
}
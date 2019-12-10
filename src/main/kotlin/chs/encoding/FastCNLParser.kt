package chs.encoding

import java.io.Reader

/**
 * Exception while parsing configuration files.
 */
class ConfigException(msg: String): RuntimeException(msg)

/**
 * Parses config files written in Chs Config Notation:
 * ==A==        The title of the config table. Usually not read by loaders.
 * ::A::        All key/value pairs below are appended to table A until a new table is named.
 * ::A: B::     All key/value pairs below are appended to table A.B until a new table is named.
 * A: B         Key/Value pair to be added to the nearest table above, or the root if no table is named.
 */
fun Reader.parseChsConfig(): MessageTable {
    val root = MessageTable()
    var table = root
    forEachLine {
        if(it.isBlank()) return@forEachLine
        else if(it.startsWith("==")) {
            val title = it.substring(2, it.lastIndex - 2).trim().toLowerCase()
            table["CNL_title"] = title
        }
        else if(it.startsWith("::")) {
            val split = it.indexOf(':', 2)
            if(split == -1) throw ConfigException("Incomplete table header: $it")
            val type = it.substring(2, split).trim().toLowerCase()
            val name = if(it[split+1] == ':') "" else it.substring(split+1, it.lastIndex - 1).trim().toLowerCase()

            table = root.createTable(type)
            if(name.isNotEmpty()) {
                table = table.createTable(name)
            }
        } else {
            val split = it.indexOf(':', 0)
            if(split == -1) throw ConfigException("Incomplete key-value line: $it")
            val key = it.substring(0, split).trim()
            val value = it.substring(split+1).trim()
            table[key] = value
        }
    }
    return root
}
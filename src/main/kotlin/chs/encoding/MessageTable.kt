package chs.encoding

/**
 * Hash table containing key-value pairs, where keys are always strings, and values may be strings or subtables.
 */
typealias MessageTable = LinkedHashMap<String, Any>

/**
 * Get the text value of a particular key, or null if there is no such value.
 */
fun MessageTable.valueOf(key: String) = this[key] as String?

/**
 * Get the subtable value of a particular key, or null if there is no such table.
 */
@Suppress("UNCHECKED_CAST")
fun MessageTable.tableOf(key: String) = this[key] as MessageTable?

/**
 * Get the subtable value of a particular key, or create that table if no such table existed.
 */
@Suppress("UNCHECKED_CAST")
fun MessageTable.createTable(key: String) = getOrPut(key) { MessageTable() } as MessageTable

/**
 * Iterates through entries in the table. The second argument will be non-null if the value was text,
 * and the third argument will be non-null if the value was a table. If one argument is null, the other is non-null,
 * and vice versa.
 */
@Suppress("UNCHECKED_CAST")
inline fun MessageTable.tableIterate(fn: (key: String, value: String?, table: MessageTable?) -> Unit) {
    for(e in this.entries) {
        val v = e.value
        if(v is String) fn(e.key, v, null)
        else if(v is LinkedHashMap<*,*>) fn(e.key, null, v as MessageTable)
    }
}
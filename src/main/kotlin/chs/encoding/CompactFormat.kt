package chs.encoding

/**
 * A default format to use for MessageFormat parameters.
 */
typealias DefaultFormat = ReadableFormat

/**
 * The null character, used in messages to indicate EoF.
 */
const val NUL = '\u0000'

/**
 * Renders messages in a format which uses unprinted control characters, freeing keys and values to contain all printable characters.
 */
object CompactFormat: MessageFormat {
    override val keyStart = '\u0001'
    override val valueStart = '\u0002'
    override val tableStart = '\u001D'
    override val tableEnd = '\u001E'
    override val escape = '\u001B'
}

/**
 * Renders messages in a semi-readable format, with newlines beginning new keys, {} as table markers, and : as value start.
 */
object ReadableFormat: MessageFormat {
    override val keyStart: Char get() = '\n'
    override val tableStart: Char get() = '{'
    override val tableEnd: Char get() = '}'
    override val valueStart: Char get() = ':'
    override val escape: Char get() = '\\'
}

/**
 * Describes characters used in message readers and writers to delimit data.
 */
interface MessageFormat {
    /**
     * Character used to indicate the start of a key.
     */
    val keyStart: Char
    /**
     * Character used to indicate the start of a text value.
     */
    val valueStart: Char
    /**
     * Character used to indicate the start of a table.
     */
    val tableStart: Char
    /**
     * Character used to indicate the end of a table.
     */
    val tableEnd: Char
    /**
     * Character for escaping other control characters used by message formats.
     */
    val escape: Char
}
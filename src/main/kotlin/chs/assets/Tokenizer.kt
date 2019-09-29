package chs.assets

import java.io.Reader

internal class Tokenizer(private val r: Reader) {
    private var pushed: Char? = null

    private fun Int.toNullableChar(): Char? = if (this < 0) null else this.toChar()

    fun next(): Char? {
        return if(pushed != null) {
            val p = pushed
            pushed = null
            p
        } else {
            if(!r.ready()) null
            else r.read().toNullableChar()
        }
    }

    /*fun skipUntil(dontSkip: String) {
        //if (!markSupported()) error("Reader does not support marking.")
        while (true) {
            val ch = next() ?: break
            if (ch in dontSkip) {
                pushed = ch
                break
            }
        }
    }*/

    fun readNextWord(): String? {
        val str = StringBuilder()
        var start = false
        while (true) {
            //mark(1)
            val ch = this.next() ?: return if(!str.isEmpty()) str.toString() else null
            if (ch == '\n') {
                if (!start) return "\n"
                else {
                    pushed = ch
                    break
                }
            }
            if (ch in " \r\t") {
                if (!start) continue
                else {
                    pushed = ch
                    break
                }
            }
            start = true
            str.append(ch)
        }
        return str.toString()
    }

    /*fun skip(skip: String = " \t\r\n") {
        while (true) {
            val ch = next() ?: break
            if (ch !in skip) {
                pushed = ch
                break
            }
        }
    }*/
}
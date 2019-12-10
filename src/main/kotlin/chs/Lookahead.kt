package chs

import java.io.Reader

internal class Lookahead(private val r: Reader, val skip: String = "") {
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

    fun pushback(ch: Char?) {
        this.pushed = ch
    }

    inline fun check(predicate: (Char) -> Boolean): Char? {
        val n = next()?:return null
        return if(!predicate(n)) {
            pushback(n)
            null
        } else {
            n
        }
    }

    @API
    private inline fun tryReadWhile(skip: Boolean = true, predicate: (Char) -> Boolean): String? {
        val n = check(predicate)?:return null
        pushback(n)
        return readWhile(skip, predicate)
    }

    inline fun readWhile(skip: Boolean = true, predicate: (Char)->Boolean): String {
        if(skip) skipWhile {it in this.skip}
        val token = StringBuilder()
        while(true) {
            val n = next()?:break
            if(!predicate(n)) {
                pushback(n)
                break
            } else {
                token.append(n)
            }
        }
        return token.toString()
    }

    inline fun skipWhile(predicate: (Char) -> Boolean) {
        while(true) {
            val n = next()?:return
            if(!predicate(n)) {
                pushback(n)
                return
            }
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

    /*fun readNextWord(): String? {
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
    }*/

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
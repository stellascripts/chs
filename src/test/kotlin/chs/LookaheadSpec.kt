package chs

import io.kotlintest.matchers.string.beBlank
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class LookaheadSpec : StringSpec({
    "Reads next character in stream" {
        val la = Lookahead("a".reader())
        la.next() shouldBe 'a'
    }
    "Treats EoF as null" {
        val la = Lookahead("".reader())
        la.next() shouldBe null
    }
    "Pushed back character is read again on next read" {
        val la = Lookahead("ab".reader())
        val ch = la.next()
        ch shouldBe 'a'
        la.pushback(ch)
        la.next() shouldBe 'a'
    }
    "Read while: predicate" {
        val la = Lookahead("abcdef".reader())
        la.readWhile { it < 'd' } shouldBe "abc"
    }
    "Read while: EoF ends" {
        val la = Lookahead("abc".reader())
        la.readWhile { it < 'd' } shouldBe "abc"
    }
    "Skips characters in skip parameter" {
        val la = Lookahead("---abc--def--g-".reader(), "-")
        la.readWhile { it in 'a'..'c' } shouldBe "abc"
        la.readWhile { it in 'a'..'c' } should beBlank()
    }
})
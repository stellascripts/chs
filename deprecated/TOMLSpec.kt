package chs.assets

import chs.callInternal
import io.kotlintest.*
import io.kotlintest.matchers.beOfType
import io.kotlintest.matchers.sequences.contain
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.specs.StringSpec

private fun streamOf(string: String) = string.byteInputStream(Charsets.UTF_8)
private fun TOMLParser.parseKey(): String = this.callInternal("parseKey")
private fun TOMLParser.parseValue(): Any? = this.callInternal("parseValue")
private fun TOMLParser.parseNewline(): Unit = this.callInternal("parseNewline")
private fun TOMLParser.parseKeychain(): List<String> = this.callInternal("parseKeychain")

class TOMLSpec: StringSpec({
    "Comments" {
        var toml = TOMLParser(streamOf(" # this is a comment\nhahaha"))
        toml.parseNewline()
        toml = TOMLParser(streamOf("\"# this is not a comment\""))
        toml.parseKey() shouldBe "# this is not a comment"
    }
    "Bare Keys" {
        val keys = listOf(
            "key",
            "bare_key",
            "bare-key",
            "1234"
        )
        for(key in keys) {
            val actual = TOMLParser(streamOf("$key=")).parseKey()
            actual shouldBe key
        }
    }
    "Quoted Keys" {
        val keys = listOf(
            "\"127.0.0.1\"",
            "\"character encoding\"",
            "\"ʎǝʞ\"",
            "'key2'",
            "'quoted \"value\"'"
        )
        for(key in keys) {
            val actual = TOMLParser(streamOf("$key=")).parseKey()
            val quotes = key[0]
            actual shouldBe key.removeSurrounding("$quotes")
        }
    }
    "Keychain" {
        val chains = listOf(
            "physical.color" to listOf("physical", "color"),
            "physical.shape" to listOf("physical", "shape"),
            "site.\"google.com\"" to listOf("site", "google.com")
        )
        for((input, output) in chains) {
            val actual = TOMLParser(streamOf("$input=")).parseKeychain()
            actual shouldBe output
        }
    }
    "Defining A Key Multiple Times Is Invalid" {
        val input = """
            # DO NOT DO THIS
            name = "Tom"
            name = "Pradyun"
        """.trimIndent()
        val toml = TOMLParser(streamOf(input))
        val throws = shouldThrowExactlyUnit<TOMLParser.Exception> { toml.parse() }
        throws.message shouldContain(Regex("[Mm]ultiple"))
    }
    "Appending" {
        val valid = """
            a.b.c = 1
            a.d = 2
        """.trimIndent()
        val invalid = """
            a.b = 1
            a.b.c = 2
        """.trimIndent()
        shouldNotThrowAnyUnit { TOMLParser(streamOf(valid)).parse() }
        shouldThrowExactlyUnit<TOMLParser.Exception> { TOMLParser(streamOf(invalid)).parse() }
    }

    "Strings" {
        val string = "\"I'm a string. \\\"You can quote me\\\". Name\\tJos\\u00E9\\nLocation\\tSF.\""
        val invalidEscape = "\"\\q\""
        val actual = TOMLParser(streamOf(string)).parseValue()
        actual shouldBe "I'm a string. \"You can quote me\". Name\tJos\u00E9\nLocation\tSF."
        val throws = shouldThrowExactly<TOMLParser.Exception> {
            TOMLParser(streamOf(invalidEscape)).parseValue()
        }
        throws.message shouldContain(Regex("[Ee]scape"))
    }

    "Escape Characters" {
        val escapes = listOf(
            "b" to "\b",
            "n" to "\n",
            "f" to "\u000C",
            "r" to "\r",
            "\"" to "\"",
            "\\" to "\\",
            "u000C" to "\u000C",
            "u00000000" to "\u0000" // i have no idea how to do this one buddy
        )
        for((char, result) in escapes) {
            TOMLParser(streamOf("\"\\$char\"")).parseValue() shouldBe result
        }
    }

    "Multiline Strings" {
        val threequote = "\"\"\""
        val multiline = """
            '''
            Roses are red
            Violets are blue'''
        """.trimIndent().replace("'''", threequote)
        TOMLParser(streamOf(multiline)).parseValue() shouldBe "Roses are red\nViolets are blue"

        val lineEndingBackslash = """
        '''
        The quick brown \


            fox jumps over \
                the lazy dog.'''
        """.trimIndent().replace("'''", threequote)
        TOMLParser(streamOf(lineEndingBackslash)).parseValue() shouldBe "The quick brown fox jumps over the lazy dog."
    }

    "Literal Strings" {
        val literals = listOf(
            """'C:\users\nodejs\templates'""",
            """'\\ServerX\admin$\system32\'""",
            """'Tom "Dubs" Preston-Warner'""",
            """'<\i\c*\s*>'"""
            )
        for(literal in literals) {
            TOMLParser(streamOf(literal)).parseValue() shouldBe literal.removeSurrounding("\'")
        }
    }
    "Multiline Literal Strings" {
        val multilineLiteral = """
            '''
            The first newline is
            trimmed in raw strings.
                All other whitespace
                is preserved.
            '''
        """.trimIndent()
        TOMLParser(streamOf(multilineLiteral)).parseValue() shouldBe """
            The first newline is
            trimmed in raw strings.
                All other whitespace
                is preserved.

        """.trimIndent()
    }
    "Integers" {
        val integers = listOf(
            "+99" to 99L,
            "42" to 42L,
            "0" to 0L,
            "-17" to -17L,
            "1_000" to 1000L,
            "5_349_221" to 5349221L,
            "1_2_3_4_5" to 12345L,
            "0xDEADBEEF" to 0xDEADBEEFL,
            "0xdeadbeef" to 0xdeadbeefL,
            "0xdead_beef" to 0xdeadbeefL,
            "0o01234567" to "01234567".toLong(8),
            "0o755" to "755".toLong(8),
            "0b11010110" to 0b11010110L
        )
        for((int, v) in integers) {
            TOMLParser(streamOf(int)).parseValue() shouldBe v
        }
    }
    "Floats" {
        val floats = listOf(
            "+1.0" to +1.0,
            "3.1415" to 3.1415,
            "-0.01" to -0.01,
            "5e+22" to 5e+22,
            "1e06" to 1e06,
            "-2E-2" to -2E-2,
            "6.626e-34" to 6.626e-34,
            "224_617.445_991_228" to 224_617.445_991_228,
            "+inf" to Double.POSITIVE_INFINITY,
            "-inf" to Double.NEGATIVE_INFINITY,
            "inf" to Double.POSITIVE_INFINITY
        )
        for((n, v) in floats) {
            TOMLParser(streamOf(n)).parseValue() shouldBe v
        }
    }
    "Array" {
        val arrays = listOf(
            "[1,2,3]" to listOf(1L,2L,3L),
            """["red","yellow","green"]""" to listOf("red","yellow","green"),
            "[[1,2],[3,4,5]]" to listOf(listOf(1L,2L),listOf(3L,4L,5L)),
            """[ "all", 'strings', ~are the same~, '''type''' ]""".replace("~", "\"\"\"") to
                    listOf("all", "strings", "are the same", "type"),
            "[ [1, 2], ['a','b','c'] ]" to listOf(listOf(1L,2L), listOf("a","b","c")),
            """
                [
                    1, 2, 3
                ]
            """.trimIndent() to listOf(1L,2L,3L),
            """
                [
                    1,
                    2,
                ]
            """.trimIndent() to listOf(1L,2L)
        )
        for((input, output) in arrays) {
            TOMLParser(streamOf(input)).parseValue() shouldBe output
        }
    }
    "Table" {
        val wholeHog = """
            [table-1]
            key1 = "some string"
            key2 = 123

            [table-2]
            key1 = "another string"
            key2 = 456
        """.trimIndent()
        val root = TOMLParser(streamOf(wholeHog)).parse()
        val table1 = root.asMap()["table-1"] as TOMLTable
        val table2 = root.asMap()["table-2"] as TOMLTable
        table1["key1"] shouldBe "some string"
        table2["key1"] shouldBe "another string"
        table1["key2"] shouldBe 123L
        table2["key2"] shouldBe 456L

        root.getS("table-1.key1") shouldBe "some string"
        root.getN("table-1.key2") shouldBe 123L
        root.getS("table-2.key2") shouldBe "another string"
        root.getN("table-2.key2") shouldBe 456L
    }
})
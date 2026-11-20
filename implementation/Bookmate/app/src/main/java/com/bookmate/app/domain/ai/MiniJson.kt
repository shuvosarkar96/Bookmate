package com.bookmate.app.domain.ai

/**
 * A small JSON reader (no library) for the AI recommendation answer.
 * Throws IllegalArgumentException on bad input.
 */
internal class MiniJson private constructor(private val text: String) {
    private var pos = 0

    companion object {
        /** Returns a List, Map, String, Double, Boolean or null. */
        fun parse(text: String): Any? {
            val reader = MiniJson(text)
            val value = reader.readValue()
            reader.skipSpaces()
            require(reader.pos == text.length) { "Extra text after the JSON value" }
            return value
        }
    }

    private fun skipSpaces() {
        while (pos < text.length && text[pos].isWhitespace()) pos++
    }

    private fun peek(): Char {
        require(pos < text.length) { "Unexpected end of text" }
        return text[pos]
    }

    private fun next(): Char {
        val c = peek()
        pos++
        return c
    }

    private fun readValue(): Any? {
        skipSpaces()
        return when (peek()) {
            '[' -> readArray()
            '{' -> readObject()
            '"' -> readString()
            else -> readLiteral()
        }
    }

    private fun readArray(): List<Any?> {
        pos++
        val list = mutableListOf<Any?>()
        skipSpaces()
        if (peek() == ']') {
            pos++
            return list
        }
        var done = false
        while (!done) {
            list.add(readValue())
            skipSpaces()
            when (next()) {
                ',' -> Unit
                ']' -> done = true
                else -> throw IllegalArgumentException("Expected , or ]")
            }
        }
        return list
    }

    private fun readObject(): Map<String, Any?> {
        pos++
        val map = linkedMapOf<String, Any?>()
        skipSpaces()
        if (peek() == '}') {
            pos++
            return map
        }
        var done = false
        while (!done) {
            skipSpaces()
            require(peek() == '"') { "Expected a key" }
            val key = readString()
            skipSpaces()
            require(next() == ':') { "Expected :" }
            map[key] = readValue()
            skipSpaces()
            when (next()) {
                ',' -> Unit
                '}' -> done = true
                else -> throw IllegalArgumentException("Expected , or }")
            }
        }
        return map
    }

    private fun readString(): String {
        pos++
        val builder = StringBuilder()
        var done = false
        while (!done) {
            val c = next()
            when (c) {
                '"' -> done = true
                '\\' -> builder.append(readEscape())
                else -> builder.append(c)
            }
        }
        return builder.toString()
    }

    private fun readEscape(): Char = when (val e = next()) {
        '"', '\\', '/' -> e
        'n' -> '\n'
        't' -> '\t'
        'r' -> '\r'
        'b' -> '\b'
        'f' -> '\u000C'
        'u' -> {
            require(pos + 4 <= text.length) { "Bad unicode escape" }
            val code = text.substring(pos, pos + 4).toInt(16)
            pos += 4
            code.toChar()
        }
        else -> throw IllegalArgumentException("Bad escape")
    }

    private fun readLiteral(): Any? {
        val start = pos
        while (pos < text.length && text[pos] !in ",]} \n\r\t") pos++
        return when (val token = text.substring(start, pos)) {
            "true" -> true
            "false" -> false
            "null" -> null
            else -> token.toDoubleOrNull() ?: throw IllegalArgumentException("Bad value: $token")
        }
    }
}
package com.example.starter.konsist

/**
 * The text with comments and string literals blanked out, so a word like `toggleable (` in a KDoc or a
 * `"Color(0x..."` string does not count as code.
 */
internal fun String.codeOnly(): String {
    val out = StringBuilder(length)
    var i = 0
    while (i < length) {
        val end = skippedEnd(i)
        if (end < 0) {
            out.append(this[i])
            i++
        } else {
            out.append(" ")
            i = end
        }
    }
    return out.toString()
}

/** End of the comment or literal that starts at [i], or -1 when [i] is code. */
private fun String.skippedEnd(i: Int): Int =
    when {
        startsWith("//", i) -> endAt(indexOf('\n', i), 0)
        startsWith("/*", i) -> endAt(indexOf("*/", i + 2), 2)
        startsWith("\"\"\"", i) -> endAt(indexOf("\"\"\"", i + 3), 3)
        this[i] == '"' -> stringEnd(i)
        // A char literal such as '"' must not open a string.
        this[i] == '\'' -> endAt(indexOf('\'', i + if (getOrNull(i + 1) == '\\') 3 else 2), 1)
        else -> -1
    }

private fun String.endAt(
    found: Int,
    tokenLength: Int,
): Int = if (found < 0) length else found + tokenLength

/** Index just past the `"` string literal that opens at [start], honoring backslash escapes. */
private fun String.stringEnd(start: Int): Int {
    var i = start + 1
    while (i < length && this[i] != '"' && this[i] != '\n') {
        i += if (this[i] == '\\') 2 else 1
    }
    return minOf(i + 1, length)
}

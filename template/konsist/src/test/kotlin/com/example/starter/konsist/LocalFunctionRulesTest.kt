package com.example.starter.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoCompanionObjectDeclaration
import com.lemonappdev.konsist.api.declaration.KoEnumConstantDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.declaration.KoObjectDeclaration
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

/**
 * No local functions: a named `fun` declared inside a function body, an `init` block, an accessor
 * or a lambda (a Compose `Layout { }` measure block, a `layout { }` placement block, a Gradle
 * `subprojects { }` block). A local function cannot be tested or reused on its own, and it hides
 * what it reads from the enclosing scope, which bloats composable and measure lambdas. Extract a
 * `private` top-level or member function that takes what it needs as parameters.
 *
 * Konsist's `localFunctions` sees only functions declared directly in a function body and misses
 * the ones inside lambdas, so the sources are also scanned as text: a `fun` whose innermost
 * enclosing `{ }` is not a class body is local. A function Konsist reports as top-level or as a
 * class member is never flagged. Scope: every `.kt` and `.kts` file, production and test source
 * sets, `build-logic` included, `build/` and hidden directories excluded.
 *
 * Still allowed: members of class bodies, including companion objects, local classes and object
 * expressions (`object : Painter() { override fun ... }`), since those implement a type; anonymous
 * functions (`fun(x: Int) { }`), which are expressions like lambdas; and top-level functions of a
 * `.kts` script.
 */
class LocalFunctionRulesTest {
    @Test
    fun `no function declares a local function`() {
        val konsistFiles =
            Konsist
                .scopeFromProject()
                .files
                .filter { it.isProjectSource() }
        assertTrue(konsistFiles.isNotEmpty(), "no Kotlin sources found, scope is wrong")
        val konsistFunctions = konsistFiles.flatMap { it.functions(includeNested = true, includeLocal = true) }
        val memberLines =
            konsistFunctions
                .filter { it.isTopLevelOrMember() }
                .map { it.declarationSite().let { site -> site.path to site.line } }
                .toSet()
        assertTrue(memberLines.isNotEmpty(), "no functions found, scope is wrong")

        val root = konsistFiles.first().let { File(it.path.removeSuffix(it.projectPath)) }
        val scanned =
            projectSources(root)
                .flatMap { (path, text) -> scanLocalFunctions(path, text) }
                .filterNot { (it.path to it.line) in memberLines }
        val viaKonsist = konsistFunctions.flatMap { fn -> fn.localFunctions.map { it.declarationSite() } }
        val violations =
            (scanned + viaKonsist)
                .distinctBy { it.path to it.line }
                .sortedWith(compareBy({ it.path }, { it.line }))
                .map { "${it.path}:${it.line}: ${it.name}" }
        assertTrue(
            violations.isEmpty(),
            "Local functions are not allowed; extract a private top-level or member function " +
                "with explicit parameters:\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `text scan flags local functions only`() {
        val source =
            """
            fun top() {
                fun inBody() = 1
                Layout { m, c -> fun inLambda() = 2 }
                val o = object : Painter() {
                    override fun member() { fun inObjectMember() = 3 }
                }
                class Local(val x: Int) { fun localMember() = "fun fake() {" }
                val anon = fun(x: Int) = x // fun inComment()
                val s = "${'$'}{ listOf("}").size } fun inString()"
            }
            fun interface Callback { fun call() }
            class Member { fun member() = 1 }
            """.trimIndent()
        val names = scanLocalFunctions("Fixture.kt", source).map { it.name }
        assertTrue(names == listOf("inBody", "inLambda", "inObjectMember"), "unexpected scan result: $names")
    }
}

/** Where a `fun` keyword sits. [path] is relative to the project root, without a leading slash. */
private data class FunctionSite(
    val path: String,
    val line: Int,
    val name: String,
)

private val CLASS_HEADER = Regex("""(?<![\w:])(class|object|interface)\b""")

/** `fun` followed by a name, optionally after type parameters and a receiver; not `fun(` or `fun interface`. */
private val NAMED_FUN = Regex("""\bfun\s+(?:<[^>]*>\s*)?(?!interface\b)([\w.`<>?, ]+?)\s*\(""")

private fun KoFileDeclaration.isProjectSource(): Boolean =
    projectPath
        .trimStart('/')
        .split('/')
        .dropLast(1)
        .none { it == "build" || it.startsWith(".") }

private fun KoFunctionDeclaration.isTopLevelOrMember(): Boolean =
    when (containingDeclaration) {
        is KoFileDeclaration,
        is KoClassDeclaration,
        is KoObjectDeclaration,
        is KoInterfaceDeclaration,
        is KoCompanionObjectDeclaration,
        is KoEnumConstantDeclaration,
        -> true
        else -> false
    }

/** Site of the `fun` keyword; [location] points at the declaration start, which may be a KDoc or annotation. */
private fun KoFunctionDeclaration.declarationSite(): FunctionSite {
    val startLine = location.substringBeforeLast(':').substringAfterLast(':').toInt()
    val code = stripCommentsAndStrings(text)
    val keyword = NAMED_FUN.find(code)?.range?.first ?: 0
    return FunctionSite(containingFile.projectPath.trimStart('/'), startLine + code.lineAt(keyword) - 1, name)
}

/** Every `.kt` and `.kts` file below [root] with its text, skipping `build/` and hidden directories. */
private fun projectSources(root: File): List<Pair<String, String>> =
    root
        .walk()
        .onEnter { it == root || (it.name != "build" && !it.name.startsWith(".")) }
        .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
        .map { it.relativeTo(root).invariantSeparatorsPath to it.readText().replace("\r\n", "\n") }
        .toList()

/**
 * Named functions whose innermost enclosing `{ }` is a code block (function body, lambda, `init`,
 * accessor) rather than a class body. A block is a class body when its header, read back to the
 * previous `{`, `}`, `;` or unmatched `(`, contains `class`, `object` or `interface` (not `::class`).
 */
private fun scanLocalFunctions(
    path: String,
    source: String,
): List<FunctionSite> {
    val code = stripCommentsAndStrings(source)
    val funStarts = NAMED_FUN.findAll(code).associateBy { it.range.first }
    val classBodies = ArrayDeque<Boolean>()
    val found = mutableListOf<FunctionSite>()
    code.forEachIndexed { index, char ->
        when (char) {
            '{' -> classBodies.addLast(CLASS_HEADER.containsMatchIn(blockHeader(code, index)))
            '}' -> classBodies.removeLastOrNull()
        }
        val match = funStarts[index]
        if (match != null && classBodies.lastOrNull() == false) {
            val name = match.groupValues[1].substringAfterLast('.').trim()
            found += FunctionSite(path, code.lineAt(index), name)
        }
    }
    return found
}

private fun blockHeader(
    code: String,
    brace: Int,
): String {
    var depth = 0
    var start = brace - 1
    while (start >= 0 && !(depth == 0 && code[start] in "([{};")) {
        depth += nestingStep(code[start])
        start--
    }
    return code.substring(start + 1, brace)
}

/** Reading backwards, a closing bracket opens a nesting level and an opening one closes it. */
private fun nestingStep(char: Char): Int =
    when (char) {
        ')', ']' -> 1
        '(', '[' -> -1
        else -> 0
    }

/** 1-based line of [offset]. */
private fun String.lineAt(offset: Int): Int = 1 + (0 until offset).count { this[it] == '\n' }

/**
 * Blanks comments and string and char literals (templates included) with spaces, keeping newlines
 * so offsets and lines stay put.
 */
private fun stripCommentsAndStrings(source: String): String {
    val out = StringBuilder(source)
    var i = 0
    while (i < source.length) {
        val end = literalEnd(source, i)
        if (end < 0) {
            i++
        } else {
            for (j in i until end) if (out[j] != '\n') out[j] = ' '
            i = end
        }
    }
    return out.toString()
}

/** End (exclusive) of the comment or literal starting at [start], or -1 when none starts there. */
private fun literalEnd(
    source: String,
    start: Int,
): Int =
    when {
        source.startsWith("//", start) -> source.indexOf('\n', start).let { if (it < 0) source.length else it }
        source.startsWith("/*", start) -> blockCommentEnd(source, start)
        source.startsWith("\"\"\"", start) -> stringEnd(source, start, raw = true)
        source[start] == '"' || source[start] == '\'' -> stringEnd(source, start, raw = false)
        else -> -1
    }

/** End (exclusive) of the block comment at [start]; Kotlin block comments nest. */
private fun blockCommentEnd(
    source: String,
    start: Int,
): Int {
    var depth = 0
    var i = start
    while (i < source.length) {
        when {
            source.startsWith("/*", i) -> depth++.also { i += 2 }
            source.startsWith("*/", i) -> depth--.also { i += 2 }
            else -> i++
        }
        if (depth == 0) return i
    }
    return source.length
}

/**
 * End (exclusive) of the string or char literal at [start]. A `${ }` template is skipped as a
 * whole, so braces and quotes inside it neither close the literal nor reach the block scan.
 */
private fun stringEnd(
    source: String,
    start: Int,
    raw: Boolean,
): Int {
    val quote = if (raw) "\"\"\"" else source[start].toString()
    var i = start + quote.length
    while (i < source.length) {
        when {
            !raw && source[i] == '\\' -> i += 2
            source.startsWith("\${", i) -> i = templateEnd(source, i + 2)
            source.startsWith(quote, i) -> return (i + quote.length).let { end -> if (raw) rawTail(source, end) else end }
            else -> i++
        }
    }
    return source.length
}

/** End (exclusive) of a `${ }` template whose body starts at [start]. */
private fun templateEnd(
    source: String,
    start: Int,
): Int {
    var depth = 1
    var i = start
    while (i < source.length && depth > 0) {
        val literal = literalEnd(source, i)
        when {
            literal >= 0 -> i = literal
            source[i] == '{' -> depth++.also { i++ }
            source[i] == '}' -> depth--.also { i++ }
            else -> i++
        }
    }
    return i
}

/** A raw string may end with extra quotes (`""""`); they belong to the string. */
private fun rawTail(
    source: String,
    end: Int,
): Int {
    var i = end
    while (i < source.length && source[i] == '"') i++
    return i
}

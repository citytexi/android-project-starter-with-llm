package com.example.starter.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration

/** Base package of the project. scripts/generate.py rewrites it with the rest of the sources. */
private const val BASE_PACKAGE = "com.example.starter"

/** Source sets that ship in the app: main plus the build-type source sets. */
private val PRODUCTION_SOURCE_SETS = setOf("main", "debug", "release")

/** Layers below [BASE_PACKAGE]. Anything else in the base package belongs to `app`. */
private val PACKAGE_LAYERS = listOf("feature", "domain", "data", "core")

/** Production sources of [layer] (`app`, `feature`, `domain`, `data` or `core`). */
internal fun productionFiles(layer: String): List<KoFileDeclaration> = allProductionFiles().filter { it.layer() == layer }

internal fun allProductionFiles(): List<KoFileDeclaration> =
    Konsist
        .scopeFromProject()
        .files
        .filter { it.isProduction() }

/**
 * True for files in a production source set (`src/main`, `src/debug`, `src/release`) outside any
 * module build directory. Only the path segments before `src` are checked for `build`, so a
 * package such as `com.build.app` (`src/main/kotlin/com/build/...`) still counts.
 */
internal fun KoFileDeclaration.isProduction(): Boolean {
    val segments = projectPath.trimStart('/').split('/')
    val src = segments.indexOf("src")
    val moduleDirs = if (src < 0) segments else segments.subList(0, src)
    return src >= 0 &&
        segments.getOrNull(src + 1) in PRODUCTION_SOURCE_SETS &&
        moduleDirs.none { it == "build" || it.startsWith(".") }
}

/**
 * Layer of the file, read from its package: `<base>.feature.*` is `feature`, `<base>.domain.*` is
 * `domain`, and so on; the rest of `<base>` is `app`. Files outside the base package fall back to
 * their top-level module directory.
 */
internal fun KoFileDeclaration.layer(): String {
    val pkg = packagee?.name.orEmpty()
    if (isInPackage(pkg, BASE_PACKAGE)) {
        return PACKAGE_LAYERS.firstOrNull { isInPackage(pkg, "$BASE_PACKAGE.$it") } ?: "app"
    }
    return projectPath.trimStart('/').substringBefore('/')
}

/** True when [name] is [pkg] itself (wildcard import) or lies below it; `data` never matches `datastore`. */
internal fun isInPackage(
    name: String,
    pkg: String,
): Boolean = name == pkg || name.startsWith("$pkg.")

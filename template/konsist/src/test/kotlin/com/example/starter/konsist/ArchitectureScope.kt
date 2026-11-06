package com.example.starter.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration

/** Production sources of the layer of the top-level module directory [layer]. */
internal fun productionFiles(layer: String): List<KoFileDeclaration> =
    Konsist
        .scopeFromProject()
        .files
        .filter { it.isProduction() && it.layer() == layer }

internal fun allProductionFiles(): List<KoFileDeclaration> =
    Konsist
        .scopeFromProject()
        .files
        .filter { it.isProduction() }

internal fun KoFileDeclaration.isProduction(): Boolean {
    val path = projectPath
    return path.contains("/src/main/") && !path.contains("/build/")
}

/** Top-level module directory (app, domain, data, feature, core) of the file. */
internal fun KoFileDeclaration.layer(): String = projectPath.trimStart('/').substringBefore('/')

/** True when [name] is [pkg] itself (wildcard import) or lies below it; `data` never matches `datastore`. */
internal fun isInPackage(
    name: String,
    pkg: String,
): Boolean = name == pkg || name.startsWith("$pkg.")

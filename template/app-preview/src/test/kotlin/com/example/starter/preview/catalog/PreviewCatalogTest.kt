package com.example.starter.preview.catalog

import org.junit.Test
import kotlin.test.assertEquals

class PreviewCatalogTest {
    @Test
    fun `catalog titles are unique because navigation finds screens by title`() {
        val duplicates = previewCatalog.groupBy { it.title }.filterValues { it.size > 1 }.keys
        assertEquals(emptySet(), duplicates, "duplicate catalog titles")
    }
}

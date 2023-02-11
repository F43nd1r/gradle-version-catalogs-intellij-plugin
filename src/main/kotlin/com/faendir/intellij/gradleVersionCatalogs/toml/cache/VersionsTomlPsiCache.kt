package com.faendir.intellij.gradleVersionCatalogs.toml.cache

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.faendir.intellij.gradleVersionCatalogs.toml.isBundleLibraryRef
import com.faendir.intellij.gradleVersionCatalogs.toml.isVersionRef
import com.intellij.psi.util.CachedValuesManager.getProjectPsiDependentCache
import com.intellij.psi.util.childrenOfType
import org.toml.lang.psi.*

object VersionsTomlPsiCache {
    fun getLibraryReferences(file: TomlFile): List<TomlLiteral> = getProjectPsiDependentCache(file) {
        val libraryReferences = mutableListOf<TomlLiteral>()
        object : TomlRecursiveVisitor() {
            override fun visitLiteral(element: TomlLiteral) {
                if (element.isBundleLibraryRef()) {
                    libraryReferences.add(element)
                }
                super.visitValue(element)
            }
        }.visitFile(it)
        libraryReferences
    }

    fun getVersionReferences(file: TomlFile): List<TomlLiteral> = getProjectPsiDependentCache(file) {
        val versionReferences = mutableListOf<TomlLiteral>()
        object : TomlRecursiveVisitor() {
            override fun visitKeyValue(element: TomlKeyValue) {
                if (element.isVersionRef()) {
                    val value = element.value
                    if (value is TomlLiteral) {
                        versionReferences.add(value)
                    }
                }
                super.visitKeyValue(element)
            }
        }.visitFile(it)
        versionReferences
    }

    private fun getTables(file: TomlFile): List<TomlTable> = getProjectPsiDependentCache(file) { file.childrenOfType() }

    fun getDefinitions(file: TomlFile, type: VCElementType): List<TomlKeyValue> = getAllDefinitions(file)[type].orEmpty()

    private fun getAllDefinitions(file: TomlFile): Map<VCElementType, List<TomlKeyValue>> =
        getProjectPsiDependentCache(file) {
            getTables(file)
                .associateBy { table -> VCElementType.values().find { table.header.key?.text == it.tableHeader } }
                .filterKeys { it != null }
                .mapKeys { (type, _) -> type!! }
                .mapValues { (_, table) -> table.childrenOfType() }
        }
}
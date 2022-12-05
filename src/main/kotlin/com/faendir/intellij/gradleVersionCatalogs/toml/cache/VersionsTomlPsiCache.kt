package com.faendir.intellij.gradleVersionCatalogs.toml.cache

import com.faendir.intellij.gradleVersionCatalogs.toml.isBundleLibraryRef
import com.faendir.intellij.gradleVersionCatalogs.toml.isBundleTable
import com.faendir.intellij.gradleVersionCatalogs.toml.isLibraryTable
import com.faendir.intellij.gradleVersionCatalogs.toml.isPluginsTable
import com.faendir.intellij.gradleVersionCatalogs.toml.isVersionRef
import com.faendir.intellij.gradleVersionCatalogs.toml.isVersionTable
import com.intellij.psi.util.CachedValuesManager.getProjectPsiDependentCache
import com.intellij.psi.util.childrenOfType
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlLiteral
import org.toml.lang.psi.TomlRecursiveVisitor
import org.toml.lang.psi.TomlTable

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

    fun getVersionDefinitions(file: TomlFile): List<TomlKeyValue> = getProjectPsiDependentCache(file) {
        getTables(file).find { it.isVersionTable() }?.childrenOfType<TomlKeyValue>().orEmpty()
    }

    fun getLibraryDefinitions(file: TomlFile): List<TomlKeyValue> = getProjectPsiDependentCache(file) {
        getTables(file).find { it.isLibraryTable() }?.childrenOfType<TomlKeyValue>().orEmpty()
    }

    fun getPluginDefinitions(file: TomlFile): List<TomlKeyValue> = getProjectPsiDependentCache(file) {
        getTables(file).find { it.isPluginsTable() }?.childrenOfType<TomlKeyValue>().orEmpty()
    }

    fun getBundleDefinitions(file: TomlFile): List<TomlKeyValue> = getProjectPsiDependentCache(file) {
        getTables(file).find { it.isBundleTable() }?.childrenOfType<TomlKeyValue>().orEmpty()
    }
}
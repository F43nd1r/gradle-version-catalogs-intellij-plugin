package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.faendir.intellij.gradleVersionCatalogs.toml.cache.VersionsTomlPsiCache
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.stubs.elements.KtDotQualifiedExpressionElementType
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.ext.elementType

class GotoCatalogDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement> {
        val project = editor.project
        if (sourceElement != null && sourceElement.containingFile.name.endsWith("gradle.kts") && project != null) {
            val expression = PsiTreeUtil.findFirstParent(sourceElement) {
                try {
                    it.elementType is KtDotQualifiedExpressionElementType
                } catch (e: Exception) {
                    false
                }
            }
            if (expression != null) {
                expression.findPluginAccessor()
                    ?.let { text ->
                        return project.visitAllVersionsTomlKeyValues(VersionsTomlPsiCache::getPluginDefinitions, text).toTypedArray()
                    }
                expression.findVersionAccessor()
                    ?.let { text ->
                        return project.visitAllVersionsTomlKeyValues(VersionsTomlPsiCache::getVersionDefinitions, text).toTypedArray()
                    }
                expression.findLibraryAccessor()
                    ?.let { text ->
                        return project.visitAllVersionsTomlKeyValues(VersionsTomlPsiCache::getLibraryDefinitions, text).toTypedArray()
                    }
                expression.findBundleAccessor()
                    ?.let { text ->
                        return project.visitAllVersionsTomlKeyValues(VersionsTomlPsiCache::getBundleDefinitions, text).toTypedArray()
                    }
            }
        }
        return PsiElement.EMPTY_ARRAY
    }
}

private fun Project.visitAllVersionsTomlKeyValues(getKeyValues: (TomlFile) -> List<TomlKeyValue>, search: String): MutableList<TomlKeyValue> {
    val result = mutableListOf<TomlKeyValue>()
    FilenameIndex.getAllFilesByExt(this, "toml")
        .filter { it.name.endsWith("versions.toml") }
        .map { it.toPsiFile(this) }
        .filterIsInstance<TomlFile>()
        .map { file ->
            getKeyValues(file).forEach { element ->
                if (element.key.text == search) {
                    result.add(element)
                }
            }
        }
    return result
}
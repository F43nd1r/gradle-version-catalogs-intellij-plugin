package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.faendir.intellij.gradleVersionCatalogs.toml.isBundleDef
import com.faendir.intellij.gradleVersionCatalogs.toml.isLibraryDef
import com.faendir.intellij.gradleVersionCatalogs.toml.isPluginDef
import com.faendir.intellij.gradleVersionCatalogs.toml.isVersionDef
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
import org.toml.lang.psi.TomlRecursiveVisitor
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
                    ?.let { text -> return project.visitAllVersionsTomlKeyValues { if (it.isPluginDef() && it.key.text == text) it else null }.toTypedArray() }
                expression.findVersionAccessor()
                    ?.let { text -> return project.visitAllVersionsTomlKeyValues { if (it.isVersionDef() && it.key.text == text) it else null }.toTypedArray() }
                expression.findLibraryAccessor()
                    ?.let { text -> return project.visitAllVersionsTomlKeyValues { if (it.isLibraryDef() && it.key.text == text) it else null }.toTypedArray() }
                expression.findBundleAccessor()
                    ?.let { text -> return project.visitAllVersionsTomlKeyValues { if(it.isBundleDef() && it.key.text == text) it else null }.toTypedArray() }
            }
        }
        return PsiElement.EMPTY_ARRAY
    }
}

private fun <T> Project.visitAllVersionsTomlKeyValues(visitor: (element: TomlKeyValue) -> T?): MutableList<T> {
    val result = mutableListOf<T>()
    FilenameIndex.getAllFilesByExt(this, "toml")
        .filter { it.name.endsWith("versions.toml") }
        .map { it.toPsiFile(this) }
        .filterIsInstance<TomlFile>()
        .map { file ->
            object : TomlRecursiveVisitor() {
                override fun visitKeyValue(element: TomlKeyValue) {
                    visitor(element)?.let { result.add(it) }
                    super.visitKeyValue(element)
                }
            }.visitFile(file)
        }
    return result
}
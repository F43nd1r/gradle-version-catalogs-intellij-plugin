package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.faendir.intellij.gradleVersionCatalogs.kotlin.cache.BuildGradleKtsPsiCache
import com.faendir.intellij.gradleVersionCatalogs.toml.cache.VersionsTomlPsiCache
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.stubs.elements.KtDotQualifiedExpressionElementType
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
                BuildGradleKtsPsiCache.findAccessor(expression)?.run {
                    return project.findInVersionsTomlKeyValues({ file -> VersionsTomlPsiCache.getDefinitions(file, type) }, id).toTypedArray()
                }
            }
        }
        return PsiElement.EMPTY_ARRAY
    }
}


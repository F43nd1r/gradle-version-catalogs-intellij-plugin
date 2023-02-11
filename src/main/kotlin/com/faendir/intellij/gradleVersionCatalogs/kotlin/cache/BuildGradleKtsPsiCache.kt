package com.faendir.intellij.gradleVersionCatalogs.kotlin.cache

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.faendir.intellij.gradleVersionCatalogs.kotlin.Accessor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValuesManager.getProjectPsiDependentCache
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

object BuildGradleKtsPsiCache {
    private fun getAccessors(file: KtFile): Map<VCElementType, List<Accessor>> = getProjectPsiDependentCache(file) { f ->
        val accessors = mutableListOf<Accessor>()
        object : KtTreeVisitorVoid() {
            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                findAccessor(expression)
                    ?.takeIf { expression.parent !is KtDotQualifiedExpression || findAccessor(expression.parent) == null }
                    ?.let { accessors.add(it) }
                super.visitDotQualifiedExpression(expression)
            }
        }.visitFile(f)
        accessors.groupBy { it.type }
    }

    fun getAccessors(file: KtFile, type: VCElementType) = getAccessors(file)[type].orEmpty()

    fun findAccessor(element: PsiElement) = getProjectPsiDependentCache(element) { Accessor.find(it) }
}


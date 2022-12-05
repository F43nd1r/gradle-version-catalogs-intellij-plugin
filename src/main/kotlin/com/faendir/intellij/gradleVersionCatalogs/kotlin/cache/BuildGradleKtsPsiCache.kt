package com.faendir.intellij.gradleVersionCatalogs.kotlin.cache

import com.faendir.intellij.gradleVersionCatalogs.kotlin.PotentialAccessor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValuesManager.getProjectPsiDependentCache
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

object BuildGradleKtsPsiCache {
    private fun getPotentialAccessors(file: KtFile): List<PotentialAccessor> = getProjectPsiDependentCache(file) {
        val accessors = mutableListOf<PotentialAccessor>()
        object : KtTreeVisitorVoid() {
            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                val accessor = PotentialAccessor(expression)
                if (accessor.returnType != null) accessors.add(accessor)
                super.visitDotQualifiedExpression(expression)
            }
        }.visitFile(it)
        accessors
    }

    fun getPluginAccessors(file: KtFile): List<Pair<PsiElement, String>> = getProjectPsiDependentCache(file) { f ->
        getPotentialAccessors(f).filter { it.isPluginAccessor() }.map { it.element to it.segments.drop(1).joinToString("-") }
    }

    fun getVersionAccessors(file: KtFile): List<Pair<PsiElement, String>> = getProjectPsiDependentCache(file) { f ->
        getPotentialAccessors(f).filter { it.isVersionAccessor() }.map { it.element to it.segments.drop(1).joinToString("-") }
    }

    fun getLibraryAccessors(file: KtFile): List<Pair<PsiElement, String>> = getProjectPsiDependentCache(file) { f ->
        getPotentialAccessors(f).filter { it.isLibraryAccessor() }.map { it.element to it.segments.joinToString("-") }
    }

    fun getBundleAccessors(file: KtFile): List<Pair<PsiElement, String>> = getProjectPsiDependentCache(file) { f ->
        getPotentialAccessors(f).filter { it.isBundleAccessor() }.map { it.element to it.segments.drop(1).joinToString("-") }
    }
}


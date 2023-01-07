package com.faendir.intellij.gradleVersionCatalogs.kotlin.cache

import com.faendir.intellij.gradleVersionCatalogs.kotlin.Accessor
import com.faendir.intellij.gradleVersionCatalogs.kotlin.PotentialAccessor
import com.intellij.psi.util.CachedValuesManager.getProjectPsiDependentCache
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

object BuildGradleKtsPsiCache {
    private fun getPotentialAccessors(file: KtFile): List<PotentialAccessor> = getProjectPsiDependentCache(file) {
        val accessors = mutableListOf<PotentialAccessor>()
        object : KtTreeVisitorVoid() {
            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                if (expression.parent !is KtDotQualifiedExpression) {
                    val accessor = PotentialAccessor(expression)
                    if (accessor.returnType != null) accessors.add(accessor)
                }
                super.visitDotQualifiedExpression(expression)
            }
        }.visitFile(it)
        accessors
    }

    fun getLibraryAccessors(file: KtFile): List<Accessor> = getProjectPsiDependentCache(file) { f ->
        getPotentialAccessors(f).mapNotNull { it.asLibraryAccessor() }
    }

    fun getVersionAccessors(file: KtFile): List<Accessor> = getProjectPsiDependentCache(file) { f ->
        getPotentialAccessors(f).mapNotNull { it.asVersionAccessor() }
    }

    fun getBundleAccessors(file: KtFile): List<Accessor> = getProjectPsiDependentCache(file) { f ->
        getPotentialAccessors(f).mapNotNull { it.asBundleAccessor() }
    }

    fun getPluginAccessors(file: KtFile): List<Accessor> = getProjectPsiDependentCache(file) { f ->
        getPotentialAccessors(f).mapNotNull { it.asPluginAccessor() }
    }
}


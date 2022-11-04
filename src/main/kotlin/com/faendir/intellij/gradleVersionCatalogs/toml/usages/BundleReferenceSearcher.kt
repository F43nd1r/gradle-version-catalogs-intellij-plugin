package com.faendir.intellij.gradleVersionCatalogs.toml.usages

import com.faendir.intellij.gradleVersionCatalogs.kotlin.findBundleAccessor
import com.faendir.intellij.gradleVersionCatalogs.toml.isBundleDef
import com.faendir.intellij.gradleVersionCatalogs.toml.reference.ResolvedPsiReference
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiReference
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.toml.lang.psi.TomlKey
import org.toml.lang.psi.TomlKeySegment
import org.toml.lang.psi.TomlKeyValue

class BundleReferenceSearcher : QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
    override fun execute(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>): Boolean = ReadAction.compute<_, Exception> {
        val searchFor = queryParameters.elementToSearch
        val key = ((searchFor as? TomlKeySegment)?.parent ?: searchFor) as? TomlKey
        val keyValue = (key?.parent ?: searchFor) as? TomlKeyValue
        if (keyValue?.isBundleDef() == true) {
            val text = keyValue.key.text
            try {
                FilenameIndex.getAllFilesByExt(queryParameters.project, "kts").filter { it.name == "build.gradle.kts" }
                    .map { it.toPsiFile(queryParameters.project) }
                    .filterIsInstance<KtFile>()
                    .map {
                        object : KtTreeVisitorVoid() {
                            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                                if (expression.findBundleAccessor() == text) {
                                    if (!consumer.process(ResolvedPsiReference(expression, keyValue))) throw StopComputeException()
                                }
                                super.visitDotQualifiedExpression(expression)
                            }
                        }.visitFile(it)
                    }
            } catch (_: StopComputeException) {
                return@compute false
            }
        }
        true
    }
}


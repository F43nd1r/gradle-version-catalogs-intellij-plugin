package com.faendir.intellij.gradleVersionCatalogs.toml.usages

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.faendir.intellij.gradleVersionCatalogs.kotlin.cache.BuildGradleKtsPsiCache
import com.faendir.intellij.gradleVersionCatalogs.toml.cache.VersionsTomlPsiCache
import com.faendir.intellij.gradleVersionCatalogs.toml.reference.ResolvedPsiReference
import com.faendir.intellij.gradleVersionCatalogs.toml.reference.TomlReference
import com.faendir.intellij.gradleVersionCatalogs.toml.unquote
import com.faendir.intellij.gradleVersionCatalogs.toml.vcElementType
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiReference
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlKey
import org.toml.lang.psi.TomlKeySegment
import org.toml.lang.psi.TomlKeyValue

class LibraryReferenceSearcher : QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
    override fun execute(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>): Boolean =
        ReadAction.compute<_, Exception> {
            val searchFor = queryParameters.elementToSearch
            val key = ((searchFor as? TomlKeySegment)?.parent ?: searchFor) as? TomlKey
            val keyValue = (key?.parent ?: searchFor) as? TomlKeyValue
            if (keyValue?.vcElementType == VCElementType.LIBRARY) {
                val file = searchFor.containingFile
                try {
                    if (file is TomlFile) {
                        VersionsTomlPsiCache.getLibraryReferences(file).filter { keyValue.key.textMatches(it.text.unquote()) }
                            .forEach { if (!consumer.process(TomlReference(it, VCElementType.LIBRARY))) throw StopComputeException() }
                    }
                    FilenameIndex.getAllFilesByExt(queryParameters.project, "kts").filter { it.name == GradleConstants.KOTLIN_DSL_SCRIPT_NAME }
                        .map { it.toPsiFile(queryParameters.project) }
                        .filterIsInstance<KtFile>()
                        .map { ktFile ->
                            BuildGradleKtsPsiCache.getAccessors(ktFile, VCElementType.LIBRARY).filter { keyValue.key.textMatches(it.id) }
                                .forEach { if (!consumer.process(ResolvedPsiReference(it.element, keyValue))) throw StopComputeException() }
                        }
                } catch (_: StopComputeException) {
                    return@compute false
                }
            }
            true
        }
}


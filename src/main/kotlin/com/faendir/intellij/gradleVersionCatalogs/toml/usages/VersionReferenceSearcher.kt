package com.faendir.intellij.gradleVersionCatalogs.toml.usages

import com.faendir.intellij.gradleVersionCatalogs.kotlin.cache.BuildGradleKtsPsiCache
import com.faendir.intellij.gradleVersionCatalogs.toml.cache.VersionsTomlPsiCache
import com.faendir.intellij.gradleVersionCatalogs.toml.isVersionDef
import com.faendir.intellij.gradleVersionCatalogs.toml.reference.ResolvedPsiReference
import com.faendir.intellij.gradleVersionCatalogs.toml.reference.TomlVersionReference
import com.faendir.intellij.gradleVersionCatalogs.toml.unquote
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiReference
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlKey
import org.toml.lang.psi.TomlKeySegment
import org.toml.lang.psi.TomlKeyValue

class VersionReferenceSearcher : QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
    override fun execute(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>): Boolean = ReadAction.compute<_, Exception> {
        val searchFor = queryParameters.elementToSearch
        val key = ((searchFor as? TomlKeySegment)?.parent ?: searchFor) as? TomlKey
        val keyValue = (key?.parent ?: searchFor) as? TomlKeyValue
        if (keyValue?.isVersionDef() == true) {
            val text = keyValue.key.text
            val file = searchFor.containingFile
            try {
                if (file is TomlFile) {
                    VersionsTomlPsiCache.getVersionReferences(file).filter { it.text.unquote() == text }
                        .forEach { if (!consumer.process(TomlVersionReference(it))) throw StopComputeException() }
                }
                FilenameIndex.getAllFilesByExt(queryParameters.project, "kts").filter { it.name == "build.gradle.kts" }
                    .map { it.toPsiFile(queryParameters.project) }
                    .filterIsInstance<KtFile>()
                    .map { ktFile ->
                        BuildGradleKtsPsiCache.getVersionAccessors(ktFile).filter { it.second == text }
                            .forEach { if (!consumer.process(ResolvedPsiReference(it.first, keyValue))) throw StopComputeException() }
                    }
            } catch (_: StopComputeException) {
                return@compute false
            }
        }
        true
    }
}


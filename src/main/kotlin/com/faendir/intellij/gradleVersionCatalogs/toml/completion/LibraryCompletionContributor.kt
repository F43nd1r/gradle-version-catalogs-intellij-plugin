package com.faendir.intellij.gradleVersionCatalogs.toml.completion

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.faendir.intellij.gradleVersionCatalogs.toml.inVersionsToml
import com.faendir.intellij.gradleVersionCatalogs.toml.isLibraryModuleDef
import com.faendir.intellij.gradleVersionCatalogs.toml.vcElementType
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.jetbrains.concurrency.Promise
import org.jetbrains.idea.maven.onlinecompletion.model.MavenRepositoryArtifactInfo
import org.jetbrains.idea.reposearch.DependencySearchService
import org.jetbrains.idea.reposearch.RepositoryArtifactData
import org.jetbrains.idea.reposearch.SearchParameters
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlLiteral
import java.util.concurrent.ConcurrentLinkedDeque

@Suppress("UnstableApiUsage")
class LibraryCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inVersionsToml(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    val literal = parameters.position.parent as? TomlLiteral ?: return
                    val keyValue = literal.parent as? TomlKeyValue ?: return
                    val withVersion = when {
                        keyValue.vcElementType == VCElementType.LIBRARY -> true
                        keyValue.isLibraryModuleDef() -> false
                        else -> return
                    }
                    result.stopHere()
                    val completionPrefix = CompletionUtil.findReferenceOrAlphanumericPrefix(parameters)
                    val cld = ConcurrentLinkedDeque<MavenRepositoryArtifactInfo>()
                    val split = completionPrefix.split(":")
                    val groupId = split[0]
                    val artifactId = split.getOrNull(1)
                    val dependencySearch = DependencySearchService.getInstance(literal.project)
                    val searchPromise = searchStringDependency(groupId, artifactId, dependencySearch, createSearchParameters(parameters)) {
                        (it as? MavenRepositoryArtifactInfo)?.let { artifactInfo -> cld.add(artifactInfo) }
                    }
                    result.restartCompletionOnAnyPrefixChange()
                    waitAndAdd(searchPromise, cld) { info ->
                        result.addElement(
                            LookupElementBuilder.create("${info.groupId}:${info.artifactId}${if (withVersion && info.version != null) ":${info.version}" else ""}")
                        )
                    }
                }
            }
        )
    }

    private fun searchStringDependency(
        groupId: String,
        artifactId: String?,
        service: DependencySearchService,
        searchParameters: SearchParameters,
        consumer: (RepositoryArtifactData) -> Unit
    ): Promise<Int> {
        return if (artifactId == null) {
            service.fulltextSearch(groupId, searchParameters, consumer)
        } else {
            service.suggestPrefix(groupId, artifactId, searchParameters, consumer)
        }
    }

    private fun waitAndAdd(
        searchPromise: Promise<Int>,
        cld: ConcurrentLinkedDeque<MavenRepositoryArtifactInfo>,
        handler: (MavenRepositoryArtifactInfo) -> Unit
    ) {
        while (searchPromise.state == Promise.State.PENDING || !cld.isEmpty()) {
            ProgressManager.checkCanceled()
            val item = cld.poll()
            if (item != null) {
                handler.invoke(item)
            }
        }
    }

    private fun createSearchParameters(params: CompletionParameters): SearchParameters {
        return SearchParameters(params.invocationCount < 2, ApplicationManager.getApplication().isUnitTestMode)
    }
}

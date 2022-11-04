package com.faendir.intellij.gradleVersionCatalogs.toml.completion

import com.faendir.intellij.gradleVersionCatalogs.toml.findLibraries
import com.faendir.intellij.gradleVersionCatalogs.toml.findVersions
import com.faendir.intellij.gradleVersionCatalogs.toml.inVersionsToml
import com.faendir.intellij.gradleVersionCatalogs.toml.isBundleDef
import com.faendir.intellij.gradleVersionCatalogs.toml.isVersionRef
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.toml.lang.psi.TomlArray
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlLiteral

class LibraryRefCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inVersionsToml(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    val literal = parameters.position.parent as? TomlLiteral ?: return
                    val array = literal.parent as? TomlArray ?: return
                    val keyValue = array.parent as? TomlKeyValue ?: return
                    if (!keyValue.isBundleDef()) return
                    val possibleKeys = keyValue.findLibraries()?.map { it.key.text } ?: return
                    result.addAllElements(possibleKeys.map { LookupElementBuilder.create(it) })
                }
            }
        )
    }
}

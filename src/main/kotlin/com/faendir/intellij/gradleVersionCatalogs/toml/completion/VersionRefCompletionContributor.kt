package com.faendir.intellij.gradleVersionCatalogs.toml.completion

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.faendir.intellij.gradleVersionCatalogs.toml.cache.VersionsTomlPsiCache
import com.faendir.intellij.gradleVersionCatalogs.toml.inVersionsToml
import com.faendir.intellij.gradleVersionCatalogs.toml.isVersionRef
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlLiteral

class VersionRefCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inVersionsToml(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    val literal = parameters.position.parent as? TomlLiteral ?: return
                    val keyValue = literal.parent as? TomlKeyValue ?: return
                    if (!keyValue.isVersionRef()) return
                    val file = keyValue.containingFile as? TomlFile ?: return
                    val possibleKeys = VersionsTomlPsiCache.getDefinitions(file, VCElementType.VERSION).map { it.key.text }
                    result.addAllElements(possibleKeys.map { LookupElementBuilder.create(it) })
                }
            }
        )
    }
}

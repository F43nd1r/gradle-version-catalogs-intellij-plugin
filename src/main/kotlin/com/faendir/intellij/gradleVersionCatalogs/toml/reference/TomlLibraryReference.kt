package com.faendir.intellij.gradleVersionCatalogs.toml.reference

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.faendir.intellij.gradleVersionCatalogs.toml.cache.VersionsTomlPsiCache
import com.faendir.intellij.gradleVersionCatalogs.toml.unquote
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlLiteral

class TomlLibraryReference(element: TomlLiteral) : PsiReferenceBase<TomlLiteral>(element) {
    private val referencedName = element.text.unquote()
    override fun resolve(): PsiElement? = (element.containingFile as? TomlFile)?.let { VersionsTomlPsiCache.getDefinitions(it, VCElementType.LIBRARY) }?.find { it.key.text == referencedName }
}
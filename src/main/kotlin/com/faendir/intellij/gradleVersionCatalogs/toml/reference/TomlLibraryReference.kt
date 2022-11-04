package com.faendir.intellij.gradleVersionCatalogs.toml.reference

import com.faendir.intellij.gradleVersionCatalogs.toml.findLibraries
import com.faendir.intellij.gradleVersionCatalogs.toml.findVersions
import com.faendir.intellij.gradleVersionCatalogs.toml.unquote
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.toml.lang.psi.TomlLiteral

class TomlLibraryReference(element: TomlLiteral) : PsiReferenceBase<TomlLiteral>(element) {
    private val referencedName = element.text.unquote()
    override fun resolve(): PsiElement? = element.findLibraries()?.find { it.key.text == referencedName }
}
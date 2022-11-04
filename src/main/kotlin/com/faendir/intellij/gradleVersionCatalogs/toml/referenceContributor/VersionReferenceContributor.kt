package com.faendir.intellij.gradleVersionCatalogs.toml.referenceContributor

import com.faendir.intellij.gradleVersionCatalogs.toml.inVersionsToml
import com.faendir.intellij.gradleVersionCatalogs.toml.isVersionRef
import com.faendir.intellij.gradleVersionCatalogs.toml.reference.TomlVersionReference
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlLiteral

class VersionReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(TomlLiteral::class.java).inVersionsToml(),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
                    if ((element.parent as? TomlKeyValue)?.isVersionRef() == true) {
                        arrayOf(TomlVersionReference(element as TomlLiteral))
                    } else {
                        PsiReference.EMPTY_ARRAY
                    }
            }
        )
    }
}


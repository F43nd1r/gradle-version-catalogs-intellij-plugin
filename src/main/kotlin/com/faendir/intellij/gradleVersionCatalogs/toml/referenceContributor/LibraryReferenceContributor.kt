package com.faendir.intellij.gradleVersionCatalogs.toml.referenceContributor

import com.faendir.intellij.gradleVersionCatalogs.toml.inVersionsToml
import com.faendir.intellij.gradleVersionCatalogs.toml.isBundleLibraryRef
import com.faendir.intellij.gradleVersionCatalogs.toml.reference.TomlLibraryReference
import com.faendir.intellij.gradleVersionCatalogs.toml.reference.TomlVersionReference
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import org.toml.lang.psi.TomlLiteral

class LibraryReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(TomlLiteral::class.java).inVersionsToml(),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
                    if ((element as? TomlLiteral)?.isBundleLibraryRef() == true) {
                        arrayOf(TomlLibraryReference(element))
                    } else {
                        PsiReference.EMPTY_ARRAY
                    }
            }
        )
    }
}


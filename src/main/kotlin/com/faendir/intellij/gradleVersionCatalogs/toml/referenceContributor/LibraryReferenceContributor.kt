package com.faendir.intellij.gradleVersionCatalogs.toml.referenceContributor

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.faendir.intellij.gradleVersionCatalogs.toml.inVersionsToml
import com.faendir.intellij.gradleVersionCatalogs.toml.isBundleLibraryRef
import com.faendir.intellij.gradleVersionCatalogs.toml.reference.TomlReference
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.toml.lang.psi.TomlLiteral

class LibraryReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(TomlLiteral::class.java).inVersionsToml(),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
                    if ((element as? TomlLiteral)?.isBundleLibraryRef() == true) {
                        arrayOf(TomlReference(element, VCElementType.LIBRARY))
                    } else {
                        PsiReference.EMPTY_ARRAY
                    }
            }
        )
    }
}


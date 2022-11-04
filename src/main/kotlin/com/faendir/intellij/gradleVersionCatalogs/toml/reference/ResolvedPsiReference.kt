package com.faendir.intellij.gradleVersionCatalogs.toml.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class ResolvedPsiReference<T : PsiElement>(element: T, private val target: PsiElement) : PsiReferenceBase<T>(element) {
    override fun resolve(): PsiElement = target
}
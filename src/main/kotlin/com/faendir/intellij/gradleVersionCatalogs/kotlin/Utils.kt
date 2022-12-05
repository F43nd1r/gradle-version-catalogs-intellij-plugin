package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.intellij.psi.PsiElement

fun PsiElement.findPluginAccessor(): String? = PotentialAccessor(this).takeIf { it.isPluginAccessor() }?.segments?.drop(1)?.joinToString("-")

fun PsiElement.findVersionAccessor(): String? = PotentialAccessor(this).takeIf { it.isVersionAccessor() }?.segments?.drop(1)?.joinToString("-")

fun PsiElement.findLibraryAccessor(): String? = PotentialAccessor(this).takeIf { it.isLibraryAccessor() }?.segments?.joinToString("-")

fun PsiElement.findBundleAccessor(): String? = PotentialAccessor(this).takeIf { it.isBundleAccessor() }?.segments?.drop(1)?.joinToString("-")
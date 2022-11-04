package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil

fun PsiElement.findPluginAccessor(): String? {
    val referencedElement = lastChild.references.map { it.resolve() }.firstIsInstanceOrNull<PsiMethod>()
    val returnType = referencedElement?.returnType
    if (returnType != null) {
        val segments = text.replace(Regex("\\s+"), "").split(".").drop(1)
        when {
            returnType.isAssignableFrom(
                TypesUtil.createGenericType(
                    "org.gradle.api.provider.Provider",
                    this,
                    TypesUtil.createType("org.gradle.plugin.use.PluginDependency", this)
                )
            ) && segments.firstOrNull() == "plugins" -> {
                return segments.drop(1).joinToString("-")
            }
        }
    }
    return null
}

fun PsiElement.findVersionAccessor(): String? {
    val referencedElement = lastChild.references.map { it.resolve() }.firstIsInstanceOrNull<PsiMethod>()
    val returnType = referencedElement?.returnType
    if (returnType != null) {
        val segments = text.replace(Regex("\\s+"), "").split(".").drop(1)
        when {
            returnType.isAssignableFrom(
                TypesUtil.createGenericType(
                    "org.gradle.api.provider.Provider",
                    this,
                    TypesUtil.createType("java.lang.String", this)
                )
            ) && segments.firstOrNull() == "versions" -> {
                return segments.drop(1).joinToString("-")
            }
        }
    }
    return null
}

fun PsiElement.findLibraryAccessor(): String? {
    val referencedElement = lastChild.references.map { it.resolve() }.firstIsInstanceOrNull<PsiMethod>()
    val returnType = referencedElement?.returnType
    if (returnType != null) {
        val segments = text.replace(Regex("\\s+"), "").split(".").drop(1)
        when {
            returnType.isAssignableFrom(
                TypesUtil.createGenericType(
                    "org.gradle.api.provider.Provider",
                    this,
                    TypesUtil.createType("org.gradle.api.artifacts.MinimalExternalModuleDependency", this)
                )
            ) -> {
                return segments.joinToString("-")
            }
        }
    }
    return null
}

fun PsiElement.findBundleAccessor(): String? {
    val referencedElement = lastChild.references.map { it.resolve() }.firstIsInstanceOrNull<PsiMethod>()
    val returnType = referencedElement?.returnType
    if (returnType != null) {
        val segments = text.replace(Regex("\\s+"), "").split(".").drop(1)
        when {
            returnType.isAssignableFrom(
                TypesUtil.createGenericType(
                    "org.gradle.api.provider.Provider",
                    this,
                    TypesUtil.createType("org.gradle.api.artifacts.ExternalModuleDependencyBundle", this)
                )
            ) && segments.firstOrNull() == "bundles" -> {
                return segments.drop(1).joinToString("-")
            }
        }
    }
    return null
}
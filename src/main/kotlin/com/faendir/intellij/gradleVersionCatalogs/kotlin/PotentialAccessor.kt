package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil

class PotentialAccessor(val element: PsiElement) {
    val returnType = element.lastChild.references.map { it.resolve() }.firstIsInstanceOrNull<PsiMethod>()?.returnType
    val segments by lazy { element.text.replace(Regex("\\s+"), "").split(".").drop(1) }

    fun isPluginAccessor() = returnType != null && returnType.isAssignableFrom(
        TypesUtil.createGenericType(
            "org.gradle.api.provider.Provider",
            element,
            TypesUtil.createType("org.gradle.plugin.use.PluginDependency", element)
        )
    ) && segments.firstOrNull() == "plugins"

    fun isVersionAccessor() = returnType != null && returnType.isAssignableFrom(
        TypesUtil.createGenericType(
            "org.gradle.api.provider.Provider",
            element,
            TypesUtil.createType("java.lang.String", element)
        )
    ) && segments.firstOrNull() == "versions"

    fun isLibraryAccessor() = returnType != null && returnType.isAssignableFrom(
        TypesUtil.createGenericType(
            "org.gradle.api.provider.Provider",
            element,
            TypesUtil.createType("org.gradle.api.artifacts.MinimalExternalModuleDependency", element)
        )
    )

    fun isBundleAccessor() = returnType != null && returnType.isAssignableFrom(
        TypesUtil.createGenericType(
            "org.gradle.api.provider.Provider",
            element,
            TypesUtil.createType("org.gradle.api.artifacts.ExternalModuleDependencyBundle", element)
        )
    ) && segments.firstOrNull() == "bundles"
}
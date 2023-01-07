package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil

private const val PROVIDER = "org.gradle.api.provider.Provider"
private const val LIBRARY_DEPENDENCY = "org.gradle.api.artifacts.MinimalExternalModuleDependency"
private const val STRING = "java.lang.String"
private const val DEPENDENCY_BUNDLE = "org.gradle.api.artifacts.ExternalModuleDependencyBundle"
private const val PLUGIN_DEPENDENCY = "org.gradle.plugin.use.PluginDependency"
private const val LIBRARY_SUPPLIER = "org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.DependencyNotationSupplier"
private const val VERSION_SUPPLIER = "org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.VersionNotationSupplier"
private const val BUNDLE_SUPPLIER = "org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.BundleNotationSupplier"
private const val PLUGIN_SUPPLIER = "org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.PluginNotationSupplier"

class PotentialAccessor(private val element: PsiElement) {
    val returnType = element.lastChild.references.map { it.resolve() }.firstIsInstanceOrNull<PsiMethod>()?.returnType
    private val segments by lazy { element.text.replace(Regex("\\s+"), "").split(".").drop(1) }

    private fun isLibraryAccessor() = returnType != null && (
            returnType.extendsFrom(TypesUtil.createGenericType(PROVIDER, element, TypesUtil.createType(LIBRARY_DEPENDENCY, element))) ||
                    returnType.extendsFrom(TypesUtil.createType(LIBRARY_SUPPLIER, element))
            )

    private fun isVersionAccessor() = returnType != null && (
            returnType.extendsFrom(TypesUtil.createGenericType(PROVIDER, element, TypesUtil.createType(STRING, element))) ||
                    returnType.extendsFrom(TypesUtil.createType(VERSION_SUPPLIER, element))
            ) &&
            segments.firstOrNull() == "versions"

    private fun isBundleAccessor() = returnType != null && (
            returnType.extendsFrom(TypesUtil.createGenericType(PROVIDER, element, TypesUtil.createType(DEPENDENCY_BUNDLE, element))) ||
                    returnType.extendsFrom(TypesUtil.createType(BUNDLE_SUPPLIER, element))
            ) &&
            segments.firstOrNull() == "bundles"

    private fun isPluginAccessor() = returnType != null && (
            returnType.extendsFrom(TypesUtil.createGenericType(PROVIDER, element, TypesUtil.createType(PLUGIN_DEPENDENCY, element))) ||
                    returnType.extendsFrom(TypesUtil.createType(PLUGIN_SUPPLIER, element))
            ) &&
            segments.firstOrNull() == "plugins"

    fun asLibraryAccessor() = takeIf { it.isLibraryAccessor() }?.run { Accessor(element, segments.joinToString("-")) }
    fun asVersionAccessor() = takeIf { it.isVersionAccessor() }?.run { Accessor(element, segments.drop(1).joinToString("-")) }
    fun asBundleAccessor() = takeIf { it.isBundleAccessor() }?.run { Accessor(element, segments.drop(1).joinToString("-")) }
    fun asPluginAccessor() = takeIf { it.isPluginAccessor() }?.run { Accessor(element, segments.drop(1).joinToString("-")) }
}

data class Accessor(val element: PsiElement, val id: String)

fun PsiType.extendsFrom(other: PsiType) = other.isAssignableFrom(this)
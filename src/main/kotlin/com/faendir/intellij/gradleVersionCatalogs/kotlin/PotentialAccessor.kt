package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.intellij.psi.*
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

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
            returnType.extendsFrom(createGenericType(PROVIDER, element, createType(LIBRARY_DEPENDENCY, element))) ||
                    returnType.extendsFrom(createType(LIBRARY_SUPPLIER, element))
            )

    private fun isVersionAccessor() = returnType != null && (
            returnType.extendsFrom(createGenericType(PROVIDER, element, createType(STRING, element))) ||
                    returnType.extendsFrom(createType(VERSION_SUPPLIER, element))
            ) &&
            segments.firstOrNull() == "versions"

    private fun isBundleAccessor() = returnType != null && (
            returnType.extendsFrom(createGenericType(PROVIDER, element, createType(DEPENDENCY_BUNDLE, element))) ||
                    returnType.extendsFrom(createType(BUNDLE_SUPPLIER, element))
            ) &&
            segments.firstOrNull() == "bundles"

    private fun isPluginAccessor() = returnType != null && (
            returnType.extendsFrom(createGenericType(PROVIDER, element, createType(PLUGIN_DEPENDENCY, element))) ||
                    returnType.extendsFrom(createType(PLUGIN_SUPPLIER, element))
            ) &&
            segments.firstOrNull() == "plugins"

    fun asLibraryAccessor() = takeIf { it.isLibraryAccessor() }?.run { Accessor(element, segments.joinToString("-")) }
    fun asVersionAccessor() = takeIf { it.isVersionAccessor() }?.run { Accessor(element, segments.drop(1).joinToString("-")) }
    fun asBundleAccessor() = takeIf { it.isBundleAccessor() }?.run { Accessor(element, segments.drop(1).joinToString("-")) }
    fun asPluginAccessor() = takeIf { it.isPluginAccessor() }?.run { Accessor(element, segments.drop(1).joinToString("-")) }
}

data class Accessor(val element: PsiElement, val id: String)

fun PsiType.extendsFrom(other: PsiType) = other.isAssignableFrom(this)

fun createGenericType(fqn: String, context: PsiElement, type: PsiType): PsiClassType {
    val facade = JavaPsiFacade.getInstance(context.project)
    val resolveScope = context.resolveScope
    val clazz = facade.findClass(fqn, resolveScope)
    return if (clazz == null || clazz.typeParameters.size != 1) {
        facade.elementFactory.createTypeByFQClassName(fqn, resolveScope)
    } else {
        facade.elementFactory.createType(clazz, type)
    }
}

fun createType(fqn: String, context: PsiElement): PsiClassType {
    return JavaPsiFacade.getInstance(context.project).elementFactory.createTypeByFQClassName(fqn, context.resolveScope)
}
package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.intellij.psi.*
import org.jetbrains.kotlin.utils.addToStdlib.applyIf
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


@Suppress("SpellCheckingInspection")
data class Accessor(val element: PsiElement, val id: String, val type: VCElementType) {
    companion object {
        fun find(element: PsiElement): Accessor? {
            val references = if (element.lastChild.references.mapNotNull(PsiReference::resolve).isEmpty()) {
                element.lastChild.firstChild.references
            } else element.lastChild.references
            val returnType = references.map { it.resolve() }.firstIsInstanceOrNull<PsiMethod>()?.returnType ?: return null
            val segments by lazy { element.text.replace(Regex("\\s+"), "").split(".").drop(1)
                // e.g. libs.map.get3dmap() -> [libs, map, get3dmap()] -> [map, get3dmap()] -> [map, 3dmap]
                .map {
                    it.applyIf(it.matches(Regex("^get.*\\(\\s*\\)\$"))) {
                        removePrefix("get").removeSuffix("()")
                    }
                }
            }
            val type = when {
                returnType.extendsFrom(createGenericType(PROVIDER, element, createType(LIBRARY_DEPENDENCY, element))) ||
                        returnType.extendsFrom(createType(LIBRARY_SUPPLIER, element)) -> VCElementType.LIBRARY

                (
                        returnType.extendsFrom(createGenericType(PROVIDER, element, createType(STRING, element))) ||
                                returnType.extendsFrom(createType(VERSION_SUPPLIER, element))
                        ) && segments.firstOrNull() == "versions" -> VCElementType.VERSION

                (
                        returnType.extendsFrom(createGenericType(PROVIDER, element, createType(DEPENDENCY_BUNDLE, element))) ||
                                returnType.extendsFrom(createType(BUNDLE_SUPPLIER, element))
                        ) && segments.firstOrNull() == "bundles" -> VCElementType.BUNDLE

                (
                        returnType.extendsFrom(createGenericType(PROVIDER, element, createType(PLUGIN_DEPENDENCY, element))) ||
                                returnType.extendsFrom(createType(PLUGIN_SUPPLIER, element))
                        ) && segments.firstOrNull() == "plugins" -> VCElementType.PLUGIN

                else -> return null
            }
            val id = (if (type != VCElementType.LIBRARY) segments.drop(1) else segments).joinToString("-")
            return Accessor(element, id, type)
        }
    }
}

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
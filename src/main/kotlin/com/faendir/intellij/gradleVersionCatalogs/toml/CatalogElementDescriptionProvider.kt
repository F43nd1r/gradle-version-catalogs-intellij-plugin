package com.faendir.intellij.gradleVersionCatalogs.toml

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewTypeLocation
import org.toml.lang.psi.TomlKeySegment
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlLiteral

class CatalogElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        return when (location) {
            is UsageViewLongNameLocation -> getText(element)
            is UsageViewTypeLocation -> {
                val keyValue = element as? TomlKeyValue ?: element.parent as? TomlKeyValue ?: element.parent?.parent as? TomlKeyValue
                keyValue?.let {
                    when (it.vcElementType) {
                        VCElementType.VERSION -> "Catalog Version"
                        VCElementType.LIBRARY -> "Catalog Library"
                        VCElementType.BUNDLE -> "Catalog Bundle"
                        VCElementType.PLUGIN -> "Catalog Plugin"
                        null -> null
                    }
                } ?: getText(element)
            }

            else -> null
        }
    }

    private fun getText(element: PsiElement) = when (element) {
        is TomlLiteral -> element.text.unquote()
        is TomlKeySegment -> element.text
        else -> null
    }
}

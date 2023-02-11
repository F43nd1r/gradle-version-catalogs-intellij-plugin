package com.faendir.intellij.gradleVersionCatalogs.toml

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.psi.PsiElement
import org.toml.lang.psi.TomlKey
import org.toml.lang.psi.TomlKeyValue

class CatalogDeclarationFindUsagesHandlerFactory : FindUsagesHandlerFactory() {
    override fun canFindUsages(element: PsiElement): Boolean {
        val key = element.parent as? TomlKey ?: return false
        val keyValue = key.parent as? TomlKeyValue ?: return false
        return keyValue.vcElementType != null
    }

    override fun createFindUsagesHandler(searchFor: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler {
        return object : FindUsagesHandler(searchFor) {}
    }
}

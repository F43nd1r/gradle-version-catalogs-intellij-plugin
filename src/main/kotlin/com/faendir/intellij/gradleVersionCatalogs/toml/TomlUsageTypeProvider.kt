package com.faendir.intellij.gradleVersionCatalogs.toml

import com.intellij.psi.PsiElement
import com.intellij.usages.UsageTarget
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProviderEx
import org.toml.lang.psi.TomlKeySegment
import org.toml.lang.psi.TomlLiteral

class TomlUsageTypeProvider : UsageTypeProviderEx {
    override fun getUsageType(element: PsiElement, targets: Array<out UsageTarget>): UsageType? {
        return getUsageType(element)
    }

    override fun getUsageType(element: PsiElement): UsageType? {
        return when (element) {
            is TomlLiteral -> return UsageType.READ
            is TomlKeySegment -> return UsageType.WRITE
            else -> null
        }
    }
}

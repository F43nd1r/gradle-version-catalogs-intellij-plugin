package com.faendir.intellij.gradleVersionCatalogs.toml

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.VirtualFilePattern
import com.intellij.psi.PsiWhiteSpace
import org.toml.lang.psi.TomlArray
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlLiteral
import org.toml.lang.psi.TomlTable

fun String.unquote() = when {
    startsWith("\"\"\"") && endsWith("\"\"\"") -> substring(3, length - 3)
    first() == '"' && last() == '"' -> substring(1, length - 1)
    else -> this
}

fun PsiElementPattern<*, *>.inVersionsToml(): PsiElementPattern<*, *> =
    inVirtualFile(VirtualFilePattern().withName(StandardPatterns.string().endsWith("versions.toml")))

fun TomlKeyValue.isVersionRef() = key.children.filter { it !is PsiWhiteSpace }.joinToString("") { it.text } == "version.ref"

val TomlKeyValue.vcElementType
    get() = (parent as? TomlTable)?.header?.key?.text?.let { text -> VCElementType.values().find { text == it.tableHeader } }

fun TomlLiteral.isBundleLibraryRef() =
    (((parent as? TomlArray)?.parent as? TomlKeyValue)?.parent as? TomlTable)?.let { it.header.key?.text == VCElementType.BUNDLE.tableHeader } == true

fun TomlKeyValue.isLibraryModuleDef() = key.children.filter { it !is PsiWhiteSpace }.joinToString("") { it.text } == "module"

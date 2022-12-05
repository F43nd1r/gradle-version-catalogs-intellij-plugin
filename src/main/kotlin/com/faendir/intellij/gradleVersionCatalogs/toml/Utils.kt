package com.faendir.intellij.gradleVersionCatalogs.toml

import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.VirtualFilePattern
import com.intellij.psi.PsiWhiteSpace
import org.toml.lang.psi.TomlArray
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlLiteral
import org.toml.lang.psi.TomlTable

fun TomlTable.isVersionTable() = header.key?.text == "versions"

fun TomlTable.isLibraryTable() = header.key?.text == "libraries"

fun TomlTable.isPluginsTable() = header.key?.text == "plugins"

fun TomlTable.isBundleTable() = header.key?.text == "bundles"

fun String.unquote() = when {
    startsWith("\"\"\"") && endsWith("\"\"\"") -> substring(3, length - 3)
    first() == '"' && last() == '"' -> substring(1, length - 1)
    else -> this
}

fun PsiElementPattern<*, *>.inVersionsToml(): PsiElementPattern<*, *> = inVirtualFile(VirtualFilePattern().withName(StandardPatterns.string().endsWith("versions.toml")))

fun TomlKeyValue.isVersionRef() = key.children.filter { it !is PsiWhiteSpace }.joinToString("") { it.text } == "version.ref"

fun TomlKeyValue.isVersionDef() = (parent as? TomlTable)?.isVersionTable() == true

fun TomlKeyValue.isLibraryDef() = (parent as? TomlTable)?.isLibraryTable() == true

fun TomlKeyValue.isPluginDef() = (parent as? TomlTable)?.isPluginsTable() == true

fun TomlKeyValue.isBundleDef() = (parent as? TomlTable)?.isBundleTable() == true

fun TomlLiteral.isBundleLibraryRef() = (((parent as? TomlArray)?.parent as? TomlKeyValue)?.parent as? TomlTable)?.isBundleTable() == true

fun TomlKeyValue.isLibraryModuleDef() = key.children.filter { it !is PsiWhiteSpace }.joinToString("") { it.text } == "module"

package com.faendir.intellij.gradleVersionCatalogs.toml

import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.VirtualFilePattern
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.childrenOfType
import org.toml.lang.psi.TomlArray
import org.toml.lang.psi.TomlElement
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlLiteral
import org.toml.lang.psi.TomlTable
import org.toml.lang.psi.TomlValue

fun TomlElement.findVersions(): List<TomlKeyValue>? = (containingFile as? TomlFile)
    ?.childrenOfType<TomlTable>()?.find { it.isVersionTable() }
    ?.childrenOfType()

fun TomlElement.findLibraries(): List<TomlKeyValue>? = (containingFile as? TomlFile)
    ?.childrenOfType<TomlTable>()?.find { it.isLibraryTable() }
    ?.childrenOfType()

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

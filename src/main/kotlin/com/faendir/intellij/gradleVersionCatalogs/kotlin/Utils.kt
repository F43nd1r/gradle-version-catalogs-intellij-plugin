package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlKeyValue

fun Project.findInVersionsTomlKeyValues(getKeyValues: (TomlFile) -> List<TomlKeyValue>, search: String): List<TomlKeyValue> {
    return FilenameIndex.getAllFilesByExt(this, "toml")
        .filter { it.name.endsWith("versions.toml") }
        .map { it.toPsiFile(this) }
        .filterIsInstance<TomlFile>()
        .flatMap { file -> getKeyValues(file).filter { it.key.textMatches(search) } }
}
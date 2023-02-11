package com.faendir.intellij.gradleVersionCatalogs

enum class VCElementType(val tableHeader: String) {
    LIBRARY("libraries"), VERSION("versions"), BUNDLE("bundles"), PLUGIN("plugins")
}
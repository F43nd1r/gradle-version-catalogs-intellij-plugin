package com.faendir.intellij.gradleVersionCatalogs.kotlin.inlayhints

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.faendir.intellij.gradleVersionCatalogs.kotlin.cache.BuildGradleKtsPsiCache
import com.faendir.intellij.gradleVersionCatalogs.kotlin.findInVersionsTomlKeyValues
import com.faendir.intellij.gradleVersionCatalogs.toml.cache.VersionsTomlPsiCache
import com.faendir.intellij.gradleVersionCatalogs.toml.isVersionRef
import com.faendir.intellij.gradleVersionCatalogs.toml.unquote
import com.faendir.intellij.gradleVersionCatalogs.toml.vcElementType
import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.stubs.elements.KtDotQualifiedExpressionElementType
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.toml.lang.psi.*
import org.toml.lang.psi.ext.elementType
import javax.swing.JPanel

@Suppress("UnstableApiUsage")
class VersionCatalogInlayHintsProvider : InlayHintsProvider<NoSettings> {

    override val key: SettingsKey<NoSettings> = SettingsKey("group.names.gradle")
    override val name: String = "Gradle Version Catalog references"
    override val group: InlayGroup = InlayGroup.OTHER_GROUP
    override val previewText: String? = null

    override fun createSettings(): NoSettings = NoSettings()

    override fun getCollectorFor(file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): InlayHintsCollector? {
        if (file is KtFile && file.name == GradleConstants.KOTLIN_DSL_SCRIPT_NAME) {
            return object : FactoryInlayHintsCollector(editor) {
                override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                    if (element.elementType is KtDotQualifiedExpressionElementType) {
                        val accessor = BuildGradleKtsPsiCache.findAccessor(element)
                        if (accessor != null && BuildGradleKtsPsiCache.findAccessor(element.parent) == null) {
                            val referencedElement = element.project.findInVersionsTomlKeyValues({ VersionsTomlPsiCache.getDefinitions(it, accessor.type) }, accessor.id)
                                    .firstOrNull()
                            if (referencedElement != null) {
                                val referencedValue = referencedElement.value
                                if (referencedValue != null) {
                                    val inlayText: String? = when (referencedElement.vcElementType) {
                                        VCElementType.LIBRARY -> resolvePotentiallyTabledDefinition(referencedValue, "module")
                                        VCElementType.VERSION -> referencedValue.text?.unquote()
                                        VCElementType.PLUGIN -> resolvePotentiallyTabledDefinition(referencedValue, "id")
                                        else -> null
                                    }
                                    if (inlayText != null) {
                                        sink.addInlineElement(
                                            element.textOffset + element.textLength,
                                            false,
                                            factory.roundWithBackgroundAndSmallInset(factory.smallText(inlayText)),
                                            false
                                        )
                                    }
                                }
                            }
                        }
                    }
                    return true
                }
            }
        }
        return null
    }

    private fun resolvePotentiallyTabledDefinition(referencedValue: TomlValue, moduleTableKey: String) = when (referencedValue) {
        is TomlTable, is TomlInlineTable -> {
            val keys = referencedValue.childrenOfType<TomlKeyValue>()
            keys.find { it.key.textMatches(moduleTableKey) }?.value?.text?.unquote()?.let { module ->
                (
                        keys.find { it.key.textMatches("version") }?.value?.text?.unquote()
                            ?: keys.find { it.isVersionRef() }?.value?.text?.unquote()?.let { search ->
                                VersionsTomlPsiCache.getDefinitions(
                                    referencedValue.containingFile as TomlFile,
                                    VCElementType.VERSION
                                ).find {
                                    it.key.textMatches(search)
                                }?.value?.text?.unquote()
                            }
                        )
                    ?.let { "$module:${it}" }
            }
        }

        is TomlLiteral -> referencedValue.text.unquote()
        else -> null
    }

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener) = JPanel()
        }
    }
}
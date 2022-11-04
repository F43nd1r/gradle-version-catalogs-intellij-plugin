package com.faendir.intellij.gradleVersionCatalogs.toml

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import org.toml.lang.psi.TomlKeySegment
import org.toml.lang.psi.TomlPsiFactory

class TomlKeySegmentManipulator : AbstractElementManipulator<TomlKeySegment>() {
    override fun handleContentChange(element: TomlKeySegment, range: TextRange, newContent: String?): TomlKeySegment {
        val oldText = element.text
        val newText = "${oldText.substring(0, range.startOffset)}$newContent${oldText.substring(range.endOffset)}"

        val newLiteral = TomlPsiFactory(element.project).createKeySegment(newText)
        return element.replace(newLiteral) as TomlKeySegment
    }
}

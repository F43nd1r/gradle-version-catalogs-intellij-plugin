package com.faendir.intellij.gradleVersionCatalogs.kotlin

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * this is a dummy implementation doing nothing. It needs to be present to prevent a crash.
 */
class KtDotQualifiedExpressionManipulator : AbstractElementManipulator<KtDotQualifiedExpression>() {
    override fun handleContentChange(element: KtDotQualifiedExpression, range: TextRange, newContent: String?): KtDotQualifiedExpression {
        return element
    }
}
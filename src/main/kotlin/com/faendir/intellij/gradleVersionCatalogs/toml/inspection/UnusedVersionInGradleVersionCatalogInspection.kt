package com.faendir.intellij.gradleVersionCatalogs.toml.inspection

import com.faendir.intellij.gradleVersionCatalogs.VCElementType
import com.faendir.intellij.gradleVersionCatalogs.toml.vcElementType
import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.searches.ReferencesSearch
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlVisitor

class UnusedVersionInGradleVersionCatalogInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : TomlVisitor() {
            override fun visitKeyValue(element: TomlKeyValue) {
                if (element.vcElementType == VCElementType.VERSION && ReferencesSearch.search(element).none()) {
                    holder.registerProblem(
                        element,
                        "Version '${element.key.text}' is never used",
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        object : LocalQuickFix {
                            override fun getName(): String = "Remove version '${element.key.text}'"
                            override fun getFamilyName(): String = "Remove version"
                            override fun applyFix(project: Project, descriptor: ProblemDescriptor) = element.delete()
                        }
                    )
                }
                super.visitKeyValue(element)
            }
        }
    }
}

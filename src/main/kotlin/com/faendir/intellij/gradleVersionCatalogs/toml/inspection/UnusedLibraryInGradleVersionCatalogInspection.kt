package com.faendir.intellij.gradleVersionCatalogs.toml.inspection

import com.faendir.intellij.gradleVersionCatalogs.toml.isLibraryDef
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.searches.ReferencesSearch
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlVisitor

class UnusedLibraryInGradleVersionCatalogInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : TomlVisitor() {
            override fun visitKeyValue(element: TomlKeyValue) {
                if (element.isLibraryDef() && ReferencesSearch.search(element).none()) {
                    holder.registerProblem(
                        element,
                        "Library '${element.key.text}' is never used",
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        object : LocalQuickFix {
                            override fun getFamilyName(): String {
                                return "Remove library '${element.key.text}'"
                            }

                            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                                element.delete()
                            }
                        }
                    )
                }
                super.visitKeyValue(element)
            }
        }
    }
}

/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.j2k

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.JavaRecursiveElementVisitor
import java.util.LinkedHashSet
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiElement

fun PsiMethod.isPrimaryConstructor(): Boolean {
    if (!isConstructor()) return false
    val parent = getParent()
    if (parent !is PsiClass) return false
    return parent.getPrimaryConstructor() == this
}

fun PsiClass.getPrimaryConstructor(): PsiMethod? {
    val constructors = getConstructors()
    return when (constructors.size) {
        0 -> null

        1 -> constructors.single()

        else -> {
            // if there is more than one constructor then choose one invoked by all others
            class Visitor() : JavaRecursiveElementVisitor() {
                //TODO: skip all non-constructor members (optimization)
                private val invokedConstructors = LinkedHashSet<PsiMethod>()

                override fun visitReferenceExpression(expression: PsiReferenceExpression) {
                    expression.getReferences()
                            .filter { it.getCanonicalText() == "this" }
                            .map { it.resolve() }
                            .filterIsInstance(javaClass<PsiMethod>())
                            .filterTo(invokedConstructors) { it.isConstructor() }
                }

                val primaryConstructor: PsiMethod?
                    get() = if (invokedConstructors.size == 1) invokedConstructors.single() else null
            }

            val visitor = Visitor()
            accept(visitor)
            visitor.primaryConstructor
        }
    }
}

fun isInsidePrimaryConstructor(element: PsiElement): Boolean
        = containingConstructor(element)?.isPrimaryConstructor() ?: false

fun isInsideSecondaryConstructor(element: PsiElement): Boolean
        = !(containingConstructor(element)?.isPrimaryConstructor() ?: true)

fun containingConstructor(element: PsiElement): PsiMethod? {
    var context = element.getContext()
    while (context != null) {
        val _context = context!!
        if (_context is PsiMethod) {
            return if (_context.isConstructor()) _context else null
        }

        context = _context.getContext()
    }
    return null
}

fun isSuperConstructorRef(ref: PsiReference): Boolean {
    if (ref.getCanonicalText().equals("super")) {
        val target = ref.resolve()
        return target is PsiMethod && target.isConstructor()
    }
    return false
}

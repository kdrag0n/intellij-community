// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.k2.search

import com.intellij.model.search.Searcher
import com.intellij.psi.PsiClass
import com.intellij.psi.search.searches.DirectClassInheritorsSearch
import com.intellij.util.Query
import com.intellij.util.QueryFactory
import com.intellij.util.concurrency.annotations.RequiresReadLock
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KtClassOrObjectSymbol
import org.jetbrains.kotlin.asJava.toFakeLightClass
import org.jetbrains.kotlin.asJava.toLightClass

private val EVERYTHING_BUT_KOTLIN = object : QueryFactory<PsiClass, DirectClassInheritorsSearch.SearchParameters>() {
    init {
        DirectClassInheritorsSearch.EP_NAME.extensionList
            .filter { !(it::class.java.name.equals("org.jetbrains.kotlin.idea.search.ideaExtensions.KotlinDirectInheritorsSearcher")) }
            .forEach { registerExecutor(it) }
    }
}

internal class DirectKotlinClassDelegatedSearcher : Searcher<DirectKotlinClassInheritorsSearch.SearchParameters, KtClassOrObjectSymbol> {
    @ApiStatus.OverrideOnly
    @RequiresReadLock
    override fun collectSearchRequest(parameters: DirectKotlinClassInheritorsSearch.SearchParameters): Query<out KtClassOrObjectSymbol> {
        val baseClass = parameters.ktClass
        val lightClass = baseClass.toLightClass() ?: baseClass.toFakeLightClass()
        val params =
            DirectClassInheritorsSearch.SearchParameters(lightClass, parameters.searchScope, parameters.includeAnonymous, true)
        return EVERYTHING_BUT_KOTLIN.createQuery(params).mapping {
            analyze(baseClass) {
                it.getNamedClassSymbol()
            }
        }.filtering {
            it != null
        }.mapping {
            it!!
        }
    }
}
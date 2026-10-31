package com.unknownrex.sensa.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Type-safe version catalog accessors are not generated for precompiled script plugins in this
 * build, so convention plugins resolve entries through this helper instead.
 */
internal val Project.catalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.lib(alias: String) = catalog.findLibrary(alias).get()

package com.avioconsulting.mule.linter.spi

import com.avioconsulting.mule.linter.catalog.RuleDefinition

/** Register through META-INF/services/com.avioconsulting.mule.linter.spi.RuleProvider. */
interface RuleProvider {
    Collection<RuleDefinition> getRuleDefinitions()
}

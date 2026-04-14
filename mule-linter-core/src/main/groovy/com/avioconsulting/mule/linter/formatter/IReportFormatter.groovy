package com.avioconsulting.mule.linter.formatter

import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.model.rule.RuleExecutor

/**
 * Interface for report formatters.
 * Implementations handle outputting lint results in different formats.
 */
interface IReportFormatter {
    
    /**
     * Format and write the report.
     * 
     * @param executor RuleExecutor containing results
     * @param context FormatterContext with configuration
     */
    void format(RuleExecutor executor, FormatterContext context)
    
    /**
     * The format type this formatter produces
     */
    ReportFormat getFormat()
}

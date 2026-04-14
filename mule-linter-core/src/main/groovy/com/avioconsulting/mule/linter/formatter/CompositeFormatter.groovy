package com.avioconsulting.mule.linter.formatter

import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import com.avioconsulting.mule.linter.model.rule.RuleSeverity

/**
 * Composite formatter that handles multiple output formats simultaneously.
 * Used by both CLI and Maven plugin to output CONSOLE + file formats.
 */
class CompositeFormatter {
    
    private List<IReportFormatter> formatters = []
    
    /**
     * Create composite formatter for the requested formats
     */
    CompositeFormatter(List<ReportFormat> formats) {
        formats.each { format ->
            switch (format) {
                case ReportFormat.CONSOLE:
                    formatters.add(new ConsoleTableFormatter())
                    break
                case ReportFormat.JSON:
                    formatters.add(new JsonReportFormatter())
                    break
                case ReportFormat.XML:
                    formatters.add(new XmlReportFormatter())
                    break
                default:
                    throw new IllegalArgumentException("Unknown format: ${format}")
            }
        }
    }
    
    /**
     * Format results with all configured formatters
     */
    void format(RuleExecutor executor, FormatterContext context) {
        formatters.each { formatter ->
            formatter.format(executor, context)
        }
    }
    
    /**
     * Check if any violations are at or above the threshold
     */
    static boolean hasViolationsAtOrAboveThreshold(RuleExecutor executor, RuleSeverity threshold) {
        return executor.results.any { violation ->
            violation.rule.severity.ordinal() <= threshold.ordinal()
        }
    }
    
    /**
     * Calculate exit code based on violations and threshold
     */
    static int calculateExitCode(RuleExecutor executor, RuleSeverity threshold) {
        return hasViolationsAtOrAboveThreshold(executor, threshold) ? 1 : 0
    }
}

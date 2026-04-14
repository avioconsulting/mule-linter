package com.avioconsulting.mule.linter.formatter

import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import groovy.xml.MarkupBuilder

/**
 * XML report formatter using Groovy's MarkupBuilder.
 * Writes to file in the output directory.
 */
class XmlReportFormatter implements IReportFormatter {
    
    @Override
    ReportFormat getFormat() {
        return ReportFormat.XML
    }
    
    @Override
    void format(RuleExecutor executor, FormatterContext context) {
        // Ensure output directory exists
        if (!context.outputDirectory.exists()) {
            context.outputDirectory.mkdirs()
        }
        
        // Build XML using MarkupBuilder
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        
        xml.violations {
            rulesExecuted(executor.ruleCount)
            executor.results.each { v ->
                violation {
                    ruleId(v.rule.ruleId)
                    ruleName(v.rule.ruleName)
                    severity(v.rule.severity.toString())
                    fileName(v.fileName - (context.applicationPath.absolutePath + "/"))
                    if (v.lineNumber > 0) {
                        lineNumber(v.lineNumber)
                    }
                    message(v.message)
                }
            }
        }
        
        // Write to file
        File outputFile = new File(context.outputDirectory, "mule-linter-report.xml")
        outputFile.text = writer.toString()
    }
}

package com.avioconsulting.mule.linter.formatter

import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * JSON report formatter.
 * Writes to file in the output directory.
 */
class JsonReportFormatter implements IReportFormatter {
    
    @Override
    ReportFormat getFormat() {
        return ReportFormat.JSON
    }
    
    @Override
    void format(RuleExecutor executor, FormatterContext context) {
        // Ensure output directory exists
        if (!context.outputDirectory.exists()) {
            context.outputDirectory.mkdirs()
        }
        
        // Prepare data
        def report = [
            rulesExecuted: executor.ruleCount,
            violations: executor.results.collect { v ->
                [
                    ruleId: v.rule.ruleId,
                    ruleName: v.rule.ruleName,
                    severity: v.rule.severity.toString(),
                    fileName: v.fileName - (context.applicationPath.absolutePath + "/"),
                    lineNumber: v.lineNumber > 0 ? v.lineNumber : null,
                    message: v.message
                ]
            }
        ]
        
        // Write to file
        File outputFile = new File(context.outputDirectory, "mule-linter-report.json")
        Gson gson = new GsonBuilder().setPrettyPrinting().create()
        outputFile.text = gson.toJson(report)
    }
}

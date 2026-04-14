package com.avioconsulting.mule.linter.formatter

import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.model.rule.RuleSeverity

/**
 * Configuration context for formatters.
 * Shared between CLI and Maven plugin.
 */
class FormatterContext {
    
    /**
     * Output directory for file-based reports (JSON, XML)
     */
    File outputDirectory
    
    /**
     * Whether to use ANSI colors (only applicable to console output)
     */
    boolean useColor
    
    /**
     * Minimum severity that causes failure/exit code 1
     */
    RuleSeverity failThreshold
    
    /**
     * Application path (for relative file path calculations)
     */
    File applicationPath
    
    FormatterContext(File outputDirectory, boolean useColor, 
                     RuleSeverity failThreshold, File applicationPath) {
        this.outputDirectory = outputDirectory
        this.useColor = useColor
        this.failThreshold = failThreshold
        this.applicationPath = applicationPath
    }
    
    /**
     * Factory method for CLI usage
     */
    static FormatterContext forCli(File outputDir, boolean useColor, 
                                   RuleSeverity threshold, File appPath) {
        return new FormatterContext(outputDir, useColor, threshold, appPath)
    }
    
    /**
     * Factory method for Maven plugin usage
     */
    static FormatterContext forMaven(File outputDir, boolean useColor, 
                                     RuleSeverity threshold, File appPath) {
        return new FormatterContext(outputDir, useColor, threshold, appPath)
    }
}

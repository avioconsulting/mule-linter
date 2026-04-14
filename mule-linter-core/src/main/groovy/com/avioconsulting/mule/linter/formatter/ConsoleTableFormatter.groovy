package com.avioconsulting.mule.linter.formatter

import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * Console table formatter with box borders and ANSI colors.
 * Outputs to System.out with 130-character width.
 */
class ConsoleTableFormatter implements IReportFormatter {
    
    @Override
    ReportFormat getFormat() {
        return ReportFormat.CONSOLE
    }
    
    @Override
    void format(RuleExecutor executor, FormatterContext context) {
        // Group violations by severity
        def critical = executor.results.findAll { it.rule.severity == RuleSeverity.CRITICAL }
        def major = executor.results.findAll { it.rule.severity == RuleSeverity.MAJOR }
        def minor = executor.results.findAll { it.rule.severity == RuleSeverity.MINOR }
        def blocker = executor.results.findAll { it.rule.severity == RuleSeverity.BLOCKER }
        
        def useColor = context.useColor
        def appPath = context.applicationPath
        
        // Write header
        writeHeaderBox(System.out, executor.ruleCount, critical.size(), major.size(), minor.size(), useColor)
        
        // Write each severity section
        if (blocker) writeSeveritySection(System.out, RuleSeverity.BLOCKER, blocker, useColor, "Failing", appPath)
        if (critical) writeSeveritySection(System.out, RuleSeverity.CRITICAL, critical, useColor, "Failing", appPath)
        if (major) writeSeveritySection(System.out, RuleSeverity.MAJOR, major, useColor, "Failing", appPath)
        if (minor) writeSeveritySection(System.out, RuleSeverity.MINOR, minor, useColor, "Non-failing", appPath)
        
        // Write summary footer
        int failingCount = blocker.size() + critical.size() + major.size()
        writeSummaryFooter(System.out, failingCount, minor.size(), useColor)
    }
    
    private void writeHeaderBox(OutputStream out, int totalRules, int critical, int major, int minor, boolean useColor) {
        String topBorder = "┌" + "─" * 128 + "┐\n"
        String title = "│" + padCenter("Mule Linter Report", 128) + "│\n"
        
        def violationsText = ""
        if (useColor) {
            violationsText = "${critical > 0 ? "@|red ${critical} 🔴|@" : "${critical} 🔴"}  " +
                    "${major > 0 ? "@|yellow ${major} 🟡|@" : "${major} 🟡"}  " +
                    "${minor > 0 ? "@|cyan ${minor} 🔵|@" : "${minor} 🔵"}"
        } else {
            violationsText = "${critical} CRITICAL  ${major} MAJOR  ${minor} MINOR"
        }
        
        String stats = "│ Rules: ${totalRules} executed    │ Violations: ${violationsText}" +
                padRight("", 128 - 23 - "Violations: ${violationsText}".size()) + "│\n"
        
        String bottomBorder = "└" + "─" * 128 + "┘\n"
        
        out.write(topBorder.bytes)
        out.write(title.bytes)
        out.write(stats.bytes)
        out.write(bottomBorder.bytes)
    }
    
    private void writeSeveritySection(OutputStream out, RuleSeverity severity, List<RuleViolation> violations, 
                                      boolean useColor, String label, File appPath) {
        def (emoji, color) = getSeverityStyle(severity, useColor)
        
        // Section header
        String headerText = "${emoji} ${severity} (${violations.size()}) - ${label}"
        String header = "│" + (useColor ? "${color}${headerText}@|reset |@" : headerText) +
                padRight("", 128 - headerText.size()) + "│\n"
        
        // Top separator with column headers
        String separator = "├" + "─" * 30 + "┼" + "─" * 30 + "┼" + "─" * 66 + "┤\n"
        String columnHeaders = "│" + padRight("Rule", 30) + "│" + padRight("Location", 30) +
                "│" + padRight("Message", 66) + "│\n"
        
        // Rows
        StringBuilder rows = new StringBuilder()
        violations.each { v ->
            String location = v.fileName - (appPath.absolutePath + "/")
            if (v.lineNumber > 0) {
                location += ":${v.lineNumber}"
            }
            rows.append(formatRow(v.rule.ruleId, location, v.message))
        }
        
        // Bottom border
        String bottomBorder = "└" + "─" * 30 + "┴" + "─" * 30 + "┴" + "─" * 66 + "┘\n"
        
        out.write(("┌" + "─" * 128 + "┐\n").bytes)
        out.write(header.bytes)
        out.write(separator.bytes)
        out.write(columnHeaders.bytes)
        out.write(separator.bytes)
        out.write(rows.toString().bytes)
        out.write(bottomBorder.bytes)
    }
    
    private void writeSummaryFooter(OutputStream out, int failing, int nonFailing, boolean useColor) {
        String topBorder = "┌" + "─" * 128 + "┐\n"
        
        String summaryText
        if (useColor) {
            def exitColor = failing > 0 ? "@|red,bold" : "@|green,bold"
            summaryText = "Summary: ${failing} failing │ ${nonFailing} non-failing │ Exit Code: ${exitColor} ${failing > 0 ? 1 : 0}|@"
        } else {
            summaryText = "Summary: ${failing} failing │ ${nonFailing} non-failing │ Exit Code: ${failing > 0 ? 1 : 0}"
        }
        
        String summary = "│" + padCenter(summaryText, 128) + "│\n"
        String bottomBorder = "└" + "─" * 128 + "┘\n"
        
        out.write(topBorder.bytes)
        out.write(summary.bytes)
        out.write(bottomBorder.bytes)
    }
    
    private String formatRow(String rule, String location, String message) {
        "│" + padRight(truncate(rule, 29), 30) +
                "│" + padRight(truncate(location, 29), 30) +
                "│" + padRight(truncate(message, 65), 66) + "│\n"
    }
    
    private List getSeverityStyle(RuleSeverity severity, boolean useColor) {
        switch (severity) {
            case RuleSeverity.BLOCKER:
                return ["🛑", useColor ? "@|red,bold " : ""]
            case RuleSeverity.CRITICAL:
                return ["🔴", useColor ? "@|red,bold " : ""]
            case RuleSeverity.MAJOR:
                return ["🟡", useColor ? "@|yellow,bold " : ""]
            case RuleSeverity.MINOR:
                return ["🔵", useColor ? "@|cyan " : ""]
            default:
                return ["⚪", ""]
        }
    }
    
    private String padRight(String s, int width) {
        if (s == null) return " " * width
        if (s.length() >= width) {
            return s.substring(0, width - 1) + "…"
        }
        return s + " " * (width - s.length())
    }
    
    private String padCenter(String s, int width) {
        if (s == null) return " " * width
        int padding = width - s.length()
        int leftPad = padding / 2
        int rightPad = padding - leftPad
        return " " * leftPad + s + " " * rightPad
    }
    
    private String truncate(String s, int maxLength) {
        if (s == null) return ""
        if (s.length() > maxLength) {
            return s.substring(0, maxLength) + "…"
        }
        return s
    }
}

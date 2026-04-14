package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.ReportFormat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject

import com.google.gson.*
import org.json.*;
import javax.xml.transform.*
import javax.xml.transform.stream.*

class RuleExecutor {

    List<RuleSet> rules
    Application application
    List<RuleViolation> results = []
    Integer ruleCount = 0

    RuleExecutor(Application application, List<RuleSet> rules) {
        this.rules = rules
        this.application = application
    }

    void executeRules() {
        rules.each { ruleSet ->
            ruleSet.getRules().each { // assigns current rule to 'it'
                results.addAll(it.execute(application))
                ruleCount++
            }
        }
    }

    static class SonarQubeReport {
        static class SonarQubeReportIssues {
            static class SonarQubeReportLocation {
                static class TextRange {
                    Integer startLine
                    Integer endLine
                    Integer startColumn
                    Integer endColumn
                }

                String message
                String filePath
                TextRange textRange;
            }

            String engineId
            String ruleId
            String severity
            String type
            SonarQubeReportLocation primaryLocation;

            SonarQubeReportIssues(violation) {
                ruleId = violation.rule.ruleId
                engineId = violation.rule.ruleName
                severity = violation.rule.severity
                type = violation.rule.ruleType
                this.primaryLocation = new SonarQubeReportLocation();
                this.primaryLocation.filePath = violation.fileName
                this.primaryLocation.message = violation.message
                if (violation.lineNumber > 0) {
                    this.primaryLocation.textRange = new SonarQubeReportLocation.TextRange();
                    this.primaryLocation.textRange.startLine = violation.lineNumber
                }
            }
        }

        List<SonarQubeReportIssues> issues

        SonarQubeReport() {
            this.issues = new ArrayList<>();
        }
    }

    /**
     * Legacy method for backward compatibility.
     * Delegates to new method with color enabled.
     */
    void displayResults(ReportFormat outputFormat, OutputStream outputStream) {
        displayResults(outputFormat, outputStream, true)
    }

    /**
     * Display results with optional color support for console output.
     */
    void displayResults(ReportFormat outputFormat, OutputStream outputStream, boolean useColor) {
        def format = outputFormat
        if (format == ReportFormat.JSON) {
            displayJsonResults(outputStream)
        } else if (format == ReportFormat.XML) {
            displayXmlResults(outputStream)
        } else {
            displayTableResults(outputStream, useColor)
        }
        outputStream.flush()
    }

    private void displayJsonResults(OutputStream outputStream) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SonarQubeReport sq = new SonarQubeReport();

        results.each { violation ->
            violation.setFileName(violation.getFileName() - (application.getApplicationPath().absolutePath + "/"))
            sq.getIssues().add(new SonarQubeReport.SonarQubeReportIssues(violation))
        }
        String prettyJsonString = gson.toJson(sq)
        outputStream.write(prettyJsonString.bytes)
    }

    private void displayXmlResults(OutputStream outputStream) {
        final StringBuilder builder = new StringBuilder();
        results.each { violation ->
            String json = new Gson().toJson(violation);
            JSONTokener jt = new JSONTokener(json);
            String xml = XML.toString(jt.nextValue(), "violation")
            builder.append(xml + "")
        }
        String concatenatedString = builder.toString();
        String xmlString = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n<" + 'violations' + ">" + concatenatedString + "</" + 'violations' + ">";
        String xmlOutput = convertToXML(xmlString);
        outputStream.write(xmlOutput.bytes)
    }

    private void displayTableResults(OutputStream out, boolean useColor) {
        // Group violations by severity
        def critical = results.findAll { it.rule.severity == RuleSeverity.CRITICAL }
        def major = results.findAll { it.rule.severity == RuleSeverity.MAJOR }
        def minor = results.findAll { it.rule.severity == RuleSeverity.MINOR }
        def blocker = results.findAll { it.rule.severity == RuleSeverity.BLOCKER }

        // Write header
        writeHeaderBox(out, ruleCount, critical.size(), major.size(), minor.size(), useColor)

        // Write each severity section
        if (blocker) writeSeveritySection(out, RuleSeverity.BLOCKER, blocker, useColor, "Failing")
        if (critical) writeSeveritySection(out, RuleSeverity.CRITICAL, critical, useColor, "Failing")
        if (major) writeSeveritySection(out, RuleSeverity.MAJOR, major, useColor, "Failing")
        if (minor) writeSeveritySection(out, RuleSeverity.MINOR, minor, useColor, "Non-failing")

        // Write summary footer
        int failingCount = blocker.size() + critical.size() + major.size()
        writeSummaryFooter(out, failingCount, minor.size(), useColor)
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

    private void writeSeveritySection(OutputStream out, RuleSeverity severity, List<RuleViolation> violations, boolean useColor, String label) {
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
            String location = v.fileName - (application.getApplicationPath().absolutePath + "/")
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

    String convertToXML(String xml) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 2);

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter stringWriter = new StringWriter();
        StreamResult xmlOutput = new StreamResult(stringWriter);

        Source xmlInput = new StreamSource(new StringReader(xml));
        transformer.transform(xmlInput, xmlOutput);
        return xmlOutput.getWriter().toString();
    }

    boolean hasErrors() {
        this.results.size() > 0
    }
}

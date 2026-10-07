package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.linter.model.ReportFormat
import com.google.gson.GsonBuilder
import org.json.JSONTokener
import org.json.XML
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import java.nio.charset.StandardCharsets

/** Independent writers. Caller owns the supplied stream; file adapters must close it. */
final class ReportWriters {
    static void write(AnalysisResult result, ReportFormat format, OutputStream stream) {
        String text
        switch (format) {
            case ReportFormat.JSON:
                def issues = result.findings.collect { finding ->
                    def location = [message: finding.message, filePath: relativePath(result, finding.fileName)]
                    if (finding.lineNumber > 0) location.textRange = [startLine: finding.lineNumber]
                    [engineId: finding.ruleName, ruleId: finding.ruleId, severity: finding.severity.name(),
                     type: finding.ruleType.name(), primaryLocation: location]
                }
                text = new GsonBuilder().setPrettyPrinting().create().toJson([issues: issues])
                break
            case ReportFormat.XML:
                def failed = result.findings.find { it.legacySerializationError != null }
                if (failed) throw new IOException('Failed to serialize XML report: ' + failed.legacySerializationError)
                String body = result.findings.collect { XML.toString(new JSONTokener(it.legacyJson).nextValue(), 'violation') }.join('')
                text = convertToXML('<?xml version="1.0" encoding="ISO-8859-15"?>\n<violations>' + body + '</violations>')
                break
            case ReportFormat.CONSOLE:
                def lines = []
                console(result, false) { String line, boolean warning -> lines.add(line) }
                text = lines.join('\n') + '\n'
                break
            default:
                throw new IllegalArgumentException("Unsupported report format: $format")
        }
        stream.write(text.getBytes(StandardCharsets.UTF_8))
        stream.flush()
    }

    /** Shared rendering with a Maven logging adapter, retaining its existing presentation. */
    static void console(AnalysisResult result, boolean maven, Closure sink) {
        String separator = '****************************************************************************'
        if (maven) {
            sink(separator, false)
            sink('AVIO Mule linter execution results', false)
        }
        sink("${result.ruleCount} rules executed.".toString(), false)
        sink(maven ? 'Rule validation results' : 'Rule Results', false)
        result.findings.each { finding ->
            String line = maven ?
                    " - [${finding.severity}] ${finding.ruleId} - ${finding.ruleName} : File:Linenumber: ${finding.fileName}:${finding.lineNumber > 0 ? finding.lineNumber : ''} ${finding.message}" :
                    "    [${finding.severity}] ${finding.ruleId} - ${finding.fileName} ${finding.lineNumber > 0 ? '( ' + finding.lineNumber + ' ) ' : ''}${finding.message} "
            sink(line, finding.severity != RuleSeverity.MINOR)
        }
        result.warnings.each { sink("Analysis warning: $it".toString(), true) }
        if (!result.complete) sink('Analysis incomplete.', true)
        sink(maven ? "Found a total of ${result.findings.size()} violations of ${result.ruleCount} rules.".toString() :
                "\nFound a total of ${result.findings.size()} violations.".toString(), false)
        if (maven) sink(separator, false)
    }

    private static String relativePath(AnalysisResult result, String fileName) {
        String prefix = result.applicationPath + File.separator
        fileName?.startsWith(prefix) ? fileName.substring(prefix.length()) : fileName
    }

    static String convertToXML(String xml) {
        def factory = TransformerFactory.newInstance()
        factory.setAttribute('indent-number', 2)
        def transformer = factory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, 'yes')
        def writer = new StringWriter()
        transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(writer))
        writer.toString()
    }
}

package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.ReportFormat

class RuleExecutor {

    List<RuleSet> rules
    Application application
    List<RuleViolation> results = []
    Integer ruleCount = 0
    AnalysisResult analysisResult

    RuleExecutor(Application application, List<RuleSet> rules) {
        this.rules = rules
        this.application = application
    }

    void executeRules() {
        results = []
        ruleCount = 0
        analysisResult = null
        rules.each { ruleSet ->
            ruleSet.getRules().each { // assigns current rule to 'it'
                results.addAll(it.execute(application))
                ruleCount++
            }
        }
        analysisResult = new AnalysisResult(results.collect { AnalysisFinding.snapshot(it) },
                application instanceof MuleApplication ? application.analysisWarnings : [],
                ruleCount, application.applicationPath.absolutePath)
    }
    /** Legacy report DTO retained for binary/source compatibility. */
    @Deprecated
    static class SonarQubeReport{

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
            SonarQubeReportIssues(violation){

                ruleId = violation.rule.ruleId
                engineId = violation.rule.ruleName
                severity = violation.rule.severity
                type = violation.rule.ruleType
                this.primaryLocation = new SonarQubeReportLocation();
                this.primaryLocation.filePath = violation.fileName
                this.primaryLocation.message = violation.message
                if (violation.lineNumber > 0) {
                    this.primaryLocation.textRange = new SonarQubeReportLocation.TextRange();
                    this.primaryLocation.textRange.startLine=violation.lineNumber
                }
            }


        }

        List<SonarQubeReportIssues> issues
        SonarQubeReport(){
            this.issues = new ArrayList<>();
        }

    }


    /** Compatibility adapter; new integrations can use ReportWriters directly. */
    void displayResults(ReportFormat outputFormat,OutputStream outputStream) {
        ReportWriters.write(getAnalysisResult(), outputFormat, outputStream)
    }

    AnalysisResult getAnalysisResult() {
        if (analysisResult == null) throw new IllegalStateException('Rules have not completed execution')
        analysisResult
    }

    String convertToXML(String xml){
        ReportWriters.convertToXML(xml)
    }

    boolean hasErrors(){
        this.results.size()>0
    }

}

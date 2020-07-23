package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.linter.model.Application

class RuleExecutor {

    List<RuleSet> rules
    Application application
    List<RuleViolation> results = []
    private Integer ruleCount = 0

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

    void displayResults(OutputStream outputStream) {
        outputStream.write("$ruleCount rules executed.\n".bytes)
        outputStream.write('Rule Results\n'.bytes)

        results.each { violation ->
            outputStream.write("    [$violation.rule.severity] $violation.rule.ruleId - $violation.fileName ".bytes)
            outputStream.write((violation.lineNumber > 0 ? "( $violation.lineNumber ) " : '').bytes)
            outputStream.write("$violation.message \n".bytes)
        }
        outputStream.write("\nFound a total of $results.size violations.\n".bytes)
        outputStream.flush()
        outputStream.close()
    }

}

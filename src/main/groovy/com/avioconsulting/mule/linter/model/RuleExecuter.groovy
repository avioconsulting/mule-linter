package com.avioconsulting.mule.linter.model

class RuleExecuter {

    RuleSet rules
    Application application
    List<RuleViolation> results = []

    RuleExecuter(Application application, RuleSet rules) {
        this.rules = rules
        this.application = application
    }

    void executeRules() {
        rules.getRules().each { // assigns current rule to 'it'
            results.addAll(it.execute(application))
        }
    }

    void displayResults(OutputStream outputStream) {
        outputStream.write("$rules.rules.size rules executed.\n".bytes)
        outputStream.write('Rule Results\n'.bytes)
        Integer count = 0
        results.each { violation ->
            outputStream.write("    $violation.rule.severity: $violation.fileName ".bytes)
            outputStream.write((violation.lineNumber > 0 ? "( $violation.lineNumber ) " : '').bytes)
            outputStream.write("$violation.message \n".bytes)
        }
        outputStream.write("\nFound a total of $count violations.\n".bytes)
        outputStream.flush()
        outputStream.close()
    }
}

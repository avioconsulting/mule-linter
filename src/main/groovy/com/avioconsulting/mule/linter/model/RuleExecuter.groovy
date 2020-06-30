package com.avioconsulting.mule.linter.model

class RuleExecuter {
    RuleSet rules
    Application application
    List<RuleViolation> results = new ArrayList<RuleViolation>()

    RuleExecuter(Application application, RuleSet rules) {
        this.rules = rules
        this.application = application
    }

    void executeRules(){
        rules.getRules().each { // assigns current rule to 'it'
            println("$it.ruleName executing.")
            results.addAll(it.execute(application))
        }
        println("All rules run.")
    }

    void displayResults(){
        println()
        println(rules.getRules().size() + " rules executed.")
        println("Rule Results")
        Integer count = 0
        results.each { violation ->
            println("    $violation.rule.severity: $violation.fileName " + (violation.lineNumber > 0 ? "( $violation.lineNumber ) " : "") + "$violation.message ")
        }

        println()
        println("Found a total of $count violations.")
    }
}

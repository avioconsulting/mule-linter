package com.avioconsulting.mule.linter.model

class RuleExecuter {
    List<Rule> rules = new ArrayList<Rule>()
    Map<Rule, List<RuleViolation>> results = new HashMap<Rule, RuleViolation>()

    RuleExecuter() {

    }

    void addRule(Rule r) {
        println("Adding new rule: $r")
        rules.add(r);
    }

    void executeRules(){
        rules.each { // assigns current rule to 'it'
            println("$it.ruleName executing.")
            results.put(it, it.execute())
        }
        println("All rules run.")
    }

    void displayResults(){
        println()
        println()
        println("Rule Results: ")
        Integer count = 0;
        results.each {
            Rule r = it.key
            List<RuleViolation> res = it.value
            count += res.size()
            if(res.size() > 0) {
                println("  $r.ruleName Violations:")

                it.value.each { violation ->
                    println("    $violation.severity $violation.fileName ( $violation.lineNumber ) Failed. $violation.message ")
                }
            } else {
                println("  $r.ruleName Successly passed.")
            }
            println()
            println("Found a total of $count violations.")
        }
    }
}

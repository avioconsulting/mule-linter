package com.avioconsulting.mule.linter.model

class RuleSet {

    List<Rule> rules = []

    RuleSet() {
    }

    void addRule(Rule rule) {
        rules.add(rule)
    }

    List<Rule> getRules() {
        return rules
    }

    void setRules(List<Rule> rules) {
        this.rules = rules
    }

}

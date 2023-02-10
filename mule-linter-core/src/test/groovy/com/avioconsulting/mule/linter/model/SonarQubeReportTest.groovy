package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.model.rule.RuleExecutor.SonarQubeReport
import spock.lang.Specification

class SonarQubeReportTest extends Specification {

    def 'check lineNumber reported correctly'() {
        given:
        SonarQubeReport sq = new SonarQubeReport();
        DummyRule dummyRule = new DummyRule();
        when:
        RuleViolation violationZeroLine = new RuleViolation(dummyRule, 'testPath', 0, 'testViolation')
        RuleViolation violationGreaterThanZeroLine = new RuleViolation(dummyRule, 'testPath', 11, 'testViolation')
        sq.getIssues().add(new SonarQubeReport.SonarQubeReportIssues(violationZeroLine))
        sq.getIssues().add(new SonarQubeReport.SonarQubeReportIssues(violationGreaterThanZeroLine))
        then:
        sq.getIssues()[0].primaryLocation.textRange == null 
        sq.getIssues()[1].primaryLocation.textRange != null
        sq.getIssues()[1].primaryLocation.textRange.startLine == 11
    }
}

class DummyRule extends Rule {
    static final String RULE_ID = 'DUMMY_RULE'
    static final String RULE_NAME = 'Dummy Test Rule.'
    @Override
    List<RuleViolation> execute(Application app){
        return null
    }
    DummyRule() {
        super(RULE_ID, RULE_NAME)
    }
}

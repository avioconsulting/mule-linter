package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class HttpsConfigExistsRule extends Rule {

    static final String RULE_ID = 'HTTPS_TLS'
    static final String RULE_NAME = 'All HTTPS connections have TLS properly configured. '
    static final String RULE_VIOLATION_MESSAGE = 'TLS configuration for HTTPS connection not configured.'
    static final String HTTP_IMPLEMENTED_MESSAGE = 'HTTP listener is unsecure.'

    HttpsConfigExistsRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.setSeverity(RuleSeverity.BLOCKER)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        app.configurationFiles.each {
            configFile ->
                List<MuleComponent> globalConfigs = configFile.findGlobalConfigs()
                if (globalConfigs.size() > 0) {
                    globalConfigs.each {
                        if(it.getChildren().size() > 0) {
                            it.getChildren().each {
                                if(it.componentNamespace.contains("/schema/mule/http")) {
                                    if(it.attributes.get("protocol") != null
                                            && it.attributes.get("protocol").equals("HTTPS")) {
                                        if(it.getChildren().size() > 0) {
                                            int count = 0
                                            it.getChildren().each {
                                                if(it.componentNamespace.contains("/schema/mule/tls")
                                                        && it.getChildren().size() > 0) {
                                                    it.getChildren().each {
                                                        if(it.componentName.equals("key-store")) {
                                                            count++
                                                        }
                                                    }
                                                }
                                            }
                                            if(count == 0) {
                                                violations.add(new RuleViolation(this, it.file.path,
                                                    it.lineNumber, RULE_VIOLATION_MESSAGE))
                                            }
                                        } else {
                                            violations.add(new RuleViolation(this, it.file.path,
                                                    it.lineNumber, RULE_VIOLATION_MESSAGE))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }

        return violations
    }

}

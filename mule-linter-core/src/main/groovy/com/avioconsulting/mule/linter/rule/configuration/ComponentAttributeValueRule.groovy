package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

import java.util.regex.Pattern

class ComponentAttributeValueRule extends Rule {

    static final String RULE_ID = 'COMPONENT_ATTRIBUTE_VALUE'
    static final String RULE_NAME = 'A specified component has the required attributes. '
    static final String RULE_VIOLATION_MESSAGE = 'Component is missing attribute '
    static final String EXTENDING = ' with value '

    @Param("component") String component
    @Param("namespace") String namespace
    @Param("requiredAttributes") List<String> requiredAttributes
    @Param("attributeMatchers") Map<String, String> attributeMatchers
    private Map<String, Pattern> privateAttributeMatchers

    ComponentAttributeValueRule(){
        super(RULE_ID, RULE_NAME)
    }

    ComponentAttributeValueRule(String component, String namespace, List<String> requiredAttributes) {
        this(RULE_ID, RULE_NAME, component, namespace, requiredAttributes)
    }

    ComponentAttributeValueRule(String ruleId, String ruleName, String component, String namespace, List<String> requiredAttributes) {
        super(ruleId, ruleName)
        this.component = component
        this.namespace = namespace
        this.requiredAttributes = requiredAttributes
    }

    ComponentAttributeValueRule(String component, String namespace, Map<String, Pattern> attributeMatchers) {
        this(RULE_ID, RULE_NAME, component, namespace, attributeMatchers)
    }

    ComponentAttributeValueRule(String ruleId, String ruleName, String component, String namespace, Map<String,Pattern> attributeMatchers) {
        super(ruleId, ruleName)
        this.component = component
        this.namespace = namespace
        this.privateAttributeMatchers = attributeMatchers
    }

    @Override
    void init(){
        if(attributeMatchers != null){
            privateAttributeMatchers = new HashMap<>()
            attributeMatchers.forEach((key,value)->{
                privateAttributeMatchers.put(key as String, Pattern.compile(value as String))
            })
        }
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []

        application.findComponents(this.component, namespace).each {component ->
            if (!(requiredAttributes != null && requiredAttributes.empty)) {
                requiredAttributes.each {attribute ->
                    if (!component.hasAttributeValue(attribute) || component.getAttributeValue(attribute).empty) {
                        violations.add(new RuleViolation(this, component.file.path,
                            component.lineNumber, RULE_VIOLATION_MESSAGE + attribute))
                    }
                }
            }
            if (!(privateAttributeMatchers != null && privateAttributeMatchers.isEmpty())) {
                privateAttributeMatchers.each {attribute ->
                    if (!component.hasAttributeValue(attribute.key) ||
                            component.getAttributeValue(attribute.key).empty) {
                        violations.add(new RuleViolation(this, component.file.path,
                                component.lineNumber, RULE_VIOLATION_MESSAGE + attribute.key))
                    } else {
                        if (!(component.getAttributeValue(attribute.key) ==~ attribute.value)) {
                            violations.add(new RuleViolation(this, component.file.path,
                                    component.lineNumber, RULE_VIOLATION_MESSAGE + attribute.key +
                                    EXTENDING + attribute.value.pattern()))
                        }
                    }
                }
            }
        }
        return violations
    }
}

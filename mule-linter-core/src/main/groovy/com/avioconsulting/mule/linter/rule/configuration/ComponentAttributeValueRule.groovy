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
    String component
    String namespace
    List<String> requiredAttributes
    Map<String, Pattern> attributeMatchers

    ComponentAttributeValueRule(String component, String namespace, List<String> requiredAttributes) {
        this(RULE_ID, RULE_NAME, component, namespace, requiredAttributes)
    }

    ComponentAttributeValueRule(String ruleId, String ruleName, String component, String namespace, List<String> requiredAttributes) {
        super(ruleId, ruleName)
        this.component = component
        this.namespace = namespace
        this.requiredAttributes = requiredAttributes
    }

    ComponentAttributeValueRule(@Param("component") String component, @Param("namespace") String namespace, @Param("attributeMatchers") Map<String, Pattern> attributeMatchers) {
        this(RULE_ID, RULE_NAME, component, namespace, attributeMatchers)
    }

    ComponentAttributeValueRule(String ruleId, String ruleName, String component, String namespace, Map<String,Pattern> attributeMatchers) {
        super(ruleId, ruleName)
        this.component = component
        this.namespace = namespace
        this.attributeMatchers = attributeMatchers
    }


    static ComponentAttributeValueRule createRule(Map<String, Object> params){
        String component = params.get("component") as String
        String namespace  = params.get("namespace") as String
        List<String> requiredAttributes = params.get("requiredAttributes") as List<String>
        Map attributeMatchers = params.get("attributeMatchers") as Map

        if(requiredAttributes != null)
            return new ComponentAttributeValueRule(component,namespace,requiredAttributes)
        else if(attributeMatchers != null){

            Map<String, Pattern> attributeMatchersParam = new HashMap<>()

            attributeMatchers.forEach((key,value)->{
                attributeMatchersParam.put(key as String, Pattern.compile(value as String))
            })

            return new ComponentAttributeValueRule(component,namespace,attributeMatchersParam)
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
            if (!(attributeMatchers != null && attributeMatchers.isEmpty())) {
                attributeMatchers.each {attribute ->
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

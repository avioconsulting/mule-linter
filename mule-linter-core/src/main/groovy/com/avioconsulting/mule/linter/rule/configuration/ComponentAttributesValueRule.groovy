package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

import java.util.regex.Pattern

/**
 * This rule checks that a component has a particular attribute, or a particular value on that attribute.
 * It exists as a customizable tool for a company to enforce some standard regarding a given component.
 */
class ComponentAttributesValueRule extends Rule {

    static final String RULE_ID = 'COMPONENT_REQUIRED_ATTRIBUTES'
    static final String RULE_NAME = 'A specified component has the required attributes. '
    static final String RULE_VIOLATION_MESSAGE = 'Component is missing attribute '
    static final String EXTENDING = ' with value '

    /** is the name of the mule component this rule should search for.
     * Examples include `"flow"` or `"request"`.
     */
    @Param("component") String component

    /**
     * is the namespace of the given mule component.
     * Examples include `"http://www.mulesoft.org/schema/mule/core"` or `"http://www.mulesoft.org/schema/mule/http"`.
     * The most common namespaces can be referenced from the class `com.avioconsulting.mule.linter.model.Namespace`.
     */
    @Param("namespace") String namespace

    /**
     * is a List of the attributes expected to be found on the component being checked by the rule.
     * An example for this list might be:
     * ['clientId','clientSecret']
     */
    @Param("requiredAttributes") List<String> requiredAttributes

    /**
     * is a map of attributes whose values are expected to match the provided patterns.
     * An example for this Map might be:
     * [
     *     'clientId':'~/\$\{org\.client\.id}/',
     *     'clientSecret':'~/\$\{org\.client\.secret}/'
     * ]
     */
    @Param("attributeMatchers") Map<String, String> attributeMatchers

    private Map<String, Pattern> privateAttributeMatchers

    ComponentAttributesValueRule(){
        super(RULE_ID, RULE_NAME)
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

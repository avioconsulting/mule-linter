package com.avioconsulting.mule.linter.model.configuration

import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class EmailComponent extends MuleComponent {

    final static String COMPONENT_NAMESPACE = Namespace.EMAIL
    final static String COMPONENT_NAME = 'email'
    private final MuleComponent comp

    EmailComponent(MuleComponent comp, File file) {
        super(COMPONENT_NAME, COMPONENT_NAMESPACE, comp.attributes, file)
        this.comp = comp
    }

    List getAllEmails() {
        List<String> emails = []
        if(comp.componentNamespace.contains("/schema/mule/email")) {
            comp.attributes.each {entry->
                if(entry.key.toLowerCase().contains("address")) {
                    emails.add(entry.value)
                }
            }
            comp.children.each {child->
                if(child.componentName.toLowerCase().contains("address")) {
                    child.children.each {
                        emails.add(it.attributes.get("value"))
                    }
                }
            }
        }
        return emails
    }



}

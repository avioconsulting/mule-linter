package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PropertyFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class EncryptedPasswordRule extends Rule {

    static final String RULE_ID = 'ENCRYPTED_VALUE'
    static final String RULE_NAME = 'The Property File contains ‘secret’ or ‘password’ values that are encrypted. '
    static final String RULE_VIOLATION_MESSAGE = 'The Property file contains a ‘secret’ or ‘password’ that is not encrypted: '
    static final String ENC_REGEX = '(\\!\\[.*?\\])'

    EncryptedPasswordRule() {
        super(RULE_ID, RULE_NAME)
    }

    static Boolean isEncrypted(String value) {
        return value.trim().matches(ENC_REGEX)
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []

        List<PropertyFile> propFiles = application.propertyFiles

        propFiles.each { file ->
            file.getProperties().each {
                String propName = it.key.toLowerCase()
                if ( propName.contains('password') || propName.contains('secret')) {
                    def propertyValue = it.value.toString()
                    // if the property references another property, that should not be a violation
                    if (!isEncrypted(propertyValue) && !propertyValue.startsWith('${secure::')) {
                        violations.add(new RuleViolation(this, file.getFile().absolutePath,
                                0, RULE_VIOLATION_MESSAGE + propName))
                    }
                }
            }
        }
        return violations
    }

}

package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.model.pom.MunitMavenPlugin
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.pom.PomFile

class MunitMavenPluginAttributesRule extends Rule {

    static final String RULE_ID = 'MUNIT_MAVEN_PLUGIN_ATTRIBUTES'
    static final String RULE_NAME = 'The Munit Maven plugins attribute contains required values. '
    static final String RULE_MESSAGE = 'Munit Maven plugin has incorrect or missing configuration coverage value '
    static final String RULE_MESSAGE_MISSING = 'Munit Maven plugin is missing element '
    static final Map<String, String> COVERAGE_DEFAULTS = ['runCoverage':'true',
                                                          'failBuild':'true',
                                                          'requiredApplicationCoverage':'80',
                                                          'requiredResourceCoverage':'80',
                                                          'requiredFlowCoverage':'80']
    private static final String IGNORE_FILES = 'ignoreFiles'
    Map<String, String> coverageAttributeMap
    List<String> ignoreFiles = []

    MunitMavenPluginAttributesRule() {
        this([:], true, [])
    }

    MunitMavenPluginAttributesRule(List<String> ignoreFiles) {
        this([:], true, ignoreFiles)
    }

    MunitMavenPluginAttributesRule(Map<String,String> coverageAttributeMap,
                                   Boolean includeDefaults) {
        this(coverageAttributeMap, includeDefaults, [])
    }

    MunitMavenPluginAttributesRule(Map<String,String> coverageAttributeMap,
                                   Boolean includeDefaults, List<String> ignoreFiles) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.coverageAttributeMap = includeDefaults ? COVERAGE_DEFAULTS + coverageAttributeMap : coverageAttributeMap
        this.ignoreFiles = ignoreFiles
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        MunitMavenPlugin plugin = application.pomFile.munitPlugin
        if (plugin != null) {
            coverageAttributeMap.each { key, val ->
                PomElement pe = plugin.getConfigProperty(key)
                if (pe?.value != val) {
                    violations.add(new RuleViolation(this, PomFile.POM_XML,
                            pe == null ? plugin.lineNo : pe.lineNo, RULE_MESSAGE + key + '|' + val))
                }
            }
            if (ignoreFiles.size() > 0) {
                List<String> diff = ignoreFiles - plugin.getIgnoreFiles()
                diff.each {
                    violations.add(new RuleViolation(this, PomFile.POM_XML,
                            plugin.getIgnoreFilesLineNo(), RULE_MESSAGE_MISSING + 'ignoreFile|' + it ))
                }
            }
        } else {
            violations.add(new RuleViolation(this, PomFile.POM_XML, 0, 'Missing munit-maven-plugin'))
        }
        return violations
    }

}

package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.model.pom.MunitMavenPlugin
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.pom.PomFile

/**
 * This rule ensures that the `munit-maven-plugin` exists and is configured correctly.
 * AVIO reccomends that Munit should be used, passing tests should be required, and that an arbitrary amount of code should be covered by Munit tests.
 * Remember that test coverage does not ensure test quality.
 */
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

    /**
     * coverageAttributeMap: is a map of attributes expected to be present with given values on the `munit-maven-plugin` in the `pom.xml`.
     * By default, the expected attributes and values are:
     * ['runCoverage':'true',
     * 'failBuild':'true',
     * 'requiredApplicationCoverage':'80',
     * 'requiredResourceCoverage':'80',
     * 'requiredFlowCoverage':'80']
     */
    @Param("coverageAttributeMap") Map<String, String> coverageAttributeMap

    /**
     * ignoreFiles: is a list of test files that the `munit-maven-plugin should` ignore.
     * By default, AVIO does not expect to ignore test suites.
     * Ignored test suites is a code smell, and should be removed before committing/merging code.
     */
    @Param("ignoreFiles") List<String> ignoreFiles

    /**
     * includeDefaults: is a flag to include AVIO's default set of attributes in addition to whatever is provided by the *coverageAttributeMap*.
     * The default map is:
     * ['runCoverage':'true',
     * 'failBuild':'true',
     * 'requiredApplicationCoverage':'80',
     * 'requiredResourceCoverage':'80',
     * 'requiredFlowCoverage':'80']
     */
    @Param("includeDefaults") Boolean includeDefaults

    MunitMavenPluginAttributesRule() {
        super(RULE_ID, RULE_NAME)
        this.coverageAttributeMap = [:]
        this.ignoreFiles = []
        this.includeDefaults = true
    }

    @Override
    void init(){
        if(includeDefaults){
            this.coverageAttributeMap = COVERAGE_DEFAULTS + coverageAttributeMap
        }
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

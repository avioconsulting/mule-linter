package com.avioconsulting.mule.linter.catalog

import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
import com.avioconsulting.mule.linter.spi.RuleProvider
import com.avioconsulting.mule.linter.rule.FileExistsRule
import com.avioconsulting.mule.linter.rule.cicd.AzurePipelinesExistsRule
import com.avioconsulting.mule.linter.rule.cicd.GitlabFileExistsRule
import com.avioconsulting.mule.linter.rule.cicd.JenkinsFileExistsRule
import com.avioconsulting.mule.linter.rule.configuration.ApiConsoleDisabledRule
import com.avioconsulting.mule.linter.rule.configuration.AutoDiscoveryRule
import com.avioconsulting.mule.linter.rule.configuration.CommentedCodeRule
import com.avioconsulting.mule.linter.rule.configuration.ComponentAttributesValueRule
import com.avioconsulting.mule.linter.rule.configuration.ComponentCountRule
import com.avioconsulting.mule.linter.rule.configuration.ConfigFileNamingRule
import com.avioconsulting.mule.linter.rule.configuration.ConfigPlaceholderRule
import com.avioconsulting.mule.linter.rule.configuration.ConnectionRetryRule
import com.avioconsulting.mule.linter.rule.configuration.ConnectionTimeoutRule
import com.avioconsulting.mule.linter.rule.configuration.ConsecutiveLoggersCountRule
import com.avioconsulting.mule.linter.rule.configuration.CronExpressionExternalizedRule
import com.avioconsulting.mule.linter.rule.configuration.DisplayNameRule
import com.avioconsulting.mule.linter.rule.configuration.FlowErrorHandlerRule
import com.avioconsulting.mule.linter.rule.configuration.FlowSubflowComponentCountRule
import com.avioconsulting.mule.linter.rule.configuration.FlowSubflowNamingRule
import com.avioconsulting.mule.linter.rule.configuration.GlobalConfigExistsRule
import com.avioconsulting.mule.linter.rule.configuration.GlobalConfigNoFlowsRule
import com.avioconsulting.mule.linter.rule.configuration.GlobalConfigSeparationRule
import com.avioconsulting.mule.linter.rule.configuration.GlobalFilesNoFlowsRule
import com.avioconsulting.mule.linter.rule.configuration.LoggerAttributesRule
import com.avioconsulting.mule.linter.rule.configuration.LoggerCategoryExistsRule
import com.avioconsulting.mule.linter.rule.configuration.LoggerMessageContentsRule
import com.avioconsulting.mule.linter.rule.configuration.LoggerMessageExistsRule
import com.avioconsulting.mule.linter.rule.configuration.MuleConfigFlowLimitRule
import com.avioconsulting.mule.linter.rule.configuration.OnErrorLogExceptionRule
import com.avioconsulting.mule.linter.rule.configuration.UntilSuccessfulRule
import com.avioconsulting.mule.linter.rule.configuration.UnusedFlowRule
import com.avioconsulting.mule.linter.rule.git.GitIgnoreRule
import com.avioconsulting.mule.linter.rule.muleartifact.MuleArtifactHasSecurePropertiesRule
import com.avioconsulting.mule.linter.rule.muleartifact.MuleArtifactMinMuleVersionRule
import com.avioconsulting.mule.linter.rule.pom.ApikitVersionRule
import com.avioconsulting.mule.linter.rule.pom.MuleMavenPluginVersionRule
import com.avioconsulting.mule.linter.rule.pom.MuleRuntimeVersionRule
import com.avioconsulting.mule.linter.rule.pom.MunitMavenPluginAttributesRule
import com.avioconsulting.mule.linter.rule.pom.MunitPluginVersionRule
import com.avioconsulting.mule.linter.rule.pom.MunitVersionRule
import com.avioconsulting.mule.linter.rule.pom.PomDependencyVersionRule
import com.avioconsulting.mule.linter.rule.pom.PomExistsRule
import com.avioconsulting.mule.linter.rule.pom.PomPluginAttributeRule
import com.avioconsulting.mule.linter.rule.pom.PomPropertyValueRule
import com.avioconsulting.mule.linter.rule.property.EncryptedPasswordRule
import com.avioconsulting.mule.linter.rule.property.HostnamePropertyRule
import com.avioconsulting.mule.linter.rule.property.PropertyExistsRule
import com.avioconsulting.mule.linter.rule.property.PropertyFileNamingRule
import com.avioconsulting.mule.linter.rule.property.PropertyFilePropertyCountRule
import com.avioconsulting.mule.linter.rule.property.PropertyNamePatternRule
import com.avioconsulting.mule.linter.rule.readme.ReadmeRule

import static com.avioconsulting.mule.linter.catalog.OptionDefinition.*

/** Complete built-in contract. Legacy constructors remain usable, but catalog defaults win. */
class BuiltinRuleProvider implements RuleProvider {
    private static final List<String> CASES = ['CAMEL_CASE', 'PASCAL_CASE', 'SNAKE_CASE', 'KEBAB_CASE']
    private static final List<String> LEVELS = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR']
    private static final String PROPERTY_PATTERN = '${appname}-${env}.properties'

    @Override
    Collection<RuleDefinition> getRuleDefinitions() {
        [
            define(FileExistsRule, 'file-exists', 'Require a file relative to the application root.', [path().required()]),
            define(AzurePipelinesExistsRule, 'azure-pipelines-exists', 'Require a valid Azure Pipelines YAML file.', [path().defaultValue('azure-pipelines.yml')]),
            define(GitlabFileExistsRule, 'gitlab-exists', 'Require the GitLab CI configuration file.', [path().defaultValue('.gitlab-ci.yml')]),
            define(JenkinsFileExistsRule, 'jenkins-exists', 'Require a Jenkinsfile.', [path().defaultValue('Jenkinsfile')]),
            define(ApiConsoleDisabledRule, 'api-console-disabled', 'Require API Console flows to be removed or stopped.'),
            define(AutoDiscoveryRule, 'auto-discovery-exists', 'Require API Autodiscovery and externalized environment-specific API IDs.', [
                bool('enabled').description('Enable this check.').defaultValue(true),
                strings('exemptedFlows', 'HTTP listener flow names exempt from Autodiscovery.').defaultValue([]),
                environments(['dev', 'test', 'prod']), pattern()], RuleSeverity.BLOCKER, RuleType.VULNERABILITY),
            define(CommentedCodeRule, 'commented-code', 'Find XML code left inside comments.'),
            define(ComponentAttributesValueRule, 'component-required-attributes', 'Require attributes and matching values on selected components.', componentIdentity() + [
                strings('requiredAttributes', 'Attribute names that must have values.').nullable().defaultValue(null),
                stringMap('attributeMatchers').description('Attribute names mapped to regular expressions.').nullable().defaultValue(null)]),
            define(ComponentCountRule, 'component-count', 'Limit occurrences of a selected component.', componentIdentity() + [count('maxCount').required()]),
            define(ConfigFileNamingRule, 'config-file-naming', 'Require a naming convention for Mule XML files.', [format()]),
            define(ConfigPlaceholderRule, 'config-placeholder', 'Require property placeholders in global configuration attributes.', [
                strings('placeholderAttributes', 'Attributes whose values must be externalized.').defaultValue([
                    'key', 'password', 'keyPassword', 'username', 'host', 'clientId', 'clientSecret', 'tokenUrl',
                    'domain', 'workstation', 'authDn', 'authPassword', 'authentication', 'url', 'localCallbackUrl',
                    'externalCallbackUrl', 'localAuthorizationUrlResourceOwnerId', 'localAuthorizationUrl', 'authorizationUrl', 'passphrase'])]),
            define(ConnectionRetryRule, 'connection-retry-config', 'Require connector reconnection frequency and count.', [connectorOptions(false)], RuleSeverity.MAJOR),
            define(ConnectionTimeoutRule, 'connection-timeout-config', 'Require explicit connector timeouts.', [connectorOptions(true)], RuleSeverity.MAJOR),
            define(ConsecutiveLoggersCountRule, 'consecutive-loggers-count', 'Limit consecutive loggers at the same level.', [
                union('excessiveLoggers', integer('count').minimum(0), map('levels', integer('count').minimum(0)).choices(LEVELS))
                    .description('An integer limit for all levels, or a map of level names to limits.').defaultValue(2)]),
            define(CronExpressionExternalizedRule, 'cron-expression-externalized', 'Require scheduler cron expressions to use property placeholders.', [], RuleSeverity.MAJOR),
            define(DisplayNameRule, 'component-display-name', 'Reject default display names on selected components.', [
                list('components', object('component', [string('name').required(), string('namespace').required(), string('displayName').required()]))
                    .description('Component selectors and their default display names.').defaultValue([
                        [name: 'set-payload', namespace: Namespace.CORE, displayName: 'Set Payload'],
                        [name: 'set-variable', namespace: Namespace.CORE, displayName: 'Set Variable'],
                        [name: 'transform', namespace: Namespace.CORE_EE, displayName: 'Transform Message'],
                        [name: 'flow-ref', namespace: Namespace.CORE, displayName: 'Flow Reference']])]),
            define(FlowErrorHandlerRule, 'flow-error-handler', 'Require flow-level error handling when no default global handler exists.'),
            define(FlowSubflowComponentCountRule, 'flow-subflow-component-count', 'Limit components in each flow and subflow.', [count('maxCount').defaultValue(20)], RuleSeverity.MINOR),
            define(FlowSubflowNamingRule, 'flow-subflow-naming', 'Require a naming convention for flows and subflows.', [format()]),
            define(GlobalConfigExistsRule, 'global-config-exists', 'Require global configuration in the designated file.', [
                stringMap('noneGlobalElements').description('Non-global element names mapped to namespaces.').defaultValue([:]), globalFile()]),
            define(GlobalConfigNoFlowsRule, 'global-config-no-flows', 'Require the designated global file to contain no flows or subflows.', [globalFile()]),
            define(GlobalConfigSeparationRule, 'global-config-separation', 'Keep global configuration separate from flow processing.', [exceptions()]),
            define(GlobalFilesNoFlowsRule, 'global-files-no-flows', 'Keep flows out of files matching global configuration patterns.', [
                strings('patterns', 'Full-match regular expressions against paths relative to src/main/mule.')
                    .defaultValue(['global/.*\\.xml', 'global\\.xml', 'global-config\\.xml']), exceptions()]),
            define(LoggerAttributesRule, 'logger-required-attributes', 'Require selected attributes on every logger.', [loggerAttributes([])]),
            define(LoggerCategoryExistsRule, 'logger-category-has-value', 'Require a populated category on every logger.', [loggerAttributes(['category'])]),
            define(LoggerMessageExistsRule, 'logger-message-has-value', 'Require a populated message on every logger.', [loggerAttributes(['message'])]),
            define(LoggerMessageContentsRule, 'logger-message-contents', 'Reject logger messages matching prohibited regular expressions.', [
                string('pattern').description('Prohibited INFO message regex.').nullable().defaultValue('payload]'),
                stringMap('rules').description('Logging levels mapped to prohibited message regexes; overrides pattern.').choices(LEVELS).nullable().defaultValue(null)]),
            define(MuleConfigFlowLimitRule, 'mule-config-flow-limit', 'Limit flows and subflows per XML file.', [count('flowLimit').defaultValue(20)]),
            define(OnErrorLogExceptionRule, 'on-error-log-exception', 'Require exception logging at flow and shared error-handler boundaries.', [
                bool('includeTryScopes').description('Also check inline Try-scope handlers.').defaultValue(false),
                list('exceptions', object('exception', [string('file').required(), string('handler').required(),
                    stringList('errorTypes').required(), string('reason').required()]))
                    .description('Exact relative files, named handlers, explicit error types, and documented reasons.').defaultValue([])]),
            define(UntilSuccessfulRule, 'until-successful', 'Avoid until-successful retry scopes.', [
                string('component').description('Component local name.').defaultValue('until-successful'),
                string('namespace').description('Component namespace URI.').defaultValue(Namespace.CORE), count('maxCount').defaultValue(0)]),
            define(UnusedFlowRule, 'unused-flow', 'Find flows and subflows with no source or references.'),
            define(GitIgnoreRule, 'git-ignore', 'Require a .gitignore containing expected expressions.', [path().defaultValue('.gitignore'),
                strings('ignoredFiles', 'Required ignore expressions.').defaultValue(['*.jar', '*.class', 'target/', '.project', '.classpath', '.idea', 'build'])]),
            define(MuleArtifactHasSecurePropertiesRule, 'mule-artifact-secure-properties', 'Require sensitive properties in mule-artifact.json secureProperties.', [
                strings('properties', 'Additional property names that must be secured.').defaultValue([]), includeDefaults()]),
            define(MuleArtifactMinMuleVersionRule, 'mule-artifact-min-mule-version', 'Require minMuleVersion not to exceed the app.runtime POM property.'),
            define(ApikitVersionRule, 'apikit-version', 'Require an APIKit dependency newer than the configured version.', dependencyOptions('org.mule.modules', 'mule-apikit-module', '1.9.0', 'GREATER_THAN')),
            define(MuleMavenPluginVersionRule, 'mule-maven-plugin', 'Require the configured Mule Maven plugin version.', pluginOptions('org.mule.tools.maven', 'mule-maven-plugin') + [version()]),
            define(MuleRuntimeVersionRule, 'mule-runtime', 'Require app.runtime to match the configured version.', propertyOptions('app.runtime') + [version()]),
            define(MunitMavenPluginAttributesRule, 'munit-maven-plugin-attributes', 'Require MUnit coverage configuration and expected ignored files.', [
                stringMap('coverageAttributeMap').description('Additional required coverage settings.').defaultValue([:]),
                strings('ignoreFiles', 'Expected MUnit ignored test files.').defaultValue([]), includeDefaults()]),
            define(MunitPluginVersionRule, 'munit-plugin-version', 'Require the configured MUnit Maven plugin version.', pluginOptions('com.mulesoft.munit.tools', 'munit-maven-plugin') + [version()]),
            define(MunitVersionRule, 'munit-version', 'Require munit.version to match the configured version.', propertyOptions('munit.version') + [version()]),
            define(PomDependencyVersionRule, 'pom-dependency-version', 'Require a dependency matching version criteria.', dependencyOptions()),
            define(PomExistsRule, 'pom-file-exists', 'Require pom.xml at the application root.', [path().defaultValue('pom.xml')]),
            define(PomPluginAttributeRule, 'pom-plugin-attribute', 'Require a Maven plugin with matching attributes.', pluginOptions()),
            define(PomPropertyValueRule, 'maven-property', 'Require a POM property with the configured value.', [
                string('propertyName').description('POM property name.').required(), string('propertyValue').description('Expected POM property value.').required()]),
            define(EncryptedPasswordRule, 'encrypted-value', 'Require secret and password property values to be encrypted.'),
            define(HostnamePropertyRule, 'hostname-property', 'Require host properties to use names rather than IP addresses.', [
                strings('exemptions', 'Property names exempt from this check.').defaultValue([]),
                strings('fileExemptions', 'Exact property-file basenames exempt from this check.').defaultValue(['local.properties', 'unit.properties'])]),
            define(PropertyExistsRule, 'property-exists', 'Require a property in every selected environment file.', [
                string('propertyName').description('Property name to require.').required(), environments(['dev', 'test', 'prod']), pattern()]),
            define(PropertyFileNamingRule, 'property-file-naming', 'Require property files for selected environments.', [environments([]), pattern()]),
            define(PropertyFilePropertyCountRule, 'property-file-count-mismatch', 'Require matching property counts across environment files.', [environments([]), pattern()]),
            define(PropertyNamePatternRule, 'property-name-pattern', 'Require a naming convention for property keys.', [
                string('format').description('Property key naming convention.').choices(['CAMEL_CASE', 'PASCAL_CASE', 'JAVA_PROPERTIES_CASE']).defaultValue('JAVA_PROPERTIES_CASE')], RuleSeverity.MINOR),
            define(ReadmeRule, 'readme', 'Require a nonempty README.md.', [path().defaultValue('README.md')])
        ]
    }

    private static RuleDefinition define(Class<? extends Rule> type, String id, String description,
                                         List<OptionDefinition> options = [], RuleSeverity severity = RuleSeverity.CRITICAL,
                                         RuleType classification = RuleType.CODE_SMELL) {
        new RuleDefinition(id, [type.RULE_ID], type.RULE_ID, description, severity, classification, type, options, { Map values ->
            Rule rule = type.newInstance()
            values.each { name, value -> rule.setProperty(name, value) }
            rule
        })
    }

    private static OptionDefinition strings(String name, String description) { stringList(name).description(description) }
    private static OptionDefinition path() { string('path').description('File path relative to the application root.') }
    private static OptionDefinition count(String name) { integer(name).description('Maximum allowed count.').minimum(0) }
    private static OptionDefinition format() { string('format').description('Naming convention.').choices(CASES).defaultValue('KEBAB_CASE') }
    private static OptionDefinition globalFile() { string('globalFileName').description('Global configuration filename.').defaultValue('globals.xml') }
    private static OptionDefinition exceptions() { strings('exceptions', 'Exact exception paths relative to src/main/mule.').defaultValue([]) }
    private static OptionDefinition environments(List defaults) { strings('environments', 'Deployment environments to check.').defaultValue(defaults) }
    private static OptionDefinition pattern() { string('pattern').description('Filename template with appname and env bindings.').defaultValue(PROPERTY_PATTERN) }
    private static OptionDefinition includeDefaults() { bool('includeDefaults').description('Include the built-in policy values in addition to configured values.').defaultValue(true) }
    private static OptionDefinition loggerAttributes(List defaults) { strings('requiredAttributes', 'Logger attributes that must have values.').defaultValue(defaults) }
    private static OptionDefinition version() { string('version').description('Expected version string.').required() }
    private static List<OptionDefinition> componentIdentity() {
        [string('component').description('Component local name.').required(), string('namespace').description('Component namespace URI.').required()]
    }
    private static OptionDefinition connectorOptions(boolean timeout) {
        List fields = [string('name').required(), string('namespace').required(), string('config-ref').defaultValue('request-config')]
        Map defaults = [name: 'request', namespace: Namespace.HTTP, 'config-ref': 'request-config']
        if (timeout) {
            fields.add(string('timeoutAttribute').defaultValue('responseTimeout'))
            defaults.timeoutAttribute = 'responseTimeout'
        }
        list('components', object('component', fields)).description('Connector selectors and configuration references.').defaultValue([defaults])
    }
    private static List<OptionDefinition> dependencyOptions(String group = null, String artifact = null, String version = null, String operator = 'EQUAL') {
        [group ? string('groupId').description('Dependency group ID.').defaultValue(group) : string('groupId').description('Dependency group ID.').required(),
         artifact ? string('artifactId').description('Dependency artifact ID.').defaultValue(artifact) : string('artifactId').description('Dependency artifact ID.').required(),
         version ? string('artifactVersion').description('Version to compare.').defaultValue(version) : string('artifactVersion').description('Version to compare.').required(),
         string('versionOperator').description('Version comparison operator.').choices(['EQUAL', 'GREATER_THAN']).defaultValue(operator)]
    }
    private static List<OptionDefinition> pluginOptions(String group = null, String artifact = null) {
        [group ? string('groupId').description('Plugin group ID.').defaultValue(group) : string('groupId').description('Plugin group ID.').required(),
         artifact ? string('artifactId').description('Plugin artifact ID.').defaultValue(artifact) : string('artifactId').description('Plugin artifact ID.').required(),
         map('attributes', union('value', string('text'), bool('flag'), integer('number'))).description('Expected plugin attributes.').nullable().defaultValue(null)]
    }
    private static List<OptionDefinition> propertyOptions(String property) {
        [string('propertyName').description('POM property name.').defaultValue(property),
         string('propertyValue').description('Expected value; version takes precedence for this rule.').nullable().defaultValue(null)]
    }
}

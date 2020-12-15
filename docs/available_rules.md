# Available Rules

## CI/CD

### JENKINS_EXISTS

This rule checks that a Jenkinsfile exists at the root of the project. 

The constructor for this rule is:

```groovy
JenkinsFileExistsRule()
```

This rule takes no arguments. 


## Configuration

### API_CONSOLE_DISABLED

This rule checks that API Console has been removed or disabled (flow has attribute `initialState="stopped"`). 
API console has been the source of security vulnerabilities in the past, and is unnecessary outside of a local development environment thanks to Anypoint Exchange. 
AVIO recommends that deployments outside of local testing should not enable it. 

The constructor for this rule is: 

```groovy
ApiConsoleDisabledRule()
```

This rule takes no arguments. 

### COMMENTED_CODE

This rule checks that no code is found within comments inside mule configuration files. 
Commented out code should be removed or fixed prior to deployment. 

The constructor for this rule is: 

```groovy
CommentedCodeRule()
```

This rule takes no arguments. 

### COMPONENT_ATTRIBUTE_VALUE

This rule checks that a component has a particular attribute, or a particular value on that attribute. 
It exists as a customizable tool for a company to enforce some standard regarding a given component. 

The basic constructors for this rule are: 

```groovy
ComponentAttributeValueRule(String component, String namespace, List<String> requiredAttributes)
ComponentAttributeValueRule(String component, String namespace, Map<String, Pattern> attributeMatchers)

ComponentAttributeValueRule(String ruleId, String ruleName, 
	String component, String namespace, List<String> requiredAttributes)
ComponentAttributeValueRule(String ruleId, String ruleName, 
	String component, String namespace, Map<String, Pattern> attributeMatchers)
```

*component* is the name of the mule component this rule should search for. 
Examples include `"flow"` or `"request"`. 

*namespace* is the namespace of the given mule component. 
Examples include `"http://www.mulesoft.org/schema/mule/core"` or `"http://www.mulesoft.org/schema/mule/http"`. 
The most common namespaces can be referenced from the class `com.avioconsulting.mule.linter.model.Namespace`. 

*requiredAttributes* is a List of the attributes expected to be found on the component being checked by the rule. 
An example for this list might be: 
```groovy
['clientId','clientSecret']
```

*attributeMatchers* is a map of attributes whose values are expected to match the provided patterns. 
An example for this Map might be: 
```groovy
[clientId:~/\$\{org\.client\.id}/,
clientSecret:~/\$\{org\.client\.secret}/]
```

The COMPONENT_ATTRIBUTE_VALUE rule also allows for a custom ruleId and ruleName. 

*ruleId* defaults to a String with value `"COMPONENT_ATTRIBUTE_VALUE"`. 

*ruleName* defaults to a String with value `"Component Attribute Required Rule"`. 

### COMPONENT_COUNT

This rule checks that a given component has not been used more than a specified number of times. 
It exists as a customizable tool for a company to enforce a limit on the usage of a given component. 

The basic constructor for this rule is: 

```groovy
ComponentCountRule(String component, String namespace, Integer count)

ComponentCountRule(String ruleId, String ruleName, String component, 
	String namespace, Integer count)
```

*component* is the name of the mule component this rule should search for. 
Examples include `"until-successful"` or `"job"`. 

*namespace* is the namespace of the given mule component. 
Examples include `"http://www.mulesoft.org/schema/mule/core"` or `"http://www.mulesoft.org/schema/mule/batch"`. 
The most common namespaces can be referenced from the class `com.avioconsulting.mule.linter.model.Namespace`. 

The COMPONENT_COUNT rule also allows for a custom ruleId and ruleName. 

*ruleId* defaults to a String with value `"COMPONENT_COUNT"`. 

*ruleName* defaults to a String with value `"Component count rule"`. 

### CONFIG_FILE_NAMING

This rule checks that a config file matches a given case format. 
Files, properties, and certain name attributes should generally follow a self consistent case pattern for readability and predictability. 
At AVIO, we generally expect Mule Configs to follow `kebab-case`. 

The constructors for this rule are: 

```groovy
ConfigFileNamingRule()
ConfigFileNamingRule(CaseNaming.CaseFormat format)
```

*format* is an Enum `CaseFormat` provided by the class `com.avioconsulting.mule.linter.model.CaseNaming`. 
This argument is optional, and the default `CaseFormat` used by this rule is `CaseFormat.KEBAB_CASE`. 
Current options are `CAMEL_CASE`, `PASCAL_CASE`, `SNAKE_CASE`, or `KEBAB_CASE`. 

### CONFIG_PLACEHOLDER

This rule checks that certain common config elements use String interpolation placeholcers (`${value}`) to point to provided properties rather than be specified statically in line. 
Referring to properties with placeholders makes it easier to specify values by environment, encrypt secrets that should not be plain text values, or provide common values through templates or archetypes. 
This rule only considers global configuration elements, and does not look at values provided within flows. 
The default list covers the most common attributes to provide placeholders for, but is not exhaustive. You may provide your own list if necessary. 

The constructors for this rule are:

```groovy
ConfigPlaceholderRule()
ConfigPlaceholderRule(String[] placeholderAttributes)
```

*placeholderAttributes* are the component attributes that the rule should require to be placeholders. 
This argument is optional. The default array is as follows: 
```groovy
['key', 'password', 'keyPassword', 'username', 'host', 'clientId', 'clientSecret',
'tokenUrl', 'domain', 'workstation', 'authDn', 'authPassword', 'authentication',
'url', 'localCallbackUrl', 'externalCallbackUrl',
'localAuthorizationUrlResourceOwnerId', 'localAuthorizationUrl',
'authorizationUrl', 'passphrase']
```

### DISPLAY_NAME

This rule ensures that developers are taking full advantage of display name when considering certain components in Anypoint Studio. 
"Set Variable" does not tell a developer what a component does, and requires a developer to switch to XML view or click on each component to know anything about what a component is doing. 
The default list covers the most egregious examples of vague naming. You may provide your own list if you require more (or less) components to have names. 

The constructors for this rule are: 

```groovy
DisplayNameRule()
DisplayNameRule(List components)
```

*components* is a List of Maps containing `name`, `namespace`, and `displayName`. 
The most common namespaces can be referenced from the class `com.avioconsulting.mule.linter.model.Namespace`. 
This argument is optional. The default list is as follows: 
```groovy
[[name: 'set-payload', namespace: Namespace.CORE, displayName: 'Set Payload'],
[name: 'set-variable', namespace: Namespace.CORE, displayName: 'Set Variable'],
[name: 'transform', namespace: Namespace.CORE_EE, displayName: 'Transform Message'],
[name: 'flow-ref', namespace: Namespace.CORE, displayName: 'Flow Reference']]
```

### EXCESSIVE_LOGGERS

This rule checks for multiple Logger components of the same level being used back to back. 
In general, multiple logging statements back to back at the same level are redundant and make code more difficult to read. 
If you disagree with our standards, you can provide a custom limit broken down by logging level. 

The constructors for this rule are: 

```groovy
ExcessiveLoggersRule()
ExcessiveLoggersRule(Integer excessiveLoggers)
ExcessiveLoggersRule(EnumMap<LoggerComponent.LogLevel, Integer>  excessiveLoggers)
```

*excessiveLoggers* is an optional variable representing the number of sequential loggers of the same level required to fail the rule. 
The value can be an integer or an EnumMap, where the Enum is `LogLevel` found within the class `com.avioconsulting.mule.linter.model.configuration.LoggerComponent`. 
The default is the integer value `2`, or the equivalent EnumMap: 
```groovy
EnumMap<LoggerComponent.LogLevel, Integer> excessiveLoggers = 
	[(LoggerComponent.LogLevel.TRACE): 2,
	(LoggerComponent.LogLevel.DEBUG): 2,
	(LoggerComponent.LogLevel.INFO): 2,
	(LoggerComponent.LogLevel.WARN): 2,
	(LoggerComponent.LogLevel.ERROR): 2]
```

### FLOW_SUBFLOW_NAMING

This rule ensures that `flow` and `sub-flow` names follow a given case format. 
Files, properties, and certain name attributes should follow a self consistent case pattern for readability and predictability. 
Here, we require the user to specify a particular case convention. 

The constructor for the rule is: 

```groovy
FlowSubflowNamingRule(CaseNaming.CaseFormat format)
```

*format* is an Enum `CaseFormat` provided by the class `com.avioconsulting.mule.linter.model.CaseNaming`. 
Current options are `CAMEL_CASE`, `PASCAL_CASE`, `SNAKE_CASE`, or `KEBAB_CASE`. 

### GLOBAL_CONFIG_NO_FLOWS

This rule checks that a global mule configuration file does not contain flows. 
General Mule convention holds that Configuration and Property components used by the Mule API as a whole should be contained within a file separate from the flows that use these properties or configuration components. 
AVIO expects this file to be called `globals.xml` by default. 

The constructors for this rule are:

```groovy
GlobalConfigNoFlowsRule()
GlobalConfigNoFlowsRule(String globalFileName)
```

*globalFileName* is a string representing the name of the file which should contain only global configuration elements.
This argument is optional, and by default AVIO expects this value to be `globals.xml`. 

### GLOBAL_CONFIG

This rule checks that a global mule config exists, and that the expected configuration elements are not in other files. 
AVIO expects this file to be called `globals.xml` by default. 

The constructors for this rule are: 

```groovy
GlobalConfigRule()
GlobalConfigRule(String globalFileName)
GlobalConfigRule(String globalFileName, Map<String, String> noneGlobalElements)
```

*globalFileName* is the name of the file expected to contain global configuration elements for the Mule Application. 
This value is expected to be `globals.xml` by default. 

*noneGlobalElements*

### LOGGER_CATEGORY_HASVALUE

This rule checks that the category attribute is present on all loggers. 
The category attribute should be present to make Mule logs easier to search. 

The constructor for this rule is: 

```groovy
LoggerCategoryExistsRule()
```

This rule takes no arguments. 

### LOGGER_MESSAGE_CONTENT

This rule checks that the contents of a logger do not match a given Regular Expression. 
AVIO recommends that a developer never deploy code that logs the full payload at the level of INFO. 
The Mule message payload can contain sensitive information, and logging it at the default log level might persist sensitive information for longer than intended or make it visible to people who shouldn't see it. 

The constructors for this rule are: 

```groovy
LoggerMessageContentsRule()
LoggerMessageContentsRule(Pattern pattern)
LoggerMessageContentsRule(Map<String, Pattern> rules)
```

*pattern* is an optional rule to specify the regex that you would like to search for in the log message. 
By default, the regex is `~/payload]/`, which will catch basic payload logging, but allow for selecting individual fields. 

*rules* is a map where the key is the level of the logger, and the value is the regex that you would like to search for in the log message. 
The default stance of the rule is to only check INFO, which is equivalent to: 
```groovy
['INFO':~/payload]/]
```

### LOGGER_MESSAGE_HASVALUE

This rule checks that the message attribute is present on all loggers. 

The constructor for this rule is: 
```groovy
LoggerMessageExistsRule()
```

### MULE_CONFIG_SIZE

This rule checks that the Mule Configuration file does not contain too many flows or subflows. 

The constructors for this rule are: 

```groovy
MuleConfigSizeRule()
MuleConfigSizeRule(Integer flowLimit)
```

*flowLimit* is the number of flows and subflows required to fail the rule. 
By default this number of flows to fail the rule is `20`. 

### ON_ERROR_LOG_EXCEPTION

This rule checks that the logException rule is enabled for all `on-error-continue` and `on-error-propagate` components. 

The constructor for this rule is: 

```groovy
OnErrorLogExceptionRule()
```

This rule takes no arguments. 

### UNTIL_SUCCESSFUL

This rule checks that the until-successful component has not been used. 
AVIO suggests that this componnent should be avoided if possible since failures should be dealt with, not ignored. 

The constructor for the rule is: 

```groovy
UntilSuccessfulRule()
```

This rule takes no arguments. 

### UNUSED_FLOW

This rule checks that all flows or sub-flows are currently in use by this application. 
If the flow does not contain a source, is not named for the API Kit router, and is not mentioned by a flow-ref, it will fail this rule. 
Unused code should be removed before deployment. 

The constructor for this rule is: 

```groovy
UnusedFlowRule()
```

This rule takes no arguments. 

## GIT

### GIT_IGNORE

This rule checks that a `.gitignore` file is present at root and contains the expected files and folders. 

The constructor for this rule is: 

```groovy
GitIgnoreRule()
GitIgnoreRule(List<String> ignoredFiles)
```

*ignoredFiles* is a List of the expressions to look for in the `.gitignore` file. 
This argument is optional. By default the List is:
```groovy
['*.jar', '*.class', 'target/', '.project', '.classpath', '.idea', 'build']
```

## Mule Artifact

### MULE_ARTIFACT_SECURE_PROPERTIES

This rule checks that certain properties are secured within the `mule-artifact.json` file found at root. 
Some properties should be listed in the securedProperties Array in `mule-artifact.json`, to prevent keys from being shown in plain text in Anypoint Runtime Manager. 

The constructor for this rule is: 

```groovy
MuleArtifactHasSecurePropertiesRule()
MuleArtifactHasSecurePropertiesRule(List<String> properties, 
	Boolean includeDefaults)
```
*properties* is a List of values that should be in the securedProperties Array of the `mule-artifact.json`. 
By default, this list is: 
```groovy
['anypoint.platform.client_id', 'anypoint.platform.client_secret']
```

*includeDefaults* should be `true` if the developer wishes to include AVIO's default values, and `false` otherwise. 
The defaults to include are `'anypoint.platform.client_id'` and `'anypoint.platform.client_secret'`. 

### MULE_ARTIFACT_MIN_MULE_VERSION

This rule checks that the `minMuleVersion` exists and is not greater than the `app.runtime` version in the `pom.xml`. 

The constructor for this rule is: 

```groovy
MuleArtifactMinMuleVersionRule()
```

This rule takes no arguments. 

## POM

### MULE_MAVEN_PLUGIN

This rule ensures that the `mule-maven-plugin` of a specified version exists in the `pom.xml`.

```groovy
MuleMavenPluginVersionRule(String version)
```

*version* is the version number for the `mule-maven-plugin` that is expected within the `pom.xml`.

### MULE_RUNTIME

This rule ensures that the `app.runtime` property within the `pom.xml` matches a given value.

```groovy
MuleRuntimeVersionRule(String version)
```

*version* is the version number for the property `app.runtime` that is expected within the `pom.xml`.

### MUNIT_MAVEN_PLUGIN_ATTRIBUTES

This rule ensures that the `munit-maven-plugin` exists and is configured correctly. 
AVIO reccomends that Munit should be used, passing tests should be required, and that an arbitrary amount of code should be covered by Munit tests. 
Remember that test coverage does not ensure test quality. 

This rule has the following constructors:

```groovy
MunitMavenPluginAttributesRule()
MunitMavenPluginAttributesRule(List<String> ignoreFiles)
MunitMavenPluginAttributesRule(Map<String,String> coverageAttributeMap,
	Boolean includeDefaults)
MunitMavenPluginAttributesRule(Map<String,String> coverageAttributeMap,
	Boolean includeDefaults, List<String> ignoreFiles)
```

*ignoreFiles* is a list of test files that the `munit-maven-plugin should` ignore.
By default, AVIO does not expect to ignore test suites. 
Ignored test suites is a code smell, and should be removed before commiting/merging code.

*coverageAttributeMap* is a map of attributes expected to be present with given values on the `munit-maven-plugin` in the `pom.xml`.
By default, the expected attributes and values are:
```groovy
['runCoverage':'true',
'failBuild':'true',
'requiredApplicationCoverage':'80',
'requiredResourceCoverage':'80',
'requiredFlowCoverage':'80']
```

*includeDefaults* is a flag to include AVIO's default set of attributes in addition to whatever is provided by the *coverageAttributeMap*.
The default map is:
```groovy
['runCoverage':'true',
'failBuild':'true',
'requiredApplicationCoverage':'80',
'requiredResourceCoverage':'80',
'requiredFlowCoverage':'80']
```

### MUNIT_PLUGIN_VERSION

This rule checks that the version number for the `munit-maven-version` plugin matches a given value. 

The constructor for this rule is: 

```groovy
MunitPluginVersionRule(String version)
```

*version* is the version number expected for the `munit-maven-version` plugin.

### POM_DEPENDENCY_VERSION

This rule checks for the presence of a given dependency with a given version in the `pom.xml`. 

The constructor for this rule is: 

```groovy
PomDependencyVersionRule(String groupId, String artifactId, 
	String artifactVersion, Version.Operator versionOperator)
```

*groupId* is the group id for the jar in your artifact repository. 
An example might be `"com.companyname"`. 

*artifactId* is the artifact id for the jar in your artifact repository. 
An example might be `"example-plugin"`. 

*artifactVersion* is the artifact version for the jar in your artifact repository. 
An example might be `"1.0.0-SNAPSHOT"`. 

*versionOperator* is an Enum `Operator` found within class `com.avioconsulting.mule.linter.model.Version` describing how to match the provided `artifactVersion` to the one in the `pom.xml`. 
Current options are `EQUAL` or `GREATER_THAN`. 

### POM_EXISTS

This rule checks for the existence of a `pom.xml` file at the root of the project. 
Maven is required in Mule 4, and your project won't build if you don't have a `pom.xml`, so this rule might not be strictly necessary for a project. 

The constructor for this rule is: 

```groovy
PomExistsRule()
```

This rule takes no arguments. 

### POM_PLUGIN_ATTRIBUTE

This rule checks for the existence of a given set of attributes in a given maven plugin within the `pom.xml`. 
It exists as a customizable tool for a company to enforce a standard in their maven plugins. 

The constructors for this rule are: 

```groovy
PomPluginAttributeRule(String groupId, String artifactId, 
	Map<String,String> attributes)

PomPluginAttributeRule(String ruleId, String ruleName, String groupId, 
	String artifactId, Map<String,String> attributes)
```

*groupId* is a String representing the group id of the plugin to match against. 
An example might be `"com.companyname"`. 

*artifactId* is a String representing the artifact id of the plugin to match against. 
An example might be `"example-plugin"`. 

*attributes* is a Map representing the attributes that must be present in the plugin. 
An example for the map might be: 
```groovy
['examplekey':'examplevalue']
```

The POM_PLUGIN_ATTRIBUTE rule also allows for a custom ruleId and ruleName. 

*ruleId* defaults to a String with value `"POM_PLUGIN_ATTRIBUTE"`. 

*ruleName* defaults to a String with value `"Maven plugin exists in pom.xml and matches the attributes."`. 

### MAVEN_PROPERTY

This rule checks for the existence of a property with a given value in the `pom.xml`. 
It exists as a customizable tool for a company to enforce a standard for maven versioning. 

The constructors for this rule are: 

```groovy
PomPropertyValueRule(String propertyName, String propertyValue)

PomPropertyValueRule(String ruleId, String ruleName, String propertyName, 
	String propertyValue)
```

*propertyName* is the key to be set in the `pom.xml`. 

*propertyValue* is the value to be set in the `pom.xml`. 

The MAVEN_PROPERTY rule also allows for a custom ruleId and ruleName. 

*ruleId* defaults to a String with value `"MAVEN_PROPERTY"`. 

*ruleName* defaults to a String with value `"Maven property match"`. 

### ENCRYPTED_VALUE

This rule checks that properties containing `secret` or `password` are encrypted. 
AVIO always recommends that passwords and secrets are encrypted with a proper encryption key (Like AES or Blowfish). 
Unencrypted secrets within property files are dangerous in cases where code or files could be leaked, and past Mulesoft security issues have exposed or made visible arbitrary project files. 

The constructor for this rule is: 

```
EncryptedPasswordRule()
```

This rule takes no arguments. 

### HOSTNAME_PROPERTY

This rule checks that properties containing host or hostname should be fully qualified domain names, and not IP addresses. 
AVIO recommends that developers avoid using IP addresses, as they can be frequently changed and constrain routing options. 

The constructors for this rule are: 

```groovy
HostnamePropertyRule()
HostnamePropertyRule(String[] exemptions)
```

*exemptions* is a list of properties the rule should ignore. 

### PROPERTY_EXISTS

This rule checks that a property exists in a property file, with optional arguments for checking property files for custom environments and property file naming formats. 

The constructors for this rule are: 

```groovy
PropertyExistsRule(String propertyName)
PropertyExistsRule(String propertyName, List<String> environments)
PropertyExistsRule(String propertyName, List<String> environments, 
	String pattern)
```

*propertyName* is a String that the property being searched for must contain. 
For example, `hostname`. 

*environments* is a list of environments that the property must be found in.
This value is used when determining the name for property files to be searched.
The default list is:
```groovy
['dev', 'test', 'prod']
```

*pattern* is a custom naming scheme for property files loaded by environment. 
The default pattern is `"${appname}-${env}.properties"`. 

### PROPERTY_FILE_NAMING

This rule ensures that property files exist for each deployment environment, according to an optionally given pattern. 
Using string interpolation, it is possible to load a specific property file depending on other parameters such as environment. 
AVIO highly recommends that organizations take full advantage of that feature to avoid cross contamination between testing environments and production. 

The constructors for this rule are: 

```groovy
PropertyFileNamingRule(List<String> environments)
PropertyFileNamingRule(List<String> environments, String pattern)
```

*environments* is a list of environments used to check for the existence of property files for those environments. 
For example: 
```groovy
['dev', 'test', 'prod']
```

*pattern* is the basis for how a property file for a given environment is named. 
The default pattern is `"${appname}-${env}.properties"`. 

### PROPERTY_FILE_COUNT_MISMATCH

This rule ensures that the propety files for each environment have a matching number of properties in them. 
While not always the case, a different number of properties in one environment might mean that a property was left out of an environment by mistake. 

The constructors for this rule are: 

```groovy
PropertyFilePropertyCountRule(List<String> environments)
PropertyFilePropertyCountRule(List<String> environments, String pattern)
```

*environments* is a list of environments used to check for the number of properties per environment. 
For example: 
```groovy
['dev', 'test', 'prod']
```

*pattern* is the basis for how a property file for a given environment is named. 
The default pattern is `"${appname}-${env}.properties"`. 

### README

This rule checks for the existence of a `README.md` file at the root of the project. 
Projects should have a README in the project, explaining the purpose and content of the project. 

The constructor for this rule is: 

```groovy
ReadmeRule()
```

This rule takes no arguments. 

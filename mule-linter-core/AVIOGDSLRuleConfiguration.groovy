mule_linter {
    rules {
    /* CICD */
        AZURE_PIPELINES_EXISTS {}
        JENKINS_EXISTS {}

    /* CONFIGURATION */
        API_CONSOLE_DISABLED{}
        AUTO_DISCOVERY_EXISTS {
            enabled = true
            exemptedFlows = []
            environments = ['dev', 'test', 'prod']
            pattern = '${appname}-${env}.properties'
        }
        COMMENTED_CODE {}
        COMPONENT_ATTRIBUTE_VALUE {
            component = 'flow-ref'
            namespace = 'http://www.mulesoft.org/schema/mule/core'
            requiredAttributes = ['name']
        }
        COMPONENT_COUNT {
            component = 'flow-ref'
            namespace = 'http://www.mulesoft.org/schema/mule/core'
            maxCount = 5
        }
        CONFIG_FILE_NAMING {}
        CONFIG_PLACEHOLDER {
            placeholderAttributes = ['key', 'password', 'keyPassword', 'username', 'host']
        }
        CRON_EXPRESSION_EXTERNALIZED{}
        DISPLAY_NAME {
            components = [
                [name: 'set-payload', namespace: "http://www.mulesoft.org/schema/mule/core", displayName: 'Set Payload'],
                [name: 'set-variable', namespace: "http://www.mulesoft.org/schema/mule/core", displayName: 'Set Variable'],
                [name: 'transform', namespace: "http://www.mulesoft.org/schema/mule/ee/core", displayName: 'Transform Message'],
                [name: 'flow-ref', namespace: "http://www.mulesoft.org/schema/mule/core", displayName: 'Flow Reference']
            ]
        }
        EXCESSIVE_LOGGERS {
            excessiveLoggers = [
                    'INFO':3,
                    'DEBUG':2
            ]
        }
        EXCESSIVE_LOGGERS {
            excessiveLoggers = 2
        }
        FLOW_ERROR_HANDLER{}
        FLOW_SUBFLOW_COMPONENT_COUNT{
            maxCount = 20
        }
        FLOW_SUBFLOW_NAMING {
            format = 'KEBAB_CASE'
        }
        GLOBAL_CONFIG_NO_FLOWS {
            globalFileName = 'globals.xml'
        }
        GLOBAL_CONFIG_NO_FLOWS {}
        GLOBAL_CONFIG {
            globalFileName = 'global-config.xml'
        }
        LOGGER_ATTRIBUTES_RULE {
            requiredAttributes = ['category']
        }
        LOGGER_CATEGORY_HASVALUE {}
        LOGGER_MESSAGE_CONTENTS {
            pattern = '[0-9]*'
        }
        LOGGER_MESSAGE_HASVALUE {}
        MULE_CONFIG_SIZE {
            flowLimit = 2
        }
        ON_ERROR_LOG_EXCEPTION {}
        UNTIL_SUCCESSFUL {}
        UNUSED_FLOW {}

    /* GIT */
        GIT_IGNORE {}
        GIT_IGNORE {
            ignoredFiles = ['*.jar', '*.class', 'target/', '.project', '.classpath', '.idea', 'build']
        }

    /* MULE ARTIFACT */
        MULE_ARTIFACT_SECURE_PROPERTIES {
            properties = [
                'anypoint.platform.db.password'
            ]
            includeDefaults = false
        }
        MULE_ARTIFACT_MIN_MULE_VERSION {}

    /* POM */
        MULE_MAVEN_PLUGIN {
            version = '3.3.5'
        }
        MULE_RUNTIME {
            version = '4.3.0'
        }
        MUNIT_MAVEN_PLUGIN_ATTRIBUTES {
            coverageAttributeMap =[
                'runCoverage':'true',
                'failBuild':'true',
                'requiredApplicationCoverage':'80',
                'requiredResourceCoverage':'80',
                'requiredFlowCoverage':'80'
            ]
            includeDefaults = false
        }
        MUNIT_PLUGIN_VERSION {
            version = '2.2.1'
        }
        MUNIT_VERSION {
            version = '2.3.6'
        }
        POM_DEPENDENCY_VERSION {
            groupId = 'com.mulesoft.connectors'
            artifactId = 'mule-amazon-sqs-connector'
            artifactVersion = '5.11.0'
            versionOperator = 'GREATER_THAN'
        }
        POM_EXISTS {}
        POM_PLUGIN_ATTRIBUTE {
            groupId = 'org.mule.tools.maven'
            artifactId = 'mule-maven-plugin'
            attributes = [
                extensions: true
            ]
        }
        MAVEN_PROPERTY {
            propertyName = 'cloudhubWorkers'
            propertyValue = '2'
        }

    /* PROPERTY */
        APIKIT_VERSION {
            artifactVersion = '1.9.0'
        }
        ENCRYPTED_VALUE {}
        HOSTNAME_PROPERTY {
            exemptions = []
        }
        PROPERTY_EXISTS {
            environments = ['dev', 'test', 'prod']
            propertyName = 'db.user'
            pattern = '${appname}-${env}.yaml'
        }
        PROPERTY_NAME_PATTERN{}
        PROPERTY_FILE_NAMING {
            environments = ['dev', 'test', 'prod']
            pattern = '${appname}-${env}.properties'
        }
        PROPERTY_FILE_COUNT_MISMATCH {
            environments = ['dev', 'test', 'prod']
            pattern = '${appname}-${env}.properties'
        }

    /* README */
        README {}

    }
}
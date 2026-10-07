mule_linter {
    rules {
    /* CICD */
        rule('azure-pipelines-exists')
        rule('jenkins-exists')

    /* CONFIGURATION */
        rule('api-console-disabled')
        rule('auto-discovery-exists') {
            enabled = true
            exemptedFlows = []
            environments = ['dev', 'test', 'prod']
            pattern = '${appname}-${env}.properties'
        }
        rule('commented-code')
        rule('component-required-attributes') {
            component = 'flow-ref'
            namespace = 'http://www.mulesoft.org/schema/mule/core'
            requiredAttributes = ['name']
        }
        rule('component-count') {
            component = 'flow-ref'
            namespace = 'http://www.mulesoft.org/schema/mule/core'
            maxCount = 5
        }
        rule('config-file-naming')
        rule('config-placeholder') {
            placeholderAttributes = ['key', 'password', 'keyPassword', 'username', 'host']
        }
        rule('connection-retry-config') {
            components = [
                    [name: 'request', namespace: 'http://www.mulesoft.org/schema/mule/http', 'config-ref': 'request-config'],
                    [name: 'publish', namespace: 'http://www.mulesoft.org/schema/mule/vm', 'config-ref': 'config '],
                    [name: 'publish-consume', namespace: 'http://www.mulesoft.org/schema/mule/vm', 'config-ref': 'config']
            ]
        }
        rule('connection-timeout-config') {
            components = [
                    [name: 'request', namespace: 'http://www.mulesoft.org/schema/mule/http', timeoutAttribute: 'responseTimeout', 'config-ref': 'request-config'],
                    [name: 'publish', namespace: 'http://www.mulesoft.org/schema/mule/vm', timeoutAttribute: 'responseTimeout', 'config-ref': 'config'],
                    [name: 'publish-consume', namespace: 'http://www.mulesoft.org/schema/mule/vm', timeoutAttribute: 'responseTimeout', 'config-ref': 'config']
            ]
        }
        rule('cron-expression-externalized')
        rule('component-display-name') {
            components = [
                [name: 'set-payload', namespace: "http://www.mulesoft.org/schema/mule/core", displayName: 'Set Payload'],
                [name: 'set-variable', namespace: "http://www.mulesoft.org/schema/mule/core", displayName: 'Set Variable'],
                [name: 'transform', namespace: "http://www.mulesoft.org/schema/mule/ee/core", displayName: 'Transform Message'],
                [name: 'flow-ref', namespace: "http://www.mulesoft.org/schema/mule/core", displayName: 'Flow Reference']
            ]
        }
        rule('consecutive-loggers-count') {
            excessiveLoggers = [
                    'INFO':3,
                    'DEBUG':2
            ]
        }
        rule('consecutive-loggers-count') {
            excessiveLoggers = 2
        }
        rule('flow-error-handler')
        rule('flow-subflow-component-count') {
            maxCount = 20
        }
        rule('flow-subflow-naming') {
            format = 'KEBAB_CASE'
        }
        rule('global-config-no-flows') {
            globalFileName = 'globals.xml'
        }
        rule('global-config-no-flows')
        rule('global-config-exists') {
            globalFileName = 'global-config.xml'
        }
        rule('logger-required-attributes') {
            requiredAttributes = ['category']
        }
        rule('logger-category-has-value')
        rule('logger-message-contents') {
            pattern = '[0-9]*'
        }
        rule('logger-message-has-value')
        rule('mule-config-flow-limit') {
            flowLimit = 2
        }
        rule('on-error-log-exception')
        rule('until-successful')
        rule('unused-flow')

    /* GIT */
        rule('git-ignore')
        rule('git-ignore') {
            ignoredFiles = ['*.jar', '*.class', 'target/', '.project', '.classpath', '.idea', 'build']
        }

    /* MULE ARTIFACT */
        rule('mule-artifact-secure-properties') {
            properties = [
                'anypoint.platform.db.password'
            ]
            includeDefaults = false
        }
        rule('mule-artifact-min-mule-version')

    /* POM */
        rule('mule-maven-plugin') {
            version = '3.3.5'
        }
        rule('mule-runtime') {
            version = '4.3.0'
        }
        rule('munit-maven-plugin-attributes') {
            coverageAttributeMap =[
                'runCoverage':'true',
                'failBuild':'true',
                'requiredApplicationCoverage':'80',
                'requiredResourceCoverage':'80',
                'requiredFlowCoverage':'80'
            ]
            includeDefaults = false
        }
        rule('munit-plugin-version') {
            version = '2.2.1'
        }
        rule('munit-version') {
            version = '2.3.6'
        }
        rule('pom-dependency-version') {
            groupId = 'com.mulesoft.connectors'
            artifactId = 'mule-amazon-sqs-connector'
            artifactVersion = '5.11.0'
            versionOperator = 'GREATER_THAN'
        }
        rule('pom-file-exists')
        rule('pom-plugin-attribute') {
            groupId = 'org.mule.tools.maven'
            artifactId = 'mule-maven-plugin'
            attributes = [
                extensions: true
            ]
        }
        rule('maven-property') {
            propertyName = 'cloudhubWorkers'
            propertyValue = '2'
        }

    /* PROPERTY */
        rule('apikit-version') {
            artifactVersion = '1.9.0'
        }
        rule('encrypted-value')
        rule('hostname-property') {
            exemptions = []
        }
        rule('property-exists') {
            environments = ['dev', 'test', 'prod']
            propertyName = 'db.user'
            pattern = '${appname}-${env}.yaml'
        }
        rule('property-name-pattern')
        rule('property-file-naming') {
            environments = ['dev', 'test', 'prod']
            pattern = '${appname}-${env}.properties'
        }
        rule('property-file-count-mismatch') {
            environments = ['dev', 'test', 'prod']
            pattern = '${appname}-${env}.properties'
        }

    /* README */
        rule('readme')

    }
}

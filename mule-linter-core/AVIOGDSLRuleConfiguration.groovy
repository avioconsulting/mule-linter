mule_linter {
    rules {
        /* CICD */
        AZURE_PIPELINES_EXISTS {}
        JENKINS_EXISTS {}


        /* CONFIGURATION */
        API_CONSOLE_DISABLED{}
        COMMENTED_CODE {}
        /*COMPONENT_ATTRIBUTE_VALUE {}*/
        COMPONENT_COUNT {
            component = 'flow-ref'
            namespace = 'http://www.mulesoft.org/schema/mule/core'
            maxCount = 5
        }
        CONFIG_FILE_NAMING {}
        CONFIG_PLACEHOLDER {
            placeholderAttributes = ['key', 'password', 'keyPassword', 'username', 'host']
        }
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
/*

        GIT_IGNORE {}
        MUNIT_VERSION {
            version = '3.3.51'
        }
        FLOW_SUBFLOW_NAMING {
            format = 'CAMEL_CASE'
        }
        PROPERTY_EXISTS {
            environments = ['dev', 'test', 'prod']
            propertyName = 'db.user'
        }
        */
    }
}
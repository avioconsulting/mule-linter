mule_linter {
    rules {
        JENKINS_EXISTS {}
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
    }
}
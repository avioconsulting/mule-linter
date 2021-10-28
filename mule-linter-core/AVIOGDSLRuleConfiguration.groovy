/*
mule_linter {
    rules {
        JENKINS_EXISTS ()
        GIT_IGNORE ()
        CONFIG_FILE_NAMING ([
            format : CaseFormat.KEBAB_CASE
        ])
        FLOW_SUBFLOW_NAMING ([
            format : CaseFormat.PASCAL_CASE
        ])
    }
}
*/

mule_linter {
    rules {
        addRule RULE.JENKINS_EXISTS
        addRule RULE.GIT_IGNORE
        addRule RULE.CONFIG_FILE_NAMING,[
            format: CaseFormat.KEBAB_CASE
        ]
        addRule RULE.FLOW_SUBFLOW_NAMING,[
            format : CaseFormat.PASCAL_CASE
        ]
    }
}

package com.avioconsulting.mule.linter.model

class Version {

    enum Operator {

        EQUAL,
        GREATER_THAN

    }

    private String version

    Version(String version) {
        this.version = version
    }

    void setVersion(String version) {
        this.version = version
    }

    Boolean isEqual(String version) {
        return this.version == version
    }

    Boolean isGreater(String version) {
        return this.version < version
    }

}


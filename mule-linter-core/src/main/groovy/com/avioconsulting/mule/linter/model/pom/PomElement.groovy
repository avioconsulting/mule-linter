package com.avioconsulting.mule.linter.model.pom

class PomElement {

    private String name
    private String value
    private Integer lineNo

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getValue() {
        return value
    }

    void setValue(String value) {
        this.value = value
    }

    Integer getLineNo() {
        return lineNo
    }

    void setLineNo(Integer lineNo) {
        this.lineNo = lineNo
    }

}

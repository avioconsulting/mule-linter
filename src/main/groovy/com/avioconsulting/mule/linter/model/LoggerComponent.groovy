package com.avioconsulting.mule.linter.model

class LoggerComponent {

    private String name
    private String message
    private String level
    private String category
    private Integer lineNo

    LoggerComponent(){

    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getMessage() {
        return message
    }

    void setMessage(String message) {
        this.message = message
    }

    String getLevel() {
        return level
    }

    void setLevel(String level) {
        this.level = level
    }

    String getCategory() {
        return category
    }

    void setCategory(String category) {
        this.category = category
    }

    Integer getLineNo() {
        return lineNo
    }

    void setLineNo(Integer lineNo) {
        this.lineNo = lineNo
    }
}

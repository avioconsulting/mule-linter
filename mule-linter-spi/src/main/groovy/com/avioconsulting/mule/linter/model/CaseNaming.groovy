package com.avioconsulting.mule.linter.model

class CaseNaming {

    enum CaseFormat {

        CAMEL_CASE,
        PASCAL_CASE,
        SNAKE_CASE,
        KEBAB_CASE

    }

    static final Map<CaseFormat,String> regex = [(CaseFormat.CAMEL_CASE):'[a-z]+((\\d)|([A-Z0-9][a-z0-9]+))*([A-Z])?',
                                                 (CaseFormat.PASCAL_CASE):'^[A-Z][a-zA-Z0-9]+$',
                                                 (CaseFormat.SNAKE_CASE):'^([a-z][a-z0-9]*)(_[a-z0-9]+)*$',
                                                 (CaseFormat.KEBAB_CASE):'^([a-z][a-z0-9]*)(-[a-z0-9]+)*$',
                                                 ('JAVA_PROPERTIES_CASE'):'^([a-z][a-z0-9]*)(\\.[a-z0-9]+)*$']
    CaseFormat format

    CaseNaming(CaseFormat format) {
        this.format = format
    }

    void setFormat(CaseFormat format) {
        this.format = format
    }

    Boolean isValidFormat(String value) {
        return value.matches(regex.get(format))
    }
}

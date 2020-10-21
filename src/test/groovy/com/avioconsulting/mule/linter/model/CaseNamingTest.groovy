package com.avioconsulting.mule.linter.model

import spock.lang.Specification

class CaseNamingTest extends Specification {

    def "camelCase string check"() {
        given:
        CaseNaming caseNaming = new CaseNaming(CaseNaming.CaseFormat.CAMEL_CASE)

        expect:
        caseNaming.isValidFormat(STRING) == RESULT

        where:
        STRING                        | RESULT
        'cameCase'                    | true
        'cameCase2'                   | true
        'cameCaseID'                  | false
        'newCuXMLHTTPRequeststomerID' | false
        'PascalCase'                  | false
        'PascalCase2'                 | false
        'snake_case'                  | false
        'snake_case_2'                | false
        'snake_Case_incorrect'        | false
        'SNAKE_CASE_INCORRECT'        | false
        'kebab-case'                  | false
        'kebab-case-2'                | false
        'kebab-case-Incorrect'        | false
    }

    def "PascalCase string check"() {
        given:
        CaseNaming caseNaming = new CaseNaming(CaseNaming.CaseFormat.PASCAL_CASE)

        expect:
        caseNaming.isValidFormat(STRING) == RESULT

        where:
        STRING                        | RESULT
        'cameCase'                    | false
        'cameCase2'                   | false
        'cameCaseID'                  | false
        'newCuXMLHTTPRequeststomerID' | false
        'PascalCase'                  | true
        'PascalCase2'                 | true
        'snake_case'                  | false
        'snake_case_2'                | false
        'snake_Case_incorrect'        | false
        'SNAKE_CASE_INCORRECT'        | false
        'kebab-case'                  | false
        'kebab-case-2'                | false
        'kebab-case-Incorrect'        | false
    }

    def "sake_case string check"() {
        given:
        CaseNaming caseNaming = new CaseNaming(CaseNaming.CaseFormat.SNAKE_CASE)

        expect:
        caseNaming.isValidFormat(STRING) == RESULT

        where:
        STRING                        | RESULT
        'cameCase'                    | false
        'cameCase2'                   | false
        'cameCaseID'                  | false
        'newCuXMLHTTPRequeststomerID' | false
        'PascalCase'                  | false
        'PascalCase2'                 | false
        'snake_case'                  | true
        'snake_case_2'                | true
        'snake_Case_incorrect'        | false
        'SNAKE_CASE_INCORRECT'        | false
        'kebab-case'                  | false
        'kebab-case-2'                | false
        'kebab-case-Incorrect'        | false
    }

    def "kebab-case string check"() {
        given:
        CaseNaming caseNaming = new CaseNaming(CaseNaming.CaseFormat.KEBAB_CASE)

        expect:
        caseNaming.isValidFormat(STRING) == RESULT

        where:
        STRING                        | RESULT
        'cameCase'                    | false
        'cameCase2'                   | false
        'cameCaseID'                  | false
        'newCuXMLHTTPRequeststomerID' | false
        'PascalCase'                  | false
        'PascalCase2'                 | false
        'snake_case'                  | false
        'snake_case_2'                | false
        'snake_Case_incorrect'        | false
        'SNAKE_CASE_INCORRECT'        | false
        'kebab-case'                  | true
        'kebab-case-2'                | true
        'kebab-case-Incorrect'        | false
    }
}
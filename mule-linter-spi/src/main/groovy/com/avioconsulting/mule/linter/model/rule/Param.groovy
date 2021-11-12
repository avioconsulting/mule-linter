package com.avioconsulting.mule.linter.model.rule

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.PARAMETER, ElementType.FIELD])
@interface Param {
    String value()
}
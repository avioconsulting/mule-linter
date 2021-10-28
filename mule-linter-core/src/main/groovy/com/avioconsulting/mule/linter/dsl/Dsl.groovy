package com.avioconsulting.mule.linter.dsl

import static groovy.lang.Closure.DELEGATE_ONLY

abstract class Dsl extends Script {
    static MuleLinterDsl mule_linter(@DelegatesTo(value = MuleLinterDsl, strategy = DELEGATE_ONLY) final Closure closure) {
        final MuleLinterDsl dsl = new MuleLinterDsl()

        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()

        return dsl
    }
}

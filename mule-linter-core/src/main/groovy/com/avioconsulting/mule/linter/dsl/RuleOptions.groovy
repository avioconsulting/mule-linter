package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule

import java.lang.reflect.Field

/**
 * Validates public DSL options before Groovy's otherwise permissive coercion.
 * Checks declared outer types, not generic collection contents or requiredness.
 * Dynamic Object options and domain-specific values remain the rule's init() responsibility.
 */
class RuleOptions {
    private final Rule rule
    private final Map<String, Field> options

    RuleOptions(Rule rule) {
        this.rule = rule
        this.options = fieldsFor(rule.class)
    }

    static Map<String, Field> fieldsFor(Class<? extends Rule> ruleClass) {
        Map<String, Field> fields = [:]
        for (Class type = ruleClass; type != null && Rule.isAssignableFrom(type); type = type.superclass) {
            type.declaredFields.each { field ->
                Param param = field.getAnnotation(Param)
                if (param != null) {
                    fields.putIfAbsent(param.value(), field)
                } else if (type == Rule && field.name in ['severity', 'ruleType', 'ruleName']) {
                    fields.putIfAbsent(field.name, field)
                }
            }
        }
        return fields
    }

    @Override
    Object getProperty(String name) {
        Field field = option(name)
        return rule.getProperty(field.name)
    }

    @Override
    void setProperty(String name, Object value) {
        Field field = option(name)
        Class type = field.type
        Object converted = value
        if (value != null && type.isEnum() && value instanceof CharSequence) {
            try {
                converted = Enum.valueOf(type, value.toString())
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid option '${name}' for rule '${rule.ruleId}': expected one of ${type.enumConstants*.name()}, got '${value}'", e)
            }
        } else if (type == String && value instanceof GString) {
            converted = value.toString()
        }
        Class boxed = [
                (Boolean.TYPE): Boolean, (Byte.TYPE): Byte, (Short.TYPE): Short,
                (Integer.TYPE): Integer, (Long.TYPE): Long, (Float.TYPE): Float,
                (Double.TYPE): Double, (Character.TYPE): Character
        ].get(type, type)
        if ((value == null && type.isPrimitive()) || (converted != null && !boxed.isInstance(converted))) {
            throw new IllegalArgumentException("Invalid option '${name}' for rule '${rule.ruleId}': expected ${type.simpleName}, got ${value == null ? 'null' : value.getClass().simpleName}")
        }
        try {
            rule.setProperty(field.name, converted)
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid option '${name}' for rule '${rule.ruleId}': ${e.message}", e)
        }
    }

    def methodMissing(String name, args) {
        // Preserve normal Groovy setter/getter syntax without bypassing option validation.
        if (name.startsWith('set') && name.length() > 3 && args.length == 1) {
            String optionName = java.beans.Introspector.decapitalize(name.substring(3))
            setProperty(optionName, args[0])
            return null
        }
        if (name.startsWith('get') && name.length() > 3 && args.length == 0) {
            return getProperty(java.beans.Introspector.decapitalize(name.substring(3)))
        }
        return rule.invokeMethod(name, args)
    }

    private Field option(String name) {
        Field field = options.get(name)
        if (field == null) {
            throw new IllegalArgumentException("Unknown option '${name}' for rule '${rule.ruleId}'. Available options: ${options.keySet().sort().join(', ')}")
        }
        return field
    }
}

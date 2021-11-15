package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

class GDSLGenerator {


    class RuleMeta {
        String ruleId
        List<Tuple2> params = new ArrayList<>()
        Class<? extends Rule> ruleClass
        RuleMeta(ruleClass){
            this.ruleClass = ruleClass
        }
    }

    def generate(String templatePath, String outputPath) {

        println 'Generating GDSL file'
        VelocityEngine engine = new VelocityEngine();
        VelocityContext context = new VelocityContext();
        String templateFileName = templatePath
        Template template = engine.getTemplate(templateFileName)
        Map<String, RuleMeta> rulesMap = getRulesMap(null)
        context.put("rules", rulesMap)
        context.put("ruleIds", rulesMap.keySet())
        try {
            def file = new File(outputPath)
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file))
            template.merge(context, writer);
            writer.close();
            println 'Generated GDSL at ' + file.path
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Map<String, RuleMeta> getRulesMap(String packagePrefix) {
        def Map<String, RuleMeta> rulesMap = [:]
//This loads all Rule classes shipped with core.
//TODO: Find a way to load all classes from external library that can have different package.
        Reflections rf = new Reflections(new ConfigurationBuilder()
                .forPackages("com.", "org.", "io.")
                .setExpandSuperTypes(false)
                .addScanners(Scanners.values()))

        def rules = rf.getSubTypesOf(Rule.class)
//Look for field named RULE_ID
        rules.each { rule -> {
            rulesMap.put(rule.RULE_ID, new RuleMeta(rule))
        }}


        rf.getFieldsAnnotatedWith(Param.class).each { field -> {
            def aValue = field.getAnnotation(Param.class).value()
            def ruleId = field.declaringClass.RULE_ID
            rulesMap.get(ruleId).params.add(new Tuple2(aValue, field.genericType.typeName))
        }}


        rf.getConstructorsWithParameter(Param.class).each { cs -> {
            cs.parameters.each { p ->
                if(p.isAnnotationPresent(Param.class)) {
                    def annotation = p.getAnnotation(Param.class)
                    def aValue = annotation.value()

                    def ruleId = cs.declaringClass.RULE_ID
                    rulesMap.get(ruleId).params.add(new Tuple2(aValue, p.getParameterizedType().typeName))
                }

            }
        }}

        return rulesMap
    }
}

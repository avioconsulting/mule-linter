package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine

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
        // Use exactly the same discovery and duplicate-ID checks as the runtime DSL.
        RulesLoader.rulesMap.each { ruleId, rule ->
            RuleMeta meta = new RuleMeta(rule)
            meta.ruleId = ruleId
            RuleOptions.fieldsFor(rule).each { name, field ->
                meta.params.add(new Tuple2(name, field.genericType.typeName))
            }
            rulesMap.put(ruleId, meta)
            rule.declaredConstructors.each { cs ->
                cs.parameters.each { p ->
                    if(p.isAnnotationPresent(Param.class)) {
                        def annotation = p.getAnnotation(Param.class)
                        def aValue = annotation.value()
                        if (!meta.params.any { it.first == aValue }) {
                            meta.params.add(new Tuple2(aValue, p.getParameterizedType().typeName))
                        }
                    }
                }
            }
        }

        return rulesMap
    }
}

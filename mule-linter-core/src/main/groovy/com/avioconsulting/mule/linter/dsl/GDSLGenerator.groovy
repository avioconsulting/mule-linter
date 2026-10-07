package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.catalog.RuleCatalog
import com.avioconsulting.mule.linter.model.rule.Rule
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine

class GDSLGenerator {


    class RuleMeta {
        String ruleId
        String canonicalId
        List<String> aliases
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
        RuleCatalog.instance.definitions.each { definition ->
            RuleMeta meta = new RuleMeta(definition.ruleClass)
            meta.ruleId = definition.reportId
            meta.canonicalId = definition.id
            meta.aliases = definition.aliases
            definition.options.each { name, option ->
                meta.params.add(new Tuple2(name, option.javaTypeName))
            }
            rulesMap.put(definition.reportId, meta)
        }

        return rulesMap
    }
}

package com.avioconsulting.mule;
import org.sonar.api.Plugin;
import org.sonar.api.Plugin;
import com.avioconsulting.mule.linter.model.rule.RuleSet;
import com.avioconsulting.mule.linter.model.CaseNaming;
import com.avioconsulting.mule.linter.rule.cicd.*;
import com.avioconsulting.mule.linter.rule.configuration.*;
import com.avioconsulting.mule.linter.rule.git.*;
import com.avioconsulting.mule.linter.rule.muleartifact.*;
import com.avioconsulting.mule.linter.rule.pom.*;
import com.avioconsulting.mule.linter.rule.property.*;

import org.sonar.api.Plugin;
import org.sonar.api.Plugin.Context;
import org.sonar.api.config.PropertyDefinition;

public class MulePlugin implements Plugin {
    private static final String GENERAL = "General";

    public MulePlugin() {
    }

    public void define(Context context) {


        context.addExtensions(MuleLanguage.class, MuleSensor.class, new Object[0]);
        context.addExtension(MuleRulesDefinition.class);
        context.addExtension(MuleQualityProfile.class);
        context.addExtension(PropertyDefinition.builder("sonar.mule.file.suffixes").defaultValue(".xml").name("File Suffixes").description("List of suffixes for files to analyze.").subCategory("General").category("Mule").multiValues(true).onQualifiers("TRK", new String[0]).build());
        context.addExtensions(MuleMetrics.class, ConfigurationFilesSensor.class, new Object[]{MuleSizeRating.class, MuleFlowCount.class, MuleSubFlowCount.class, MuleTransformationCount.class, CoverageSensor.class, MUnitSensor.class});
    }
}

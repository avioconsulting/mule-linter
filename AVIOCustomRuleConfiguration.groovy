import com.avioconsulting.mule.linter.model.rule.RuleSet
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.rule.cicd.*
import com.avioconsulting.mule.linter.rule.configuration.*
import com.avioconsulting.mule.linter.rule.git.*
import com.avioconsulting.mule.linter.rule.muleartifact.*
import com.avioconsulting.mule.linter.rule.pom.*
import com.avioconsulting.mule.linter.rule.property.*

class AVIOCustomRuleConfiguration {
	static final List<String> ENVIRONMENTS = ['dev','test','prod']
	static final String GLOBALS_FILENAME = 'globals.xml'

	static RuleSet getRules() {
		RuleSet rules = new RuleSet()

		//cicd
		rules.addRule(new JenkinsFileExistsRule())

		//configuration
		rules.addRule(new ConfigFileNamingRule(CaseNaming.CaseFormat.KEBAB_CASE))
		rules.addRule(new FlowSubflowNamingRule(CaseNaming.CaseFormat.KEBAB_CASE))
		rules.addRule(new GlobalConfigNoFlowsRule(GLOBALS_FILENAME))
		rules.addRule(new GlobalConfigRule(GLOBALS_FILENAME))
		rules.addRule(new LoggerCategoryExistsRule())
		rules.addRule(new LoggerMessageExistsRule())
		rules.addRule(new OnErrorLogExceptionRule())
		rules.addRule(new UnusedFlowRule())

		//git
		rules.addRule(new GitIgnoreRule())

		//muleArtifact
		rules.addRule(new MuleArtifactHasSecurePropertiesRule())
		rules.addRule(new MuleArtifactMinMuleVersionRule())

		//pom
		rules.addRule(new MuleMavenPluginVersionRule('3.3.5'))
		rules.addRule(new MuleRuntimeVersionRule('4.2.1'))
		rules.addRule(new MunitMavenPluginAttributesRule())
		rules.addRule(new MunitVersionRule('2.2.1'))
		rules.addRule(new PomExistsRule())

		//property
		rules.addRule(new EncryptedPasswordRule())
		rules.addRule(new PropertyExistsRule('db.user', ENVIRONMENTS))
		rules.addRule(new PropertyFileNamingRule(ENVIRONMENTS))
		rules.addRule(new PropertyFilePropertyCountRule(ENVIRONMENTS))

		return rules
	}

}

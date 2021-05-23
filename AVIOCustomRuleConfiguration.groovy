import com.avioconsulting.mule.linter.model.rule.RuleSet
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.rule.cicd.*
import com.avioconsulting.mule.linter.rule.configuration.*
import com.avioconsulting.mule.linter.rule.git.*
import com.avioconsulting.mule.linter.rule.muleartifact.*
import com.avioconsulting.mule.linter.rule.pom.*
import com.avioconsulting.mule.linter.rule.property.*
import com.avioconsulting.mule.linter.rule.readme.*
import com.avioconsulting.mule.linter.rule.autodiscovery.*

class AVIOCustomRuleConfiguration {
	static final List<String> ENVIRONMENTS = ['dev','test','prod']
	// updated global config file name based on AVIO standards
	static final String GLOBALS_FILENAME = 'global-config.xml'

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
		// Updated Mule Maven plugin version, Mule Runtime Version, Munit Version
		rules.addRule(new MuleMavenPluginVersionRule('3.4.1'))
		rules.addRule(new MuleRuntimeVersionRule('4.3.0'))
		rules.addRule(new MunitMavenPluginAttributesRule())
		rules.addRule(new MunitVersionRule('2.3.1'))
		rules.addRule(new PomExistsRule())

		//property
		rules.addRule(new EncryptedPasswordRule())
		rules.addRule(new PropertyExistsRule('db.user', ENVIRONMENTS))
		rules.addRule(new PropertyFileNamingRule(ENVIRONMENTS))
		rules.addRule(new PropertyFilePropertyCountRule(ENVIRONMENTS))

		// Added Readme File Rule
		rules.addRule(new ReadmeRule())
		// Added Autodiscovery Configuration Rule
		rules.addRule(new AutoDiscoveryRule())
		//Added MuleConfigFileSize Rule
		rules.addRule(new MuleConfigSizeRule())

		//rules.addRule(new HostnamePropertyRule())
		rules.addRule(new HostnamePropertyRule('0.0.0.0'))

		rules.addRule(new PomDependencyVersionRule("org.mule.connectors", "mule-http-connector", "1.5.14"))
		rules.addRule(new AutoDiscoveryRule("global-config.xml"))


		return rules
	}

}

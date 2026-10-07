package com.avioconsulting.mule.linter

import com.avioconsulting.mule.linter.catalog.RuleCatalog
import com.avioconsulting.mule.linter.dsl.ConfigurationLoader
import groovy.json.JsonOutput
import picocli.CommandLine
import java.util.concurrent.Callable

/** Discovery/validation commands operate without an application directory or Maven access. */
final class CatalogCommands {
    @CommandLine.Command(name = 'rules', description = 'Discover installed rule contracts.', mixinStandardHelpOptions = true,
        subcommands = [ListRules, DescribeRule])
    static class RulesCommand { }

    @CommandLine.Command(name = 'config', description = 'Inspect configuration without analyzing an application.', mixinStandardHelpOptions = true,
        subcommands = [ValidateConfig])
    static class ConfigCommand { }

    static abstract class Action implements Callable<Integer> {
        @CommandLine.Spec CommandLine.Model.CommandSpec spec
        @CommandLine.Option(names = '--json', description = 'Emit a versioned machine-readable JSON response.')
        boolean json

        protected int respond(Closure<Map> action, Closure<String> human) {
            try {
                Map data = [schemaVersion: 1] + action.call()
                output(json ? encode(data) : human.call(data))
                0
            } catch (Exception e) {
                if (json) {
                    try { output(encode([schemaVersion: 1, valid: false, errors: [e.message ?: e.class.name]])) }
                    catch (Exception outputError) { spec.commandLine().err.println("Linter error: ${outputError.message}") }
                } else spec.commandLine().err.println("Linter error: ${e.message}")
                2
            }
        }

        private void output(String text) {
            PrintWriter writer = spec.commandLine().out
            writer.println(text)
            writer.flush()
            if (writer.checkError()) throw new IOException('Failed to write command output')
        }
        protected static String encode(Object value) { JsonOutput.prettyPrint(JsonOutput.toJson(value)) }
    }

    @CommandLine.Command(name = 'list', description = 'List all installed rules in canonical ID order.', mixinStandardHelpOptions = true, exitCodeOnExecutionException = 2)
    static class ListRules extends Action {
        Integer call() {
            respond({ [rules: RuleCatalog.instance.definitions.collect { it.describe() }] }, { Map data ->
                data.rules.collect { "${it.id} [${it.defaultSeverity}] — ${it.description}" }.join('\n')
            })
        }
    }

    @CommandLine.Command(name = 'describe', description = 'Describe a rule using its canonical ID or any alias.', mixinStandardHelpOptions = true, exitCodeOnExecutionException = 2)
    static class DescribeRule extends Action {
        @CommandLine.Parameters(index = '0', paramLabel = 'ID', description = 'Canonical rule ID or alias.')
        String identifier

        Integer call() {
            respond({ [rule: RuleCatalog.instance.resolve(identifier).describe()] }, { Map data ->
                def rule = data.rule
                "${rule.id}\n${rule.description}\nAliases: ${rule.aliases.join(', ')}\nReport ID: ${rule.reportId}\nDefault severity: ${rule.defaultSeverity}\nOptions:\n${encode(rule.options)}"
            })
        }
    }

    @CommandLine.Command(name = 'validate', description = 'Validate a trusted Groovy configuration without loading an application.', mixinStandardHelpOptions = true, exitCodeOnExecutionException = 2)
    static class ValidateConfig extends Action {
        @CommandLine.Parameters(index = '0', paramLabel = 'FILE', description = 'Groovy rule configuration file.')
        File file

        Integer call() {
            respond({
                def configuration = ConfigurationLoader.load(file, spec.commandLine().err)
                [valid: true, configuration: file.path, ruleCount: configuration.rulesDsl.specifications.size(),
                 rules: configuration.rulesDsl.specifications.collect { [id: it.definition.id, reportId: it.definition.reportId] }]
            }, { Map data -> "Valid configuration: ${data.configuration}\n${data.ruleCount} rule instances configured." })
        }
    }
}

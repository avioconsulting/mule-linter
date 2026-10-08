# Mule Linter Maven Plugin

The mule linter can be run as mvn plugin. The maven plugin is located in module `mule-linter-maven-plugin`.

Plugin goal is by default attached to validate which is the first phase of maven lifecycle.

## Parameters
- appDir: Defaults to `${basedir}`
- ruleConfiguration: Defaults to `${basedir}/muleLinter.groovy` for compatibility. Accepts `.yaml`, `.yml`, or trusted `.groovy` configuration.
- outputDirectory: Defaults to `${project.build.directory}/mule-linter`
- formats: Defaults to CONSOLE and JSON.
- failBuild: Defaults to false (report only). Set to true to enforce findings at or above `failureThreshold`.
- failureThreshold: Defaults to MAJOR. Valid values in descending severity order: BLOCKER, CRITICAL, MAJOR, MINOR. The default enforces MAJOR, CRITICAL and BLOCKER only; all findings are still reported.
- strictAnalysis: Defaults to false. Set to true to fail when analysis is incomplete, for example if a parent POM cannot be resolved. Otherwise unresolved parents remain analysis warnings, separate from violations.

Enforced findings produce `MojoFailureException`. Configuration, rule execution,
strict-analysis and requested report generation/write errors always produce
`MojoExecutionException`, even with `failBuild=false`. Reports are generated
before enforcing findings; report errors are never swallowed.

Console output retains Maven's logging adapter. JSON is written to
`outputDirectory/mule-linter-report.json` in the existing SonarQube external-issues
structure; warnings/completeness are logged separately rather than added to that
schema. Reports consume one immutable analysis snapshot and do not change each
other's findings or file paths when their order changes.

## Usage

The plugin is published to Maven Central and Latest version can be seen [on Maven Central](https://central.sonatype.com/artifact/com.avioconsulting.mule/mule-linter-maven-plugin/versions). 

To use plugin in your maven project, add following plugin configuration in project pom -

```xml

<plugin>
    <groupId>com.avioconsulting.mule</groupId>
    <artifactId>mule-linter-maven-plugin</artifactId>
    <version>LATEST_VERSION</version>
    <executions>
        <execution>
            <id>validate</id>
            <phase>validate</phase>
            <goals>
                <goal>validate</goal>
            </goals>
            <configuration>
                <appDir>${basedir}</appDir>
                <ruleConfiguration>muleLinter.groovy</ruleConfiguration>
                <outputDirectory>${project.build.directory}/reports</outputDirectory>
                <failBuild>false</failBuild>
                <failureThreshold>MAJOR</failureThreshold>
                <strictAnalysis>false</strictAnalysis>
                <formats>
                    <format>CONSOLE</format>
                    <format>JSON</format>
                </formats>
            </configuration>
        </execution>
    </executions>
</plugin>
```
The plugin parses the POM and resolves parent inheritance using embedded Maven
Resolver and Maven settings; it does not invoke Maven to generate an effective POM.
The published plugin isolates its embedded Resolver 2 and Maven model/settings
classes from the Maven host's classes. Resolver sessions are scoped to each goal
execution and closed before Maven disposes the plugin classloader.

To use YAML, set the configuration path explicitly:

```xml
<ruleConfiguration>${basedir}/mule-linter.yaml</ruleConfiguration>
```

YAML and Groovy share catalog validation, defaults, report IDs, and failure policy.
Generate an editor schema and validate YAML using the CLI before running Maven:

```shell
mule-linter config schema -o mule-linter.schema.json
mule-linter config validate mule-linter.yaml --json
```

See the root [README](../README.md#yaml-configuration-and-ide-completion) for the YAML
format, editor association, and parser safety limits.

You can adjust configuration parameters as applicable to your project.

The plugin will run during `validate` phase (very first in lifecycle) of Maven.
The validation report will either be generated in maven log for console format or written to a json file inside `outputDirectory`.

### External Rule Dependencies
You can extend the core linter library for adding components or rules by implementing `mule-linter-spi`.
When you have an external components or rules provider, you can add those as a dependency to plugin.

For example, `mule-linter-spi-test` module implements an extension and adds a new `HttpListenerPathRule`. 
Adding this library to plugin to use new rule, we can configure it like below - 


```xml

<plugin>
    <groupId>com.avioconsulting.mule</groupId>
    <artifactId>mule-linter-maven-plugin</artifactId>
    <version>LATEST_VERSION</version>
    <executions>
        <execution>
            <id>validate</id>
            <phase>validate</phase>
            <goals>
                <goal>validate</goal>
            </goals>
            <configuration>
                <appDir>${basedir}</appDir>
                <ruleConfiguration>muleLinter.groovy</ruleConfiguration>
                <outputDirectory>${project.build.directory}/reports</outputDirectory>
                <formats>
                    <format>CONSOLE</format>
                    <format>JSON</format>
                </formats>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <!-- Plugin dependencies for external rules/components go here -->
        <dependency>
            <groupId>com.avioconsulting.mule</groupId>
            <artifactId>mule-linter-spi-test</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</plugin>
```

Once you add required dependencies, you can add new rule in the rules configuration file - 

```groovy
    MULE_ARTIFACT_SECURE_PROPERTIES {
        properties = [
                'anypoint.platform.db.password'
        ]
        includeDefaults = false
    }
```
### Anypoint Code Builder compatibility

Add in the <properties> section ( this is required to be compatible with ACB Munits in the IDE )

```xml
<mule.linter.appDir>${project.basedir}</mule.linter.appDir>
<mule.linter.config>${project.basedir}/linter.groovy</mule.linter.config>
```

Reference the properties in the plugin configuration:
```xml
<plugin>
    <groupId>com.avioconsulting.mule</groupId>
    <artifactId>mule-linter-maven-plugin</artifactId>
    <version>LATEST_VERSION</version>
    <executions>
        <execution>
            <id>validate</id>
            <phase>validate</phase>
            <goals>
                <goal>validate</goal>
            </goals>
            <configuration>
                <appDir>${mule.linter.appDir}</appDir>
                <ruleConfiguration>${mule.linter.config}</ruleConfiguration>
                <outputDirectory>${project.build.directory}/reports</outputDirectory>
                <formats>
                    <format>CONSOLE</format>
                    <format>JSON</format>
                </formats>
            </configuration>
        </execution>
    </executions>    
</plugin>
```
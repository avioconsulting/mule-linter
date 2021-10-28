# Mule Linter Maven Plugin

The mule linter can be run as mvn plugin. The maven plugin is located in module `mule-linter-maven-plugin`.

## Parameters
- appDir: Defaults to `${basedir}`
- ruleConfiguration: Defaults to `${basedir}/muleLinter.groovy`
- outputDirectory: Defaults to `${project.build.directory}/mule-linter`
- format: Defaults to CONSOLE. Options - CONSOLE, JSON
- failBuild: Defaults to false. If `failBuild` is set to true, then rule violations with severity higher than MINOR will cause build to fail.

## Usage
To use plugin in your maven project, add following plugin configuratin in project pom -

```xml

<plugin>
    <groupId>com.avioconsulting.mule</groupId>
    <artifactId>mule-linter-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>validate</id>
            <phase>validate</phase>
            <goals>
                <goal>validate</goal>
            </goals>
            <configuration>
                <appDir>${basedir}</appDir>
                <ruleConfiguration>${basedir}/AVIOCustomRuleConfiguration.groovy</ruleConfiguration>
                <outputDirectory>${project.build.directory}/reports</outputDirectory>
                <format>JSON</format>
            </configuration>
        </execution>
    </executions>
</plugin>
```
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
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>validate</id>
            <phase>validate</phase>
            <goals>
                <goal>validate</goal>
            </goals>
            <configuration>
                <appDir>${basedir}</appDir>
                <ruleConfiguration>${basedir}/AVIOCustomRuleConfiguration.groovy</ruleConfiguration>
                <outputDirectory>${project.build.directory}/reports</outputDirectory>
                <format>JSON</format>
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
    rules.addRule(new com.avioconsulting.mule.linter.extension.rules.HttpListenerPathRule())
```

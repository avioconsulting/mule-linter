# Mule Linter Maven Plugin

The mule linter can be run as mvn plugin. The maven plugin is located in module `mule-linter-maven-plugin`.

Plugin goal is by default attached to validate which is the first phase of maven lifecycle.

## Parameters
- appDir: Defaults to `${basedir}`
- ruleConfiguration: Defaults to `${basedir}/muleLinter.groovy`
- outputDirectory: Defaults to `${project.build.directory}/mule-linter`
- formats: Defaults to CONSOLE and JSON.
- failBuild: Defaults to false. If `failBuild` is set to true, then rule violations with severity higher than MINOR will cause build to fail.

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
                <systemPropertyVariables>
                    <!-- Allows plugin to use the Maven version we are running with  -->
                    <maven.home>${maven.home}</maven.home>
                </systemPropertyVariables>
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
</plugin>
```
Mule linter plugin generates effective-pom file for the application using [maven-invoker](https://maven.apache.org/shared/maven-invoker/), and linter uses effective-pom.xml for executing the linter rulesets. 

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

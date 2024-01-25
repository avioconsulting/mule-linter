# Mule Linter Tool
A linter is a tool that analyzes source code looking for patterns that don’t follow convention.  Linting helps prevent errors and improve the overall quality of the code by following best practices.  Lint tools are a form of static code analyzers.  Some common code analyzers for Java are Checkstyle, FindBugs, and PMD.

The Mule Linter will enforce that all Mule projects are developed with a baseline set of rules.  Some basic examples of rules that will be enforced, are the proper usage of property and pom files, useful logging messages, and standard project structure.

## Usage

### Maven Plugin
See [Readme](mule-linter-maven-plugin/README.md) in `mule-linter-maven-plugin` module.

### CLI

Project uses Gradle build system. Run following command to build all components in local -

```shell
./gradlew build
```

The CLI distributions are generated in `./mule-linter-cli/build/distributions/`. 
Unzip/Untar the distribution. You can run the CLI from expanded files - 

```shell
./bin/mule-linter-cli
```

You may move expanded distribution folder to other persistent location and add it on OS PATH, 
and then run cli from anywhere on the system.

## Build

When cloning add the 'recurse-submodules' flag

```git clone --recurse-submodules```

After cloning, update the submodules

```git submodule update --remote```

To build the project run - 

`./gradlew build`

Generated Distribution and install in local - 

`./gradlew installDist`

## Release

To release this module, follow these steps - 
1. Create a new branch from `main` with naming convention - `release/x.y.z` eg. `release/1.1.0`
2. Run one of the following command -
* Release current snapshot - `./gradlew -Dversion.prerelease=`
* Release next minor non-snapshot - `./gradlew -Dversion.prerelease= incrementMinor`
3. Commit the modified `version.properties` modified by above command
4. Create PR to main
5. Once approved, JReleaser will release it to maven central


## Rule Configuration

Rule Configuration uses a Groovy-DSL provided by Mule Linter. See [AVIOGDSLRuleConfiguration.groovy](mule-linter-core/AVIOGDSLRuleConfiguration.groovy) for sample configuration.

Mule Linter Core is shipped with many rules. You can browse subpackages under `com.avioconsulting.mule.linter.rule` in https://avioconsulting.github.io/mule-linter/groovydoc/index.html.

Mule linter generates effective-pom.xml for the application using [maven-invoker](https://maven.apache.org/shared/maven-invoker/), and linter uses effective-pom.xml for executing the linter rulesets.
Also, this requires Maven home location which can be passed using below options:
- Pass maven.home system variable when executing mule-linter 
- Set MAVEN_HOME environment variable in the system executing mule-linter

### Using IntelliJ Auto Completion
Mule Linter's core library contains the GDSL file to support autocompletion in IntelliJ. To use that feature, `com.avioconsulting.mule:mule-linter-core`  dependency must be added with `provided`  scope in the project. `provided` scope will avoid maven packaging core into project artifact but still allow IntelliJ to detect the GDSL script from classpath.

## Extending for Mule Linter

Mule Linter provides a service provider interface (SPI) based mechanism to add custom rules and components. See SPI [readme](./mule-linter-spi/readme.md) for details on how to use it. A Sample extension can be seen in [mule-linter-spi-test](./mule-linter-spi-test) module. 

## Mule Application Design
![Mermaid Design](config/mermaid/mule-application-diagram.png)

### Updating Mermaid Diagram
[Mermaid Live Editor](https://mermaid-js.github.io/mermaid-live-editor/edit#pako:eNrVWFtT6zYQ_isePyWdwCQcOECGYSalhoaT2ySUnulkhhGxEnRqS64lU9KU_17dLEuyE3gtD1ja_by72pvW2YUrEsOwH64SQOkvCGxykC7xksl9MMiyBK0AQwTvljjQf7cogQGoWDPAXiruCFF2NctJBnO2FdDrILN21EPeELxGmyKXkhR85ZOsd2Ykleoz9awY9xD_iTCVzB_VugIsWI7wJsAgtYh3iA03mORQvrdBDJldBRoXCRzkDK3BigWptakgCQGxfWbaarvM2jFrCFuLzVsjzN9OM4IhZrS14kuxethmsCNPQzOwgm3lTSHEgK8rIRvIbhPyN1eqcGKzB7conj8LHSTJZ6GCNYdrA91n6AuQYWuJg7WDnwlJIMCOJM_NDfnmwJscvy_znBfdeDhJ4NqjMpFDZn5Ocq5Oy7VC3NuJqXDv6mFVWrAMe8swODq65quf-Iof7gdcsUq2Kk6LvPsgzTWxkqC1WiKCq3-PjtwjaDW64naiLSyZFjWbjp--j0cubT6dRfOHYbRQZOGy72kyAzmFeZDJh-LEBNLojQehZcVYMIQ_eTfhZCWzopK0jHGrbCcTmSOcEyUw5WlUgRUAQTpCGE4IFzfEDG5K9QKSFBuEW5ucFNkw7gRAh3YYS4mKbdDjAqPyFZEKfDcGrxCXsIMutVLTC5-hB1YAfwX0hYHnBF5Nn4WkTqCepo0iux9ajQe5PcXxA7d6r9zG6pJOVg4uI9GAuiEFZrZ3DznCrqB7SrDtDJ_nZdv4t1H0NOCpdTu4eXi6X0wnJjQLuCpy6JxUCBjkOdhW8UNYaHiEOeUFpiFehs3hXwXKofBmXMiSbwDxMhVO2SchhllCtiIXIyxcHWugn-Oq9TTbuifNVbAOe9jcZrZrDdHz6d3wYXg3mc4jRRWJFL1lOaTCRU4m8fuYAd62WtDwvda8z6B7_yJWJtl3tWvUfTT5NpwsbocjadZ72SVrkmvN25ZfYzZk0w0_-dNkMI4Ws8FN5PCjUTSOJg9Pt6Pp7_Po9tPNjPrdDMTxII6RsAIkdwl5BokyrYUJtvdthRdXvU3ee1uW4Ikr5kO8NUccQPIknEDKYAX_SO6IbHj51-Ae-f82j5jsq-VT7Yp2Xvcbm2FYXX4MsiuVbh3dYK8DwPjiuWB2f-fz0KAkP4KkODQZNQIburexR7eyBoi8N4v0GeZ-dy9lvKAkziH-wH_l-GxYqnqdwNjuchi6arkLhAosbXHqC9FBhr4hGcxWc0tqUO5lpa3eY-3cgZC3PrDZ47AbwH1E8m0z94Cj4StMPJY23TNGGT94HE4PHKCBvfNin-cwkYk8jJtN0uc02USbzTNTUnm7Nw1FZRG4vLLpc2XV15cpRV0OTZD6OFfWKTPDqqtCt3Q95zm0auRzyK9qSFA0rSdIpGJFq32CCuKdGFvnkBYJCzJpAb8svPv-MwOsvvRYRZRdxpnL67xqXDVuKBGVq70kFFSqqbJVGKBqHzWkIr-K_xW2ecCmhqEc1zaX-dJJjDmvzuiNT3CM-9hKVEFfQBbkhfPxDxVUcJ0BJUY0S8BWuZ-2SMGyQoQTgrTttXJHo93FxUqr9S0Ulmg3ygQVtOvSNn3PC1pLkCrXaDOD6h3jHMWSEtpOUZXqlmHXuV8E3Ter_tEnBIpsbiBPnO9BpYZnOmLbgOpFzc-27Y-IqK5xXbNXmulgfEsNwwuxtKwiOcUmL5_aUcR39KTp0zZVTatunOqaiwyu0BqtfD86DBnkUkDYCVOYpwDFYT-Udi9D9sJrahn2-TKGa8CTbRku8TuHgoKRxRavwv4aJBR2wiKL-Y2gf1DzqBEfCUluiBnAfxDCQSwv-BZK7lj_LCceEhL2d-Fb2D_rHp-eX573ul_PT7_0Ts7OO-E27B-d9E6OLy965ydfu5e9i4svZ--d8B8ptHd8ctE9u-ien112Ty9Ov568_wfp9MoY)
* Update code in [mule-application-design.mmd](config/mermaid/mule-application-design.mmd) and paste into live editor
* Click 'Download PNG' and save file into [config/mermaid](config/mermaid) directory
## Code Coverage
[CodeNarc](https://codenarc.github.io/CodeNarc/) is used to ensure quality in groovy code.  The configuration file is located [here.](config/code-quality-config/codenarc/codenarc.xml)  To execute run ```gradle check```, and an output [report](build/reports/codenarc/main.html) will be generated. 

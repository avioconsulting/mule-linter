#Mule Linter Tool
A linter is a tool that analyzes source code looking for patterns that don’t follow convention.  Linting helps prevent errors and improve the overall quality of the code by following best practices.  Lint tools are a form of static code analyzers.  Some common code analyzers for Java are Checkstyle, FindBugs, and PMD.

The Mule Linter will enforce that all Mule projects are developed with a baseline set of rules.  Some basic examples of rules that will be enforced, are the proper usage of property and pom files, useful logging messages, and standard project structure.

## Code Checkout
When cloning add the 'recurse-submodules' flag

```git clone --recurse-submodules```

After cloning, update the submodules

```git submodule update --remote```

## Mule Application Design
![Mermaid Design](config/mermaid/mule-application-diagram.png)


## Code Coverage
[CodeNarc](https://codenarc.github.io/CodeNarc/) is used to ensure quality in groovy code.  The configuration file is located [here.](config/codenarc/codenarc.xml)  To execute run ```gradle check```, and an output [report](build/reports/codenarc/main.html) will be generated. 
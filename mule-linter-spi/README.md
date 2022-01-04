# Mule Linter Service Provider Interface

Mule Linter contains two configurable components for core that other libraries can extend.

 * **Mule Components:** Core Library supports identifying Flow/Subflow and Logger components with attributes specific to those. All other components are just treated as generic MuleComponents.

 * **Rule:** Rules are the core of linter processing. Core library is shipped with many useful rules.

Mule linter provides a service provider interface (SPI) library to allow extending these two components.

To extend the mule linter capabilities, add following dependency to your custom library project
```xml
// For Gradle based builds
implementation 'com.avioconsulting.mule:mule-linter-spi:${mule-linter-version}'


// For Maven based builds
<dependency>
	<groupId>com.avioconsulting.mule</groupId>
	<artifactId>mule-linter-spi</artifactId>
	<version>${mule-linter-version}</version>
</dependency>
```

## Adding Mule Components
Mule linter is shipped with three components:

 * MuleComponent: The base of all components and used as a generic component
 * FlowComponent: Identifies the instances of Flow or Subflows
 * LoggerComponent: Identifies the mule logger component instances


### Component Identifiers
All components have a ComponentIdentifier that contains name and namespaceURI of that component. For example, below is the definition of HTTP Listener Component Identifier -

`ComponentIdentifier IDENTIFIER_LISTENER = new ComponentIdentifier("listener", "http://www.mulesoft.org/schema/mule/http")`

### Custom Components
Components are loaded using Java ServiceLoader mechanism. Example implementation is mule-linter-spi-test module.

 1. Add mule-linter-spi dependency to your project.
 2. Creating new components (See example of HTTP Component in mule-linter-spi-test module)
    - Add new Components in your project by extending MuleComponent class from SPI library.
    - It is advised to add a static method static boolean accepts(ComponentIdentifier identifier) on the component. This should validate the accepted identifiers.  This is not needed by SPI but can help you cleanly identify components in the factory. 
    - Add component specific methods

 3. Making components discoverable for Service loader (See example of AVIOComponentsFactory.groovy in mule-linter-spi-test module) -
 
    - Create a Factory class implementing com.avioconsulting.mule.linter.spi.ComponentsFactory.
    - Core library calls following method so implement these as applicable to your library -
      - hasComponentFor  - to define which Component’s are handled by this factory and instantiate them 
      - getComponentFor - to create and return the component for provided method arguments.      
    - Create a directory META-INF/services in your src/main/resources
    - Add a text file named same as SPI com.avioconsulting.mule.linter.spi.ComponentsFactory. The content of the file should be the fully qualified class name for your factory. Following is an example content from SPI test module.

````
❯ cat src/main/resources/META-INF/services/com.avioconsulting.mule.linter.spi.ComponentsFactory
com.avioconsulting.mule.linter.extension.components.AVIOComponentsFactory%  
````
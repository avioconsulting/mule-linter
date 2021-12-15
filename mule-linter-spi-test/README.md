# Mule Linter Extension Test
This is a test extension to validate mule-linter-spi module for extending core capabilities.

This uses `mule-linter-spi` to add following features - 

- Add HttpComponent to handle Http Listener component
- Add Components factory and configure services in META-INF for loader
- Add new Rule for validating Http Listener Path

This project includes tests to verify newly extended component and rule.
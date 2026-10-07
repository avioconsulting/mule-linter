# Global Configuration Layout

Use these rules together instead of the legacy single-filename `GLOBAL_CONFIG_EXISTS`
and `GLOBAL_CONFIG_NO_FLOWS` rules. Legacy rules remain available unchanged.

```groovy
mule_linter {
    rules {
        GLOBAL_CONFIG_SEPARATION {}
        GLOBAL_FILES_NO_FLOWS {
            exceptions = ['global/health-check.xml', 'global/global-error-handler.xml']
        }
    }
}
```

`GLOBAL_CONFIG_SEPARATION` rejects files that contain both global configuration
and flows or subflows, regardless of filename. Configuration may span multiple
files. Named core error-handler definitions are not global configuration or flows.

`GLOBAL_FILES_NO_FLOWS` rejects flows and subflows in `global/**/*.xml`, root
`global.xml`, and root `global-config.xml`. No particular file must exist.
Its optional `patterns` list replaces the defaults with full-match regular
expressions against paths relative to `src/main/mule`, using forward slashes.
The defaults are `global/.*\.xml`, `global\.xml`, and `global-config\.xml`.

Each rule has an independent `exceptions` list of exact relative paths, not
basenames or globs. A placement exception permits shared framework processing
in that global file; it does not permit mixing configuration with processing.
Keep exceptions narrow and explain their purpose in the project ruleset.

Discover existing property loaders, secure-property configurations, listeners,
connectors and handler references throughout `src/main/mule/**/*.xml`. Preserve
compliant existing layouts instead of moving files just to match an example.

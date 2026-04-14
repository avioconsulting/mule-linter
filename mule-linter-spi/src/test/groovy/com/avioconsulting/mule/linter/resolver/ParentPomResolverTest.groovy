package com.avioconsulting.mule.linter.resolver

import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Tests for ParentPomResolver system property handling.
 * Stepwise execution to avoid system property pollution between tests.
 */
@Stepwise
class ParentPomResolverTest extends Specification {

    private static final String CUSTOM_REPO_PATH = '/tmp/custom-m2-repo'
    private static final String DEFAULT_REPO_PATH = "${System.getProperty('user.home')}/.m2/repository"

    def setup() {
        // Clean up any system property from previous tests
        System.clearProperty('mule.linter.localRepo')
    }

    def cleanup() {
        // Always clean up after each test
        System.clearProperty('mule.linter.localRepo')
    }

    def "Default local repository path is used when no system property set"() {
        given: "No system property is set"
        System.clearProperty('mule.linter.localRepo')

        when: "Creating a ParentPomResolver"
        ParentPomResolver resolver = new ParentPomResolver()

        then: "Default path is used"
        resolver.localRepositoryDir.absolutePath == new File(DEFAULT_REPO_PATH).absolutePath

        cleanup:
        resolver?.close()
    }

    def "System property mule.linter.localRepo is used when set"() {
        given: "System property is set to custom path"
        System.setProperty('mule.linter.localRepo', CUSTOM_REPO_PATH)

        when: "Creating a ParentPomResolver"
        ParentPomResolver resolver = new ParentPomResolver()

        then: "System property path is used"
        resolver.localRepositoryDir.absolutePath == new File(CUSTOM_REPO_PATH).absolutePath

        cleanup:
        resolver?.close()
        System.clearProperty('mule.linter.localRepo')
    }

    def "Explicit constructor parameter overrides system property"() {
        given: "System property is set but explicit parameter provided"
        System.setProperty('mule.linter.localRepo', CUSTOM_REPO_PATH)
        String explicitPath = '/tmp/explicit-repo'

        when: "Creating a ParentPomResolver with explicit path"
        ParentPomResolver resolver = new ParentPomResolver(explicitPath)

        then: "Explicit parameter wins over system property"
        resolver.localRepositoryDir.absolutePath == new File(explicitPath).absolutePath

        cleanup:
        resolver?.close()
        System.clearProperty('mule.linter.localRepo')
    }

    def "System property overrides default when no explicit parameter"() {
        given: "Only system property is set"
        System.setProperty('mule.linter.localRepo', CUSTOM_REPO_PATH)

        when: "Creating a ParentPomResolver with no parameter"
        ParentPomResolver resolver = new ParentPomResolver()

        then: "System property is used instead of default"
        resolver.localRepositoryDir.absolutePath == new File(CUSTOM_REPO_PATH).absolutePath
        resolver.localRepositoryDir.absolutePath != new File(DEFAULT_REPO_PATH).absolutePath

        cleanup:
        resolver?.close()
        System.clearProperty('mule.linter.localRepo')
    }

    def "Null explicit parameter falls back to system property then default"() {
        given: "System property is set"
        System.setProperty('mule.linter.localRepo', CUSTOM_REPO_PATH)

        when: "Creating a ParentPomResolver with explicit null"
        ParentPomResolver resolver = new ParentPomResolver(null)

        then: "System property is used"
        resolver.localRepositoryDir.absolutePath == new File(CUSTOM_REPO_PATH).absolutePath

        cleanup:
        resolver?.close()
        System.clearProperty('mule.linter.localRepo')
    }
}

package com.avioconsulting.mule.linter.resolver

import com.sun.net.httpserver.HttpServer
import org.apache.maven.settings.Server
import org.codehaus.plexus.util.xml.Xpp3Dom
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.resolution.ArtifactRequest
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.CopyOnWriteArrayList

class Resolver2CompatibilityTest extends Specification {
    @TempDir File directory

    def 'scoped resolvers are reused within a goal and isolated between goals'() {
        given:
        def firstScope = ParentPomResolver.openScope()
        def first = ParentPomResolver.instance

        expect:
        first.is(ParentPomResolver.instance)
        first.@session instanceof RepositorySystemSession.CloseableSession

        when:
        firstScope.close()
        firstScope.close()
        def secondScope = ParentPomResolver.openScope()
        def second = ParentPomResolver.instance

        then:
        !first.is(second)
        second.is(ParentPomResolver.instance)

        cleanup:
        secondScope?.close()
        firstScope?.close()
    }

    def 'nested scopes restore their outer resolver'() {
        given:
        def outerScope = ParentPomResolver.openScope()
        def outer = ParentPomResolver.instance
        def innerScope = ParentPomResolver.openScope()
        def inner = ParentPomResolver.instance

        when:
        innerScope.close()

        then:
        !inner.is(outer)
        ParentPomResolver.instance.is(outer)

        cleanup:
        innerScope?.close()
        outerScope?.close()
    }

    def 'scopes reject out-of-order closure without losing the current scope'() {
        given:
        def outer = ParentPomResolver.openScope()
        def inner = ParentPomResolver.openScope()
        def resolver = ParentPomResolver.instance

        when:
        outer.close()

        then:
        thrown(IllegalStateException)
        ParentPomResolver.instance.is(resolver)

        cleanup:
        inner.close()
        outer.close()
    }

    def 'Apache transport uses settings authentication and caches a remote POM'() {
        given:
        byte[] pom = '<project><modelVersion>4.0.0</modelVersion><groupId>test</groupId><artifactId>parent</artifactId><version>1</version></project>'.getBytes(StandardCharsets.UTF_8)
        String sha1 = MessageDigest.getInstance('SHA-1').digest(pom).encodeHex().toString()
        String authorization = 'Basic ' + 'fixture:secret'.getBytes(StandardCharsets.UTF_8).encodeBase64().toString()
        def requests = new CopyOnWriteArrayList<String>()
        def server = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        server.createContext('/') { exchange ->
            try {
                if (exchange.requestHeaders.getFirst('Authorization') != authorization) {
                    exchange.responseHeaders.set('WWW-Authenticate', 'Basic realm="fixture"')
                    exchange.sendResponseHeaders(401, -1)
                    return
                }
                requests.add(exchange.requestURI.path)
                byte[] body = exchange.requestURI.path.endsWith('.sha1') ? sha1.getBytes(StandardCharsets.UTF_8) : pom
                exchange.sendResponseHeaders(200, body.length)
                exchange.responseBody.write(body)
            } finally {
                exchange.close()
            }
        }
        server.start()
        def resolver = new ParentPomResolver(new File(directory, 'repository').path)
        def configuration = new Xpp3Dom('configuration')
        def url = new Xpp3Dom('url')
        url.value = "http://127.0.0.1:${server.address.port}/"
        configuration.addChild(url)
        resolver.@settings.servers = [new Server(id: 'fixture', username: 'fixture', password: 'secret', configuration: configuration)]
        def method = ParentPomResolver.getDeclaredMethod('buildRemoteRepositories', List)
        method.accessible = true
        List<String> attempted = []
        def repository = method.invoke(resolver, attempted).find { it.id == 'fixture' }
        def request = new ArtifactRequest(new DefaultArtifact('test', 'parent', 'pom', '1'), [repository], null)

        when:
        def result = resolver.@repositorySystem.resolveArtifact(resolver.@session, request)
        server.stop(0)
        def cached = resolver.resolve('test', 'parent', '1', null, directory)

        then:
        result.resolved
        result.artifact.file.bytes == pom
        cached.bytes == pom
        requests.contains('/test/parent/1/parent-1.pom')
        attempted.contains(url.value)

        cleanup:
        resolver?.close()
        server?.stop(0)
    }
}

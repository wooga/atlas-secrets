/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.secrets

import nebula.test.ProjectSpec
import wooga.gradle.secrets.internal.DefaultSecret

class SecretsPluginExtensionSpec extends ProjectSpec {
    public static final String PLUGIN_NAME = 'net.wooga.secrets'

    SecretsPluginExtension subjectUnderTest
    def resolver = Mock(SecretResolver)

    def setup() {
        project.plugins.apply(PLUGIN_NAME)
        subjectUnderTest = project.extensions.findByName('secrets') as SecretsPluginExtension
        subjectUnderTest.secretResolverChain.add(resolver)
    }

    def "secretValue resolves secrets wrapped in Provider<String>"() {
        given: "a mock resolver"
        resolver.resolve(secretId) >> new DefaultSecret(expectedValue)

        when:
        def secret = subjectUnderTest.secretValue(secretId)

        then:
        noExceptionThrown()
        secret != null
        secret.present
        secret.get() == expectedValue

        where:
        secretId      | expectedValue
        "some_secret" | "some secret value"
    }

    def "secretFileAsBytes resolves secrets wrapped in Provider<byte[]>"() {
        given: "a mock resolver"
        resolver.resolve(secretId) >> new DefaultSecret(expectedValue)

        when:
        def secret = subjectUnderTest.secretFileAsBytes(secretId)

        then:
        noExceptionThrown()
        secret != null
        secret.present
        secret.get() == expectedValue

        where:
        secretId      | expectedValue
        "some_secret" | "some secret value".bytes
    }

    def "secretFile resolves secrets wrapped in Provider<RegularFile>"() {
        given: "a mock resolver"
        resolver.resolve(secretId) >> new DefaultSecret(expectedValue)

        when:
        def secret = subjectUnderTest.secretFile(secretId)

        then:
        noExceptionThrown()
        secret != null
        secret.present
        secret.get().asFile.getBytes() == expectedValue

        where:
        secretId      | expectedValue
        "some_secret" | "some secret value".bytes
    }

    def "secretValue throws no exceptions when secret can not be found"() {
        given: "a mock resolver"
        resolver.resolve(_) >> { secretId -> throw new SecretResolverException("Unable to resolve secret with id ${secretId}") }

        when:
        def present = subjectUnderTest.secretValue("some secret").isPresent()

        then:
        noExceptionThrown()
        !present
    }

    def "secretValue throws no exceptions when secret is of wrong type"() {
        given: "a mock resolver"
        def resolver = Mock(SecretResolver)
        resolver.resolve(_) >> new DefaultSecret<byte[]>("some value".bytes)

        when:
        def present = subjectUnderTest.secretValue("some secret").isPresent()

        then:
        noExceptionThrown()
        !present
    }

    def "secretFileAsBytes throws no exceptions when secret can not be found"() {
        given: "a mock resolver"
        resolver.resolve(_) >> { secretId -> throw new SecretResolverException("Unable to resolve secret with id ${secretId}") }

        when:
        def present = subjectUnderTest.secretFileAsBytes("some secret").isPresent()

        then:
        noExceptionThrown()
        !present
    }

    def "secretFileAsBytes throws no exceptions when secret is of wrong type"() {
        given: "a mock resolver"
        resolver.resolve(_) >> new DefaultSecret<String>("some value")

        when:
        def present = subjectUnderTest.secretFileAsBytes("some secret").isPresent()

        then:
        noExceptionThrown()
        !present
    }

    def "secretFile throws no exceptions when secret can not be found"() {
        given: "a mock resolver"
        resolver.resolve(_) >> { secretId -> throw new SecretResolverException("Unable to resolve secret with id ${secretId}") }

        when:
        subjectUnderTest.secretFile("some secret").isPresent()

        then:
        noExceptionThrown()
    }

    def "secretFile throws no exceptions when secret is of wrong type"() {
        given: "a mock resolver"
        resolver.resolve(_) >> new DefaultSecret<String>("some value")

        when:
        def present = subjectUnderTest.secretFile("some secret").isPresent()

        then:
        noExceptionThrown()
        !present
    }
}

/*
 * Copyright 2020 Wooga GmbH
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

package wooga.gradle.secrets.internal

import org.apache.commons.lang3.RandomStringUtils
import wooga.gradle.secrets.Secret
import wooga.gradle.secrets.SecretResolver
import wooga.gradle.secrets.SecretResolverException

class SecretResolverChainSpec extends SecretsResolverSpec<SecretResolverChain> {

    SecretResolverChain resolverChain

    private class TestSecretResolver implements SecretResolver {
        @Override
        Secret resolve(String secretId) {
            throw new SecretResolverException("Unable to resolve secret with id ${secretId}")
        }
    }

    def setup() {
        resolverChain = new SecretResolverChain()
        resolverChain.add(new TestSecretResolver())
    }

    def "returns the first non null secret it finds in chained resolvers"() {
        given: "initial empty resolver chain"
        resolverChain.clear()

        and: "a resolver that throws an exception"
        resolverChain.add(new TestSecretResolver())

        and: "a resolver that returns null"
        def nullResolver = Mock(SecretResolver)
        nullResolver.resolve(secretId) >> null
        resolverChain.add(nullResolver)

        and: "a resolver that returns a value"
        def valueResolver = Mock(SecretResolver)
        valueResolver.resolve(secretId) >> new DefaultSecret("some secret")
        resolverChain.add(valueResolver)

        and: "and a second resolver that returns a value"
        valueResolver = Mock(SecretResolver)
        valueResolver.resolve(secretId) >> new DefaultSecret("another secret")
        resolverChain.add(valueResolver)

        expect:
        resolverChain.resolve(secretId).secretValue == "some secret"

        where:
        secretId = "some_secret_${RandomStringUtils.randomAlphabetic(20)}"
    }

    def "fails when no resolver is configured"() {
        given: "empty resolver chain"
        resolverChain.clear()

        when:
        resolverChain.resolve("someSecret")

        then:
        def e = thrown(SecretResolverException)
        e.message == "No secret resolvers configured."
    }

    def "can append resolvers"() {
        given: "empty resolver chain"
        resolverChain.clear()

        when:
        resolverChain.add(Mock(SecretResolver))

        then:
        resolverChain.size() == 1

        when:
        resolverChain.addAll([Mock(SecretResolver), Mock(SecretResolver)])

        then:
        resolverChain.size() == 3

        when:
        resolverChain.addAll(Mock(SecretResolver), Mock(SecretResolver))

        then:
        resolverChain.size() == 5
    }

    def "can set resolver list"() {
        given: "empty resolver chain"
        resolverChain.addAll(Mock(SecretResolver), Mock(SecretResolver))
        assert resolverChain.size() == 3

        when:
        resolverChain.resolverChain = Mock(SecretResolver)

        then:
        resolverChain.size() == 1
    }

    @Override
    SecretResolverChain getSubject() {
        return resolverChain
    }

    @Override
    void createSecret(String secretId, byte[] secretValue) {
        //create fake resolver for given secret
        def resolver = Mock(SecretResolver)
        resolver.resolve(secretId) >> new DefaultSecret(secretValue)
        resolverChain.add(resolver)
    }

    @Override
    void createSecret(String secretId, String secretValue) {
        //create fake resolver for given secret
        def resolver = Mock(SecretResolver)
        resolver.resolve(secretId) >> new DefaultSecret(secretValue)
        resolverChain.add(resolver)
    }

    @Override
    void deleteSecret(String secretId) {
        resolverChain.resolverChain = []
    }
}

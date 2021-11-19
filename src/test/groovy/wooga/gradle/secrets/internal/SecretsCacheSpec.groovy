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

package wooga.gradle.secrets.internal

import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Shared

class SecretsCacheSpec extends SecretsResolverSpec<SecretsCache> {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @Shared
    def cacheTimeout = 5

    @Override
    SecretsCache getSubject() {
        new SecretsCache(new EnvironmentResolver(), cacheTimeout)
    }

    @Override
    void createSecret(String secretId, byte[] secretValue) {
        def f = File.createTempFile(secretId, "secret")
        f.bytes = secretValue
        f.deleteOnExit()

        environmentVariables.set(secretId.toUpperCase(), f.path)
    }

    @Override
    void createSecret(String secretId, String secretValue) {
        environmentVariables.set(secretId.toUpperCase(), secretValue)
    }

    @Override
    void deleteSecret(String secretId) {
        def v = System.getenv(secretId)
        if (v && new File(v).exists()) {
            new File(v).delete()
        }
        environmentVariables.clear(secretId.toUpperCase())
    }

    def "resolved caches the secret"() {
        given: "a secret value"
        createSecret("test.secret", "value1")

        and: "a resolver"
        def resolver = getSubject()

        when:
        def result = resolver.resolve(secretId)

        then:
        result.secretValue == "value1"

        when: "updating the secret"
        deleteSecret(secretId)
        createSecret(secretId, "value2")
        result = resolver.resolve(secretId)

        then:
        result.secretValue == "value1"

        when: "waiting for timeout"
        sleep((cacheTimeout + 1) * 1000)
        result = resolver.resolve(secretId)

        then:
        result.secretValue == "value2"

        where:
        secretId = "test.secret"

    }
}

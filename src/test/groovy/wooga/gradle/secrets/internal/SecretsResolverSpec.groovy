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
import spock.lang.Specification
import spock.lang.Unroll
import wooga.gradle.secrets.SecretResolver
import wooga.gradle.secrets.SecretResolverException

abstract class SecretsResolverSpec<T extends SecretResolver> extends Specification {

    abstract T getSubject()

    @Unroll("can resolve secret #type")
    def "can resolve secret"() {
        given: "a secret text on AWS"
        createSecret(secretId, secretValue)

        when:
        def result = subject.resolve(secretId)

        then:
        result != null
        expectedType.isAssignableFrom(result.secretValue.class)
        result.secretValue == secretValue

        cleanup:
        deleteSecret(secretId)

        where:
        secretValue                  | expectedType | type
        "a random secret".toString() | String       | "text"
        "a random secret".bytes      | byte[]       | "file"
        secretId = "wdk_unified_build_system_testSecret_${RandomStringUtils.randomAlphabetic(20)}"
    }

    def "fails when secret can't be found"() {
        when:
        subject.resolve(secretId)

        then:
        def e = thrown(SecretResolverException.class)
        e.message == "Unable to resolve secret with id ${secretId}"

        where:
        secretId = "wdk_unified_build_system_testSecret_${RandomStringUtils.randomAlphabetic(20)}"
    }

    abstract void createSecret(String secretId, byte[] secretValue)

    abstract void createSecret(String secretId, String secretValue)

    abstract void deleteSecret(String secretId)
}

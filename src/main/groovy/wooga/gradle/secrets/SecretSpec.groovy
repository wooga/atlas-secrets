/*
 * Copyright 2020-2021 Wooga GmbH
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

import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input

import javax.crypto.spec.SecretKeySpec

trait SecretSpec extends BaseSpec {
    private final Property<SecretKeySpec> secretsKey = objects.property(SecretKeySpec)

    @Input
    Property<SecretKeySpec> getSecretsKey() {
        secretsKey
    }

    void setSecretsKey(Provider<SecretKeySpec> value) {
        secretsKey.set(value)
    }

    void setSecretsKey(SecretKeySpec value) {
        secretsKey.set(value)
    }

    void setSecretsKey(File keyFile) {
        setSecretsKey(new SecretKeySpec(keyFile.bytes, "AES"))
    }

    private final Property<SecretResolver> secretResolver = objects.property(SecretResolver)

    @Input
    Property<SecretResolver> getSecretResolver() {
        secretResolver
    }

    void setSecretResolver(Provider<SecretResolver> value) {
        secretResolver.set(value)
    }

    void setSecretResolver(SecretResolver value) {
        secretResolver.set(value)
    }
}

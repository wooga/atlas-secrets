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

import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

class EnvironmentResolverSpec extends SecretsResolverSpec<EnvironmentResolver> {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @Override
    EnvironmentResolver getSubject() {
        new EnvironmentResolver()
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
}

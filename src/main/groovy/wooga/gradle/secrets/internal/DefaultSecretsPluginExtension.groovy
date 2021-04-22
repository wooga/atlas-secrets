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
import org.gradle.api.Project
import org.gradle.api.provider.Property
import wooga.gradle.secrets.SecretResolver
import wooga.gradle.secrets.SecretsConsts
import wooga.gradle.secrets.SecretsPluginExtension

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.security.spec.KeySpec

class DefaultSecretsPluginExtension implements SecretsPluginExtension {
    protected final Project project

    final Property<SecretKeySpec> secretsKey
    final Property<SecretResolver> secretResolver

    DefaultSecretsPluginExtension(Project project) {
        this.project = project
        secretsKey = project.objects.property(SecretKeySpec.class)

        secretsKey.set(project.provider({
            String keyPath = System.getenv().get(SecretsConsts.SECRETS_KEY_ENV_VAR) ?:
                    project.properties.get(SecretsConsts.SECRETS_KEY_OPTION, null)

            if (keyPath) {
                return new SecretKeySpec(new File(keyPath).bytes, "AES")
            }

            KeySpec spec = new PBEKeySpec(secretsKeyPassword().chars, secretsKeySalt(), SecretsConsts.SECRETS_KEY_ITERATION, SecretsConsts.SECRETS_KEY_LENGTH);
            // AES-256
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] key = f.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, "AES");
        }.memoize()))

        secretResolver = project.objects.property(SecretResolver)
    }

    void setSecretsKey(SecretKeySpec key) {
        secretsKey.set(key)
    }

    DefaultSecretsPluginExtension setSecretsKey(String keyFile) {
        setSecretsKey(project.file(keyFile))
    }

    DefaultSecretsPluginExtension setSecretsKey(File keyFile) {
        setSecretsKey(new SecretKeySpec(keyFile.bytes, "AES"))
    }

    @Override
    DefaultSecretsPluginExtension secretsKey(SecretKeySpec key) {
        setSecretsKey(key)
    }

    @Override
    DefaultSecretsPluginExtension secretsKey(String keyFile) {
        return setSecretsKey(keyFile)
    }

    @Override
    DefaultSecretsPluginExtension secretsKey(File keyFile) {
        return setSecretsKey(keyFile)
    }

    protected static String secretsKeyPassword() {
        RandomStringUtils.random(20)
    }

    protected static byte[] secretsKeySalt() {
        SecureRandom random = new SecureRandom()
        byte[] salt = new byte[16]
        random.nextBytes(salt)
        salt
    }
}

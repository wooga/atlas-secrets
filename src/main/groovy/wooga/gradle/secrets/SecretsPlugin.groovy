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

import org.apache.commons.lang3.RandomStringUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import wooga.gradle.secrets.internal.DefaultSecretsPluginExtension
import wooga.gradle.secrets.tasks.SecretsTask

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.security.spec.KeySpec

class SecretsPlugin implements Plugin<Project> {

    static String EXTENSION_NAME = "secrets"

    @Override
    void apply(Project project) {
        def extension = create_and_configure_extension(project)

        project.tasks.withType(SecretsTask).configureEach { task ->
            task.secretsKey.convention(extension.secretsKey)
            task.resolver.convention(extension.secretResolver)
        }
    }

    protected static SecretsPluginExtension create_and_configure_extension(Project project) {
        def extension = project.extensions.create(SecretsPluginExtension, EXTENSION_NAME, DefaultSecretsPluginExtension)
        extension.secretsKey.convention(SecretsConsts.SECRETS_KEY.getFileValueProvider(project).map({new SecretKeySpec(it.asFile.bytes, "AES")}).orElse(project.provider({
            KeySpec spec = new PBEKeySpec(secretsKeyPassword().chars, secretsKeySalt(), SecretsConsts.SECRETS_KEY_ITERATION, SecretsConsts.SECRETS_KEY_LENGTH);
            // AES-256
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] key = f.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, "AES");
        }.memoize())))

        extension
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

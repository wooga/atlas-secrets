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

import wooga.gradle.secrets.Secret

import javax.crypto.spec.SecretKeySpec

class EncryptedSecretTextSpec extends EncryptedSecretSpec<String, EncryptedSecretText, SecretText> {

    String testValue = "Secret123456789Secret"

    @Override
    SecretText createSecret(String value) {
        new SecretText(value)
    }

    @Override
    EncryptedSecretText createEncryptedSecret(Secret<String> secret, SecretKeySpec secretKey) {
        new EncryptedSecretText(secret, secretKey)
    }
}

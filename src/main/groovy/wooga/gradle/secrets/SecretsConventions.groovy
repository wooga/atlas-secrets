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

import com.wooga.gradle.PropertyLookup
import org.apache.commons.lang3.RandomStringUtils

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.security.spec.KeySpec

class SecretsConventions {
    static Integer SECRETS_KEY_ITERATION = 65536
    static Integer SECRETS_KEY_LENGTH = 256

    static PropertyLookup secretsKey = new PropertyLookup("SECRETS_SECRETS_KEY", "secrets.secretsKey", null)
}

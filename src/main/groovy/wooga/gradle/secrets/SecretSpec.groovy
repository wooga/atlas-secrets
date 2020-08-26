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

package wooga.gradle.secrets

import org.gradle.api.provider.Property

import javax.crypto.spec.SecretKeySpec

interface SecretSpec<T extends SecretSpec> {

    Property<SecretKeySpec> getSecretsKey()
    void setSecretsKey(SecretKeySpec key)
    T setSecretsKey(String keyFile)
    T setSecretsKey(File keyFile)

    T secretsKey(SecretKeySpec key)
    T secretsKey(String keyFile)
    T secretsKey(File keyFile)
}

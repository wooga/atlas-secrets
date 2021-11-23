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

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException
import wooga.gradle.secrets.Secret
import wooga.gradle.secrets.SecretResolver
import wooga.gradle.secrets.SecretResolverException

class AWSSecretsManagerResolver implements SecretResolver {

    private final SecretsManagerClient secretsManager

    AWSSecretsManagerResolver(SecretsManagerClient client) {
        secretsManager = client
    }

    AWSSecretsManagerResolver(AwsCredentialsProvider credentials, Region region) {
        this(SecretsManagerClient.builder().credentialsProvider(credentials).region(region).build())
    }

    AWSSecretsManagerResolver(Region region) {
        this(SecretsManagerClient.builder().region(region).build())
    }

    AWSSecretsManagerResolver() {
        this(SecretsManagerClient.create())
    }

    @Override
    Secret<?> resolve(String secretId) {
        GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(secretId).build() as GetSecretValueRequest
        GetSecretValueResponse response = null
        Secret<?> secret = null
        try {
            response = secretsManager.getSecretValue(request)
        } catch (ResourceNotFoundException e) {
            throw new SecretResolverException("Unable to resolve secret with id ${secretId}", e)
        }

        if (response.secretString()) {
            return new DefaultSecret(response.secretString())
        }

        new DefaultSecret(response.secretBinary().asByteArray())
    }
}

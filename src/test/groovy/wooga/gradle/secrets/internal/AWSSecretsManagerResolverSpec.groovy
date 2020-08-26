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

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest
import spock.lang.Shared
import wooga.gradle.secrets.BasicAWSCredentials

class AWSSecretsManagerResolverSpec extends SecretsResolverSpec<AWSSecretsManagerResolver> {

    @Shared
    SecretsManagerClient secretsManager

    @Shared
    Region region = Region.EU_WEST_1

    AWSSecretsManagerResolver resolver

    @Override
    AWSSecretsManagerResolver getSubject() {
        if(!resolver) {
            resolver = new AWSSecretsManagerResolver(secretsManager)
        }
        resolver
    }

    def setupSpec() {
        def accessKey = System.getenv("ATLAS_AWS_INTEGRATION_ACCESS_KEY")
        def secretKey = System.getenv("ATLAS_AWS_INTEGRATION_SECRET_KEY")
        def builder = SecretsManagerClient.builder().region(region)

        if (accessKey && secretKey) {
            def credentials = new BasicAWSCredentials(accessKey, secretKey)
            def credentialsProvider = StaticCredentialsProvider.create(credentials)
            builder.credentialsProvider(credentialsProvider)
        }
        secretsManager = builder.build()
    }

    @Override
    void createSecret(String secretId, byte[] secretValue) {
        def r = CreateSecretRequest.builder()
                .name(secretId)
                .secretBinary(SdkBytes.fromByteArray(secretValue))
                .build() as CreateSecretRequest
        secretsManager.createSecret(r)
    }

    @Override
    void createSecret(String secretId, String secretValue) {
        def r = CreateSecretRequest.builder()
                .name(secretId)
                .secretString(secretValue)
                .build() as CreateSecretRequest
        secretsManager.createSecret(r)
    }

    @Override
    void deleteSecret(String secretId) {
        def d = DeleteSecretRequest.builder().secretId(secretId).forceDeleteWithoutRecovery(true).build() as DeleteSecretRequest
        secretsManager.deleteSecret(d)
    }
}

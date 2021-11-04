/*
 * Copyright 2021 Wooga GmbH
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

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.profiles.ProfileFile
import software.amazon.awssdk.regions.Region
import wooga.gradle.secrets.internal.AWSSecretsManagerResolver
import wooga.gradle.secrets.internal.EnvironmentResolver
import wooga.gradle.secrets.internal.SecretResolverChain

trait SecretResolverFactory {
    SecretResolver awsSecretResolver(AwsCredentialsProvider credentials, Region region) {
        new AWSSecretsManagerResolver(credentials, region)
    }

    SecretResolver awsSecretResolver(String profileName, Region region) {
        new AWSSecretsManagerResolver(awsCredentialsProvider(profileName), region)
    }

    SecretResolver awsSecretResolver(Region region) {
        new AWSSecretsManagerResolver(region)
    }

    SecretResolver environmentResolver() {
        new EnvironmentResolver()
    }

    SecretResolver chainResolver(SecretResolver... resolvers) {
        chainResolver(resolvers.toList())
    }

    SecretResolver chainResolver(Iterable<SecretResolver> resolvers) {
        new SecretResolverChain(resolvers)
    }

    AwsCredentialsProvider awsCredentialsProvider(String profileName) {
        awsCredentialsProvider(profileName, ProfileFile.defaultProfileFile())
    }

    AwsCredentialsProvider awsCredentialsProvider(String profileName, ProfileFile profileFile) {
        DefaultCredentialsProvider.builder().profileFile(profileFile).profileName(profileName).build()
    }
}

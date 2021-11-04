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

import nebula.test.ProjectSpec
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.profiles.ProfileFile
import software.amazon.awssdk.regions.Region
import wooga.gradle.secrets.internal.DefaultSecret

class SecretsPluginExtensionSpec extends ProjectSpec {
    public static final String PLUGIN_NAME = 'net.wooga.secrets'

    SecretsPluginExtension subjectUnderTest
    def resolver = Mock(SecretResolver)

    @Rule
    EnvironmentVariables environmentVariables

    def setup() {
        project.plugins.apply(PLUGIN_NAME)
        subjectUnderTest = project.extensions.findByName('secrets') as SecretsPluginExtension
        subjectUnderTest.secretResolverChain.add(resolver)
    }

    def "secretValue resolves secrets wrapped in Provider<String>"() {
        given: "a mock resolver"
        resolver.resolve(secretId) >> new DefaultSecret(expectedValue)

        when:
        def secret = subjectUnderTest.secretValue(secretId)

        then:
        noExceptionThrown()
        secret != null
        secret.present
        secret.get() == expectedValue

        where:
        secretId      | expectedValue
        "some_secret" | "some secret value"
    }

    def "secretFileAsBytes resolves secrets wrapped in Provider<byte[]>"() {
        given: "a mock resolver"
        resolver.resolve(secretId) >> new DefaultSecret(expectedValue)

        when:
        def secret = subjectUnderTest.secretFileAsBytes(secretId)

        then:
        noExceptionThrown()
        secret != null
        secret.present
        secret.get() == expectedValue

        where:
        secretId      | expectedValue
        "some_secret" | "some secret value".bytes
    }

    def "secretFile resolves secrets wrapped in Provider<RegularFile>"() {
        given: "a mock resolver"
        resolver.resolve(secretId) >> new DefaultSecret(expectedValue)

        when:
        def secret = subjectUnderTest.secretFile(secretId)

        then:
        noExceptionThrown()
        secret != null
        secret.present
        secret.get().asFile.getBytes() == expectedValue

        where:
        secretId      | expectedValue
        "some_secret" | "some secret value".bytes
    }

    def "secretValue throws no exceptions when secret can not be found"() {
        given: "a mock resolver"
        resolver.resolve(_) >> { secretId -> throw new SecretResolverException("Unable to resolve secret with id ${secretId}") }

        when:
        def present = subjectUnderTest.secretValue("some secret").isPresent()

        then:
        noExceptionThrown()
        !present
    }

    def "secretValue throws no exceptions when secret is of wrong type"() {
        given: "a mock resolver"
        def resolver = Mock(SecretResolver)
        resolver.resolve(_) >> new DefaultSecret<byte[]>("some value".bytes)

        when:
        def present = subjectUnderTest.secretValue("some secret").isPresent()

        then:
        noExceptionThrown()
        !present
    }

    def "secretFileAsBytes throws no exceptions when secret can not be found"() {
        given: "a mock resolver"
        resolver.resolve(_) >> { secretId -> throw new SecretResolverException("Unable to resolve secret with id ${secretId}") }

        when:
        def present = subjectUnderTest.secretFileAsBytes("some secret").isPresent()

        then:
        noExceptionThrown()
        !present
    }

    def "secretFileAsBytes throws no exceptions when secret is of wrong type"() {
        given: "a mock resolver"
        resolver.resolve(_) >> new DefaultSecret<String>("some value")

        when:
        def present = subjectUnderTest.secretFileAsBytes("some secret").isPresent()

        then:
        noExceptionThrown()
        !present
    }

    def "secretFile throws no exceptions when secret can not be found"() {
        given: "a mock resolver"
        resolver.resolve(_) >> { secretId -> throw new SecretResolverException("Unable to resolve secret with id ${secretId}") }

        when:
        subjectUnderTest.secretFile("some secret").isPresent()

        then:
        noExceptionThrown()
    }

    def "secretFile throws no exceptions when secret is of wrong type"() {
        given: "a mock resolver"
        resolver.resolve(_) >> new DefaultSecret<String>("some value")

        when:
        def present = subjectUnderTest.secretFile("some secret").isPresent()

        then:
        noExceptionThrown()
        !present
    }

    def "security resolver factory method awsSecretResolver with region creates resolver"() {
        when:
        def resolver = subjectUnderTest.awsSecretResolver(Region.US_EAST_1)

        then:
        resolver != null
        SecretResolver.class.isAssignableFrom(resolver.class)
    }

    def "security resolver factory method awsSecretResolver with profileName and region creates resolver"() {
        when:
        def resolver = subjectUnderTest.awsSecretResolver("some_profile", Region.US_EAST_1)

        then:
        resolver != null
        SecretResolver.class.isAssignableFrom(resolver.class)
    }

    def "security resolver factory method awsSecretResolver with credentials provider and region creates resolver"() {
        when:
        def resolver = subjectUnderTest.awsSecretResolver(DefaultCredentialsProvider.builder().build(), Region.US_EAST_1)

        then:
        resolver != null
        SecretResolver.class.isAssignableFrom(resolver.class)
    }

    def "security resolver factory method environmentResolver creates resolver"() {
        when:
        def resolver = subjectUnderTest.environmentResolver()

        then:
        resolver != null
        SecretResolver.class.isAssignableFrom(resolver.class)

    }

    def "security resolver factory method chainResolver creates resolver"() {
        when:
        def resolver = subjectUnderTest.chainResolver()

        then:
        resolver != null
        SecretResolver.class.isAssignableFrom(resolver.class)

    }

    def "security resolver factory method chainResolver with provided resolvers creates resolver"() {
        when:
        def resolver = subjectUnderTest.chainResolver(subjectUnderTest.environmentResolver(), subjectUnderTest.awsSecretResolver(Region.CN_NORTH_1))

        then:
        resolver != null
        SecretResolver.class.isAssignableFrom(resolver.class)
    }

    def "method awsCredentialsProvider creates AwsCredentialsProvider with profileName and profileFile"() {
        given: "a custom aws profile file"
        def credentialsFile = File.createTempFile("some", "awsprofile")
        credentialsFile << """
        [default]
        aws_access_key_id        = AKIAIOSFODNN7EXAMPLE
        aws_secret_access_key    = wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

        [${awsProfileName}]
        aws_access_key_id        = ${accessKeyId} 
        aws_secret_access_key    = ${awsSecretAccessKey}
        
        """.stripIndent()

        def p = ProfileFile.builder().content(credentialsFile.toPath()).type(ProfileFile.Type.CREDENTIALS).build()
        def profile = ProfileFile.aggregator().addFile(p).build()

        and: "a clean aws environment"
        environmentVariables.clear(
                "AWS_ACCESS_KEY_ID",
                "AWS_SECRET_ACCESS_KEY",
                "AWS_SESSION_TOKEN",
                "AWS_CA_Bundle",
                "AWS_CONFIG_FILE",
                "AWS_SHARED_CREDENTIALS_FILE")

        when:
        def credentials = subjectUnderTest.awsCredentialsProvider(awsProfileName, profile)

        then:
        credentials != null
        def c = credentials.resolveCredentials()
        c.accessKeyId() == accessKeyId
        c.secretAccessKey() == awsSecretAccessKey

        where:
        accessKeyId = 'AKIAIOSFODNN8EXAMPLE'
        awsSecretAccessKey = 'wJalrXUtnFEMI/K7MDENG/bPyRfiCYEXAMPLEKEY'
        awsProfileName = 'someProfile'
    }

    def "method awsCredentialsProvider creates AwsCredentialsProvider with profileName"() {
        given: "a custom credentials file"
        def credentialsFile = File.createTempFile("some", "awsprofile")
        credentialsFile << """
        [default]
        aws_access_key_id        = AKIAIOSFODNN7EXAMPLE
        aws_secret_access_key    = wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

        [${awsProfileName}]
        aws_access_key_id        = ${accessKeyId} 
        aws_secret_access_key    = ${awsSecretAccessKey}
        
        """.stripIndent()

        and: "a clean aws environment"
        environmentVariables.clear(
                "AWS_ACCESS_KEY_ID",
                "AWS_SECRET_ACCESS_KEY",
                "AWS_SESSION_TOKEN",
                "AWS_CA_Bundle",
                "AWS_CONFIG_FILE",
                "AWS_SHARED_CREDENTIALS_FILE")

        and: "a new environment value to point to our custom credentials file"
        environmentVariables.set("AWS_SHARED_CREDENTIALS_FILE", credentialsFile.absolutePath)

        when:
        def credentials = subjectUnderTest.awsCredentialsProvider(awsProfileName)

        then:
        credentials != null
        def c = credentials.resolveCredentials()
        c.accessKeyId() == accessKeyId
        c.secretAccessKey() == awsSecretAccessKey

        where:
        accessKeyId = 'AKIAIOSFODNN8EXAMPLE'
        awsSecretAccessKey = 'wJalrXUtnFEMI/K7MDENG/bPyRfiCYEXAMPLEKEY'
        awsProfileName = 'someProfile'
    }

}

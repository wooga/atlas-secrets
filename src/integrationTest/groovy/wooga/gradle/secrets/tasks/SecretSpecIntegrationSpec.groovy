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

package wooga.gradle.secrets.tasks

import com.wooga.gradle.test.IntegrationSpec
import org.gradle.api.Task
import spock.lang.Unroll

import wooga.gradle.secrets.internal.DefaultSecretsPluginExtension
import wooga.gradle.secrets.internal.EncryptionSpecHelper

import static com.wooga.gradle.test.SpecUtils.escapedPath

class SecretSpecIntegrationSpec extends IntegrationSpec {

    @Unroll("#containerTypeName of type #containerType.name can set secrets key with #method(#type)")
    def "can set secrets key"() {
        given: "secret key saved to disc"
        def key = EncryptionSpecHelper.createSecretKey("a random key")
        def keyFile = File.createTempFile("secret", "key")
        def outputKeyFile = File.createTempFile("secretOut", "key")
        def keyPath = escapedPath(keyFile.path)
        keyFile.bytes = key.encoded

        and: "the value to set"
        def value = ""
        if (type == "key") {
            value = "new javax.crypto.spec.SecretKeySpec(project.file('${keyPath}').bytes, 'AES')"
        } else if (type == "keyFile") {
            value = "project.file('${keyPath}')"
        } else if (type == "keyPath") {
            value = "'${keyPath}'"
        }

        and: "the key configured"
        if (Task.isAssignableFrom(containerType)) {
            buildFile << """
                task("temp", type: ${containerType.name}) {
                    ${method}(${value})
                }
            """.stripIndent()
        } else {
            buildFile << """
                extensions.create('temp', ${containerType.name})
                temp.${method}(${value})
            """.stripIndent()
        }

        and: "a task to write out the key"
        buildFile << """
            task("writeKey") {
                doLast {
                    def output = new File("${escapedPath(outputKeyFile.path)}")
                    output.bytes = temp.secretsKey.get().encoded
                }
            }
        """

        when:
        runTasksSuccessfully("writeKey")

        then:
        outputKeyFile.exists()
        outputKeyFile.bytes == keyFile.bytes

        cleanup:
        keyFile.delete()
        outputKeyFile.delete()

        where:
        containerType                 | property         | type      | useSetter
        SecretsTask                   | "secretsKey"     | "key"     | false
        SecretsTask                   | "secretsKey"     | "key"     | true
        SecretsTask                   | "secretsKey.set" | "key"     | false
        SecretsTask                   | "secretsKey"     | "keyFile" | false
        SecretsTask                   | "secretsKey"     | "keyFile" | true

        DefaultSecretsPluginExtension | "secretsKey"     | "key"     | false
        DefaultSecretsPluginExtension | "secretsKey"     | "key"     | true
        DefaultSecretsPluginExtension | "secretsKey.set" | "key"     | false
        DefaultSecretsPluginExtension | "secretsKey"     | "keyFile" | false
        DefaultSecretsPluginExtension | "secretsKey"     | "keyFile" | true

        method = (useSetter) ? "set${property.capitalize()}" : property
        containerTypeName = Task.isAssignableFrom(containerType) ? "task" : "extension"
    }
}

class SecretsTaskTest extends SecretsTask {

}

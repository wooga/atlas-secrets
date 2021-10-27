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

import com.wooga.gradle.test.IntegrationSpec
import wooga.gradle.secrets.tasks.SecretsTask

class SecretsIntegrationSpec extends IntegrationSpec {
    static final String extensionName = SecretsPlugin.EXTENSION_NAME

    String getSubjectUnderTestName() {
        "secretsIntegrationTest"
    }

    String getSubjectUnderTestTypeName() {
        SecretsTask.class.name
    }

    void appendToSubjectTask(String... lines) {
        buildFile << """
        $subjectUnderTestName {
            ${lines.join('\n')}
        }
        """.stripIndent()
    }

    private static String createSecretTempFilePath(byte[] content) {
        def tempFile = File.createTempFile("SecretKey", "Spec")
        tempFile.bytes = content as byte[]
        tempFile.absolutePath
    }

    static wrapValueFallback = { Object rawValue, String type, Closure<String> fallback ->
        switch (type) {
            case "SecretKeySpec":
                return "new javax.crypto.spec.SecretKeySpec(${rawValue.toString()} as byte[], 'AES')"
            case "SecretKeySpecFile":
                return wrapValueBasedOnType(createSecretTempFilePath(rawValue as byte[]), "File", wrapValueFallback)

            case "SecretKeySpecFilePathRaw":
                return createSecretTempFilePath(rawValue as byte[])
            default:
                return rawValue.toString()
        }
    }
}

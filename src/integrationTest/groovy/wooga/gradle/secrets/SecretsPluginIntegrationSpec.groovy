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

import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import spock.lang.Unroll

import static com.wooga.gradle.test.PropertyUtils.*
import static com.wooga.gradle.test.SpecUtils.escapedPath

class SecretsPluginIntegrationSpec extends SecretsIntegrationSpec {
    def setup() {
        buildFile << """
        ${applyPlugin(SecretsPlugin)}
        """
    }

    @Unroll
    def "extension property :#property returns '#testValue' if #reason"() {
        given: "a set value"

        fork = false
        switch (location) {
            case PropertyLocation.script:
                buildFile << "${extensionName}.${invocation}"
                break
            case PropertyLocation.property:
                def propertiesFile = createFile("gradle.properties")
                propertiesFile << "${extensionName}.${property} = ${escapedValue}"
                break
            case PropertyLocation.environment:
                def envPropertyKey = envNameFromProperty(extensionName, property)
                environmentVariables.set(envPropertyKey, value.toString())
                break
            default:
                break
        }

        and: "the test value with replace placeholders"
        if (testValue instanceof String) {
            testValue = testValue.replaceAll("#projectDir#", escapedPath(projectDir.path))
        }

        when:
        def query = new PropertyQueryTaskWriter("${extensionName}.${property}", pInvocation.toString())
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        property     | method                  | rawValue         | expectedValue               | type                       | location                     | propertyInvocation
        "secretsKey" | _                       | _                | "a generated SecretKeySpec" | _                          | PropertyLocation.none        | ".map({'${expectedValue}'}).getOrNull()"
        "secretsKey" | _                       | "18273645".bytes | _                           | "SecretKeySpecFilePathRaw" | PropertyLocation.environment | ".map({it.getEncoded()}).getOrNull()"
        "secretsKey" | _                       | "81726354".bytes | _                           | "SecretKeySpecFilePathRaw" | PropertyLocation.property    | ".map({it.getEncoded()}).getOrNull()"
        "secretsKey" | toSetter(property)      | "12348765".bytes | _                           | "SecretKeySpecFile"        | PropertyLocation.script      | ".map({it.getEncoded()}).getOrNull()"
        "secretsKey" | toProviderSet(property) | "87654321".bytes | _                           | "Provider<SecretKeySpec>"  | PropertyLocation.script      | ".map({it.getEncoded()}).getOrNull()"
        "secretsKey" | toProviderSet(property) | "12345678".bytes | _                           | "SecretKeySpec"            | PropertyLocation.script      | ".map({it.getEncoded()}).getOrNull()"

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        pInvocation = (propertyInvocation != _) ? propertyInvocation : ".getOrNull()"
        providedValue = (location == PropertyLocation.script) ? type : value
        testValue = (expectedValue == _) ? rawValue : expectedValue
        reason = location.reason() + ((location == PropertyLocation.none) ? "" : "  with '$providedValue' ")
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }

}

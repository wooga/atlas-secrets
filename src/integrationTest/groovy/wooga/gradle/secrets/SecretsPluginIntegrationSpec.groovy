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

import com.wooga.gradle.test.ConventionSource
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import spock.lang.Unroll
import wooga.gradle.secrets.internal.DefaultResolver
import wooga.gradle.secrets.internal.EnvironmentResolver
import wooga.gradle.secrets.internal.SecretResolverChain
import wooga.gradle.secrets.tasks.SecretsTask

import static com.wooga.gradle.test.PropertyUtils.*
import static com.wooga.gradle.test.SpecUtils.escapedPath

class SecretsPluginIntegrationSpec extends SecretsIntegrationSpec {
    def setup() {
        buildFile << """
        ${applyPlugin(SecretsPlugin)}
        """
    }

    @Unroll
    def "sets convention for task type #taskType.simpleName and property #property"() {
        given: "write convention source assignment"
        if (value != _) {
            conventionSource.write(buildFile, value.toString())
        }

        and: "a task to query property from"

        buildFile << """
        class ${taskType.simpleName}Impl extends ${taskType.name} {
           //TODO this we need to adjust
           final String errorMessage = "Failed to create/update config section"
        }

        task ${subjectUnderTestName}(type: ${taskType.simpleName}Impl)
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}", pInvocation.toString())
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        taskType    | property     | rawValue         | type            | conventionSource                                    | expectedValue                        | propertyInvocation
        SecretsTask | "secretsKey" | "18273645".bytes | "SecretKeySpec" | ConventionSource.extension(extensionName, property) | _                                    | ".map({it.getEncoded()}).getOrNull()"
        SecretsTask | "resolver"   | _                | _               | _                                                   | new SecretResolverChain().toString() | _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
        pInvocation = (propertyInvocation != _) ? propertyInvocation : ".getOrNull().toString()"
    }

    private setupPropertyValue(PropertyLocation location, String invocation, String property, String value) {
        switch (location) {
            case PropertyLocation.script:
                buildFile << "${extensionName}.${invocation}"
                break
            case PropertyLocation.property:
                def propertiesFile = createFile("gradle.properties")
                propertiesFile << "${extensionName}.${property} = ${(value instanceof String) ? escapedPath(value) : value}"
                break
            case PropertyLocation.environment:
                def envPropertyKey = envNameFromProperty(extensionName, property)
                environmentVariables.set(envPropertyKey, value.toString())
                break
            default:
                break
        }
    }

    @Unroll
    def "extension property :#property returns '#testValue' if #reason"() {
        given: "a set value"
        setupPropertyValue(location, invocation.toString(), property.toString(), value.toString())

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
        property         | method                  | rawValue         | expectedValue                        | type                       | location                     | propertyInvocation
        "secretsKey"     | _                       | _                | "a generated SecretKeySpec"          | _                          | PropertyLocation.none        | ".map({'${expectedValue}'}).getOrNull()"
        "secretsKey"     | _                       | "18273645".bytes | _                                    | "SecretKeySpecFilePathRaw" | PropertyLocation.environment | ".map({it.getEncoded()}).getOrNull()"
        "secretsKey"     | _                       | "81726354".bytes | _                                    | "SecretKeySpecFilePathRaw" | PropertyLocation.property    | ".map({it.getEncoded()}).getOrNull()"
        "secretsKey"     | toSetter(property)      | "12348765".bytes | _                                    | "SecretKeySpecFile"        | PropertyLocation.script      | ".map({it.getEncoded()}).getOrNull()"
        "secretsKey"     | toProviderSet(property) | "87654321".bytes | _                                    | "Provider<SecretKeySpec>"  | PropertyLocation.script      | ".map({it.getEncoded()}).getOrNull()"
        "secretsKey"     | toProviderSet(property) | "12345678".bytes | _                                    | "SecretKeySpec"            | PropertyLocation.script      | ".map({it.getEncoded()}).getOrNull()"
        "secretResolver" | _                       | _                | new SecretResolverChain().toString() | _                          | PropertyLocation.none        | _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        pInvocation = (propertyInvocation != _) ? propertyInvocation : ".getOrNull().toString()"
        providedValue = (location == PropertyLocation.script) ? type : value
        testValue = (expectedValue == _) ? rawValue : expectedValue
        reason = location.reason() + ((location == PropertyLocation.none) ? "" : "  with '$providedValue' ")
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }

    def "extension allows to configure resolvers in secretResolverChain with closure"() {
        given: "an empty resolverChain"
        buildFile << """
        secrets.secretResolverChain.clear() 
        """.stripIndent()

        and: "configuring a new resolver"
        buildFile << """
        secrets.secretResolverChain {
            add(new ${EnvironmentResolver.class.name}())
        }
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("${extensionName}.${property}", ".getOrNull().toString()")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, expectedValue)

        where:
        property         | expectedValue
        "secretResolver" | new SecretResolverChain([new EnvironmentResolver()]).toString()
    }

    def "extension resolves secrets wrapped in provider"() {
        given: "a resolver chain with a default resolver configured"
        buildFile << """
        secrets.secretResolverChain {
            add(new ${DefaultResolver.class.name}({ String secretId ->
                switch(secretId) {
                    case "secretString":
                        return "a secret value"
                    break
                    case "secretFile":
                        return "a secret value".bytes
                    break
                }
            }))
        }
        """.stripIndent()


    }

}

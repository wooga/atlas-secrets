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

import nebula.test.ProjectSpec
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import spock.lang.Unroll

class SecretsPluginSpec extends ProjectSpec {

    public static final String PLUGIN_NAME = 'net.wooga.secrets'

    @Unroll
    def 'Creates the [#extensionName] extension with type #extensionType'() {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.extensions.findByName(extensionName)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        def extension = project.extensions.findByName(extensionName)
        extensionType.isInstance extension

        where:
        extensionName | extensionType
        'secrets'     | SecretsPluginExtension
    }

    def hasTask(Project project, String taskName) {
        try {
            project.tasks.named(taskName)
            return true
        } catch(UnknownTaskException _) {
            return false
        }

    }

}

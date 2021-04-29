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

import org.gradle.api.Plugin
import org.gradle.api.Project
import wooga.gradle.secrets.internal.DefaultSecretsPluginExtension
import wooga.gradle.secrets.tasks.FetchSecrets

class SecretsPlugin implements Plugin<Project> {

    static String EXTENSION_NAME = "secrets"

    @Override
    void apply(Project project) {
        def extension = create_and_configure_extension(project)

        project.tasks.register("fetchSecrets", FetchSecrets) {task ->
            task.group = "Secrets"
            task.description = "Fetch configured secrets"
        }

        project.tasks.withType(FetchSecrets).configureEach { task ->
            task.secretsKey.convention(extension.secretsKey)
            task.secretsFile.set(project.provider {
                project.layout.buildDirectory.dir("secret/${task.name}").get().file("secrets.yml")
            })
            task.resolver.convention(extension.secretResolver)
        }
    }

    protected static SecretsPluginExtension create_and_configure_extension(Project project) {
        def extension = project.extensions.create(SecretsPluginExtension, EXTENSION_NAME, DefaultSecretsPluginExtension, project)
        extension
    }
}

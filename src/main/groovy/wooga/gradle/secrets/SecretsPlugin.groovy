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

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.slf4j.Logger
import wooga.gradle.secrets.internal.DefaultSecretsPluginExtension
import wooga.gradle.secrets.tasks.FetchSecrets

class SecretsPlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(SecretsPlugin)

    static String EXTENSION_NAME = "secrets"

    @Override
    void apply(Project project) {
        def extension = create_and_configure_extension(project)

        def exampleTask = project.tasks.create("fetchSecrets", FetchSecrets)
        exampleTask.group = "Secrets"
        exampleTask.description = "Fetch configured secrets"

        project.tasks.withType(FetchSecrets, new Action<FetchSecrets>() {
            @Override
            void execute(FetchSecrets t) {
                t.secretsKey.convention(extension.secretsKey)
                t.secretsFile.set(project.provider({
                    project.layout.buildDirectory.dir("secret/${t.name}").get().file("secrets.yml")
                }))
                t.resolver.convention(extension.secretResolver)
            }
        })
    }

    protected static SecretsPluginExtension create_and_configure_extension(Project project) {
        def extension = project.extensions.create(SecretsPluginExtension, EXTENSION_NAME, DefaultSecretsPluginExtension, project)

        extension
    }
}

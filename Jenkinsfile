/*
 * Copyright 2018-2020 Wooga GmbH
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
 *
 *
 *
 */

#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([
                    string(credentialsId: 'atlas_secrets_coveralls_token', variable: 'coveralls_token'),
                    string(credentialsId: 'aws.secretsmanager.integration.accesskey', variable: 'accesskey'),
                    string(credentialsId: 'aws.secretsmanager.integration.secretkey', variable: 'secretkey'),
                ])
{
    def env = ["ATLAS_AWS_INTEGRATION_ACCESS_KEY=${accesskey}", "ATLAS_AWS_INTEGRATION_SECRET_KEY=${secretkey}"]
    buildGradlePlugin plaforms: ['osx','windows', 'linux'], coverallsToken: coveralls_token, testEnvironment:env
}


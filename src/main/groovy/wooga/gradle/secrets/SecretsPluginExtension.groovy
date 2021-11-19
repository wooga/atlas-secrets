/*
 * Copyright 2020-2021 Wooga GmbH
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

import org.apache.commons.lang3.RandomStringUtils
import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.util.ConfigureUtil
import wooga.gradle.secrets.internal.SecretResolverChain

import java.util.logging.Logger

trait SecretsPluginExtension implements SecretSpec, SecretResolverFactory {

    static Logger LOGGER = Logger.getLogger(SecretsPluginExtension.class.getName());

    Logger getLogger() {
        LOGGER
    }

    private final Property<Boolean> cacheSecrets = objects.property(Boolean)

    Property<Boolean> getCacheSecrets() {
        cacheSecrets
    }

    void setCacheSecrets(Provider<Boolean> value) {
        cacheSecrets.set(value)
    }

    void setCacheSecrets(Boolean value) {
        cacheSecrets.set(value)
    }

    private final Property<Integer> secretCacheTimeout = objects.property(Integer)

    Property<Integer> getSecretCacheTimeout() {
        secretCacheTimeout
    }

    void setSecretCacheTimeout(Provider<Integer> value) {
        secretCacheTimeout.set(value)
    }

    void setSecretCacheTimeout(Integer value) {
        secretCacheTimeout.set(value)
    }

    abstract SecretResolverChain getSecretResolverChain()

    Provider<SecretResolverChain> getSecretResolver() {
        providerFactory.provider({ secretResolverChain })
    }

    void secretResolverChain(Action<SecretResolverChain> action) {
        action.execute(secretResolverChain)
    }

    void secretResolverChain(Closure configure) {
        secretResolverChain(ConfigureUtil.configureUsing(configure))
    }

    void setSecretResolver(SecretResolver resolver) {
        secretResolverChain.setResolverChain(resolver)
    }

    Provider<String> secretValue(String secretId) {
        secretResolver.map({ SecretResolver resolver ->
            try {
                def resolvedValue = resolver.resolve(secretId).getSecretValue()
                if (!String.isInstance(resolvedValue)) {
                    logger.warning("Secret with secretId '${secretId}' is not of type String")
                    return null
                }
                resolvedValue as String
            } catch (SecretResolverException e) {
                logger.warning(e.message)
                return null
            }
        })
    }

    Provider<byte[]> secretFileAsBytes(String secretId) {
        secretResolver.map({ SecretResolver resolver ->
            try {
                def resolvedValue = resolver.resolve(secretId).getSecretValue()
                if (String.isInstance(resolvedValue)) {
                    logger.warning("Secret with secretId '${secretId}' is not of type byte[]")
                    return null
                }
                resolvedValue as byte[]
            } catch (SecretResolverException e) {
                logger.warning(e.message)
                return null
            }
        })
    }

    Provider<RegularFile> secretFile(String secretId) {
        layout.file(secretFileAsBytes(secretId).map({
            File tempFile = File.createTempFile(RandomStringUtils.random(10, true, true), RandomStringUtils.random(10, true, true))
            tempFile.deleteOnExit()
            tempFile.bytes = it
            tempFile
        }))
    }

}

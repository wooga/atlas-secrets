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

package wooga.gradle.secrets.internal

import wooga.gradle.secrets.Secret
import wooga.gradle.secrets.SecretResolver
import wooga.gradle.secrets.SecretResolverException
import wooga.gradle.secrets.SecretResolverFactory

class SecretResolverChain implements SecretResolver, List<SecretResolver>, SecretResolverFactory {

    @Delegate
    private final List<SecretResolver> resolverChain

    SecretResolverChain(Iterable<SecretResolver> resolver) {
        resolverChain = []
        addAll(resolver)
    }

    SecretResolverChain() {
        this([])
    }

    void setResolverChain(Iterable<SecretResolver> resolver) {
        clear()
        addAll(resolver)
    }

    void setResolverChain(SecretResolver... resolver) {
        setResolverChain(resolver.toList())
    }

    @Override
    Secret<?> resolve(String secretId) {
        if (empty) {
            throw new SecretResolverException("No secret resolvers configured.")
        }

        Secret secret = null

        for (SecretResolver resolver in resolverChain) {
            try {
                secret = resolver.resolve(secretId)
                if (secret) {
                    break
                }
            }
            catch (SecretResolverException ignored) {
            }
        }

        if (!secret) {
            throw new SecretResolverException("Unable to resolve secret with id ${secretId}")
        }

        secret
    }

    @Override
    String toString() {
        "SecretResolverChain{" +
                "resolverChain=" + resolverChain +
                '}'
    }
}

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

package wooga.gradle.secrets.internal

import wooga.gradle.secrets.Secret
import wooga.gradle.secrets.SecretResolver
import wooga.gradle.secrets.SecretResolverException

import java.sql.Timestamp
import java.time.Instant
import java.util.logging.Logger

class SecretsCache<T extends SecretResolver> implements SecretResolver {

    static Logger logger = Logger.getLogger(SecretsCache.class.name)

    private static class SecretCacheItem {
        private final Secret secret
        private final Timestamp cacheTime

        Secret getSecret() {
            secret
        }

        Timestamp getCacheTime() {
            cacheTime
        }

        SecretCacheItem(Secret secret, Integer cacheTimeout) {
            this.secret = secret
            this.cacheTime = Timestamp.from(Instant.now())
            touch(cacheTimeout)
        }

        static cacheSecret(Secret secret, Integer cacheTimeout) {
            new SecretCacheItem(secret, cacheTimeout)
        }

        private void touch(Integer cacheTimeout) {
            cacheTime.getTime()
            Calendar cal = Calendar.getInstance()
            cal.setTime(new Date(cacheTime.getTime()))
            cal.add(Calendar.SECOND, cacheTimeout)
            cacheTime.setTime(cal.getTime().getTime())
        }

        Boolean isValid() {
            Timestamp now = Timestamp.from(Instant.now())

            logger.fine("Time now: ${now}")
            logger.fine("Secret cache timeout: ${cacheTime}")

            cacheTime.after(now)
        }
    }

    @Delegate
    protected T innerResolver

    private final Map<String, SecretCacheItem> cache
    private final Integer cacheTimeout


    SecretsCache(T resolver) {
        this(resolver, 10)
    }

    SecretsCache(T resolver, Integer cacheTimeout) {
        innerResolver = resolver
        cache = new HashMap<String, SecretCacheItem>(20)
        this.cacheTimeout = cacheTimeout
    }

    @Override
    Secret resolve(String secretId) throws SecretResolverException {
        if (cache.containsKey(secretId)) {
            logger.fine("Found secrete '${secretId}' in cache.")
            SecretCacheItem cacheItem = cache.get(secretId)

            if (cacheItem.valid) {
                return cacheItem.secret
            }

            logger.fine("secret too old.")
        }

        def secret = innerResolver.resolve(secretId)
        cache.put(secretId, SecretCacheItem.cacheSecret(secret, cacheTimeout))
        secret
    }
}

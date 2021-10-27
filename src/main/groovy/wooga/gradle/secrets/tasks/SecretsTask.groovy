package wooga.gradle.secrets.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import wooga.gradle.secrets.SecretResolver
import wooga.gradle.secrets.SecretSpec

import javax.crypto.spec.SecretKeySpec

abstract class SecretsTask<T extends SecretsTask> extends DefaultTask implements SecretSpec<SecretsTask> {
    @Input
    private final Property<SecretKeySpec> secretsKey = project.objects.property(SecretKeySpec)

    Property<SecretKeySpec> getSecretsKey() {
        secretsKey
    }

    void setSecretsKey(SecretKeySpec key) {
        secretsKey.set(key)
    }

    T setSecretsKey(String keyFile) {
        setSecretsKey(project.file(keyFile))
    }

    T setSecretsKey(File keyFile) {
        setSecretsKey(new SecretKeySpec(keyFile.bytes, "AES"))
    }

    @Override
    T secretsKey(SecretKeySpec key) {
        setSecretsKey(key)
    }

    @Override
    T secretsKey(String keyFile) {
        return setSecretsKey(keyFile)
    }

    @Override
    T secretsKey(File keyFile) {
        return setSecretsKey(keyFile)
    }

    @Internal
    private final Property<SecretResolver> resolver = project.objects.property(SecretResolver)

    Property<SecretResolver> getResolver() {
        resolver
    }

    void setResolver(SecretResolver value) {
        resolver.set(value)
    }

    void resolver(SecretResolver value) {
        setResolver(value)
    }
}

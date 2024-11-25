package dulian.dulian.global.config.jasypt

import org.jasypt.encryption.StringEncryptor
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig
import org.jasypt.salt.RandomSaltGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JasyptConfig(
    @Value("\${jasypt.encryptor.password}")
    private val encryptKey: String
) {

    @Bean(name = ["jasyptStringEncryptor"])
    fun stringEncryptor():StringEncryptor {
        val config = SimpleStringPBEConfig()
        config.password = encryptKey
        config.algorithm = "PBEWithMD5AndDES"
        config.keyObtentionIterations = 1000
        config.poolSize = 1
        config.providerName = "SunJCE"
        config.saltGenerator = RandomSaltGenerator()
        config.stringOutputType = "base64"

        val encryptor = PooledPBEStringEncryptor()
        encryptor.setConfig(config)

        return encryptor
    }
}

package dulian.dulian.global.config.db

import org.hibernate.cfg.AvailableSettings
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing
class JpaConfig(
) {

    @Bean
    fun auditorAware(): AuditorAware<String> {
        return AuditorAwareImpl()
    }

    @Bean
    fun hibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer { properties: MutableMap<String?, Any?> ->
            properties[AvailableSettings.STATEMENT_INSPECTOR] = CustomStatementInspector()
        }
    }
}

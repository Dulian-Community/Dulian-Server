package dulian.dulian.global.config.mail

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templateresolver.ITemplateResolver

@Configuration
class EmailConfig {

    @Bean
    fun templateEngine(
        templateResolver: ITemplateResolver
    ): SpringTemplateEngine {
        val templateEngine = SpringTemplateEngine()
        templateEngine.setTemplateResolver(templateResolver)

        return templateEngine
    }
}
package dulian.dulian.global.config.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()

        val httpRequestFactory = HttpComponentsClientHttpRequestFactory()
        httpRequestFactory.setConnectionRequestTimeout(2000)
        httpRequestFactory.setConnectTimeout(2000)

        restTemplate.requestFactory = httpRequestFactory

        return restTemplate
    }
}
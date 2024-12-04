package dulian.dulian

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class HealthCheckController {

    private val log = KotlinLogging.logger { }

    @GetMapping("/health-check")
    fun healthCheck(): ResponseEntity<String> {
        log.info { "Health Check" }
        return ResponseEntity.ok("OK")
    }
}
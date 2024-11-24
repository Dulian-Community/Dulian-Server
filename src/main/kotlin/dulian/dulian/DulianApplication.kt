package dulian.dulian

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DulianApplication

fun main(args: Array<String>) {
    runApplication<DulianApplication>(*args)
}

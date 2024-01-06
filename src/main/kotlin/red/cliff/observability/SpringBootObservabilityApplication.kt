package red.cliff.observability

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringBootObservabilityApplication

fun main(args: Array<String>) {
	runApplication<SpringBootObservabilityApplication>(*args)
}

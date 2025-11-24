package dev.logward.sdk.examples.middleware.springboot

import dev.logward.sdk.LogWardClient
import dev.logward.sdk.middleware.LogWardInterceptor
import dev.logward.sdk.models.LogWardClientOptions
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.time.Duration.Companion.seconds

/**
 * Example of using LogWard with Spring Boot
 *
 * This example demonstrates how to configure the LogWardInterceptor
 * for automatic HTTP request/response logging in a Spring Boot application.
 */
@SpringBootApplication
class SpringBootExampleApplication

fun main(args: Array<String>) {
    runApplication<SpringBootExampleApplication>(*args)
}

@Configuration
class LogWardConfig : WebMvcConfigurer {

    @Bean
    fun logWardClient(): LogWardClient {
        return LogWardClient(
            LogWardClientOptions(
                apiUrl = "http://localhost:8080",
                apiKey = "lp_your_api_key_here",
                batchSize = 100,
                flushInterval = 5.seconds,
                maxBufferSize = 10000,
                enableMetrics = true,
                debug = false,
                globalMetadata = mapOf(
                    "env" to "production",
                    "version" to "1.0.0",
                    "service" to "spring-boot-app"
                )
            )
        )
    }

    @Bean
    fun logWardInterceptor(client: LogWardClient): LogWardInterceptor {
        return LogWardInterceptor(
            client = client,
            serviceName = "spring-boot-app",
            logRequests = true,
            logResponses = true,
            logErrors = true,
            skipHealthCheck = true,
            skipPaths = setOf("/metrics", "/internal")
        )
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(logWardInterceptor(logWardClient()))
            .addPathPatterns("/**")
            .excludePathPatterns("/static/**", "/public/**")
    }
}

@RestController
@RequestMapping("/api")
class ExampleController {

    @GetMapping("/")
    fun home(): Map<String, String> {
        return mapOf("message" to "Hello from Spring Boot with LogWard!")
    }

    @GetMapping("/users")
    fun getUsers(): Map<String, List<Map<String, Any>>> {
        // Simulate some work
        Thread.sleep(50)
        return mapOf(
            "users" to listOf(
                mapOf("id" to 1, "name" to "Alice"),
                mapOf("id" to 2, "name" to "Bob")
            )
        )
    }

    @GetMapping("/error")
    fun simulateError(): Nothing {
        // This will be logged as an error by LogWardInterceptor
        throw RuntimeException("Simulated error from Spring Boot")
    }
}

@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): Map<String, String> {
        // This will be skipped by LogWard (skipHealthCheck = true)
        return mapOf("status" to "healthy")
    }

    @GetMapping("/actuator/health")
    fun actuatorHealth(): Map<String, String> {
        // This will also be skipped by LogWard
        return mapOf("status" to "UP")
    }
}

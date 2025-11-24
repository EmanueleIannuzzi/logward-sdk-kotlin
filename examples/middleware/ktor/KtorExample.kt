package dev.logward.sdk.examples.middleware.ktor

import dev.logward.sdk.middleware.LogWardPlugin
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

/**
 * Example of using LogWard with Ktor
 *
 * This example demonstrates how to install and configure the LogWardPlugin
 * for automatic HTTP request/response logging in a Ktor application.
 */
fun main() {
    embeddedServer(Netty, port = 8080) {
        // Install LogWard plugin
        install(LogWardPlugin) {
            apiUrl = "http://localhost:8080"
            apiKey = "lp_your_api_key_here"
            serviceName = "ktor-app"

            // Optional configuration
            logRequests = true
            logResponses = true
            logErrors = true
            skipHealthCheck = true
            skipPaths = setOf("/metrics", "/internal")

            // LogWard client options
            batchSize = 100
            flushInterval = kotlin.time.Duration.parse("5s")
            maxBufferSize = 10000
            enableMetrics = true
            debug = false
            globalMetadata = mapOf(
                "env" to "production",
                "version" to "1.0.0"
            )
        }

        routing {
            get("/") {
                call.respondText("Hello from Ktor with LogWard!", ContentType.Text.Plain)
            }

            get("/api/users") {
                // Simulate some work
                Thread.sleep(50)
                call.respond(HttpStatusCode.OK, mapOf(
                    "users" to listOf(
                        mapOf("id" to 1, "name" to "Alice"),
                        mapOf("id" to 2, "name" to "Bob")
                    )
                ))
            }

            get("/api/error") {
                // This will be logged as an error
                throw RuntimeException("Simulated error")
            }

            get("/health") {
                // This will be skipped by LogWard (skipHealthCheck = true)
                call.respond(HttpStatusCode.OK, mapOf("status" to "healthy"))
            }
        }
    }.start(wait = true)
}

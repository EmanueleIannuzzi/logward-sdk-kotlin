package dev.logward.sdk.exceptions

/**
 * Exception thrown when circuit breaker is in OPEN state
 */
class CircuitBreakerOpenException(
    message: String = "Circuit breaker is OPEN - requests are blocked"
) : LogWardException(message)

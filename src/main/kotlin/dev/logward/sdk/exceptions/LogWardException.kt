package dev.logward.sdk.exceptions

/**
 * Base exception for LogWard SDK errors
 */
open class LogWardException(message: String, cause: Throwable? = null) : Exception(message, cause)

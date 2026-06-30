package com.nexify.hytale;

/**
 * Thin logging shim so the rest of the plugin doesn't depend directly on
 * Hytale's logger type (a Flogger-style API: getLogger().at(Level.INFO).log(...)).
 * Wrap that call in NexifyPlugin and pass this interface down instead.
 */
public interface NexifyLog {
    void info(String message);
    void warn(String message);
    void warn(String message, Throwable cause);
}

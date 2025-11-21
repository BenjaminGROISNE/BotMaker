package com.botmaker.events;

/**
 * Base interface for all application events.
 * Events represent things that have happened in the application.
 */
public interface ApplicationEvent {
    /**
     * Timestamp when the event was created
     */
    long getTimestamp();

    /**
     * Optional source identifier for debugging
     */
    default String getSource() {
        return getClass().getSimpleName();
    }
}
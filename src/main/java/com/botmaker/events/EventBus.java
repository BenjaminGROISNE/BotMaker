package com.botmaker.events;

import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central event bus for application-wide event communication.
 * Thread-safe and supports both synchronous and asynchronous event delivery.
 */
public class EventBus {
    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());

    private final Map<Class<? extends ApplicationEvent>, List<EventHandler<?>>> handlers;
    private final boolean enableLogging;

    public EventBus() {
        this(false);
    }

    public EventBus(boolean enableLogging) {
        this.handlers = new ConcurrentHashMap<>();
        this.enableLogging = enableLogging;
    }

    /**
     * Subscribe to events of a specific type
     */
    public <T extends ApplicationEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribe(eventType, handler, false);
    }

    /**
     * Subscribe to events with option to run on JavaFX thread
     */
    public <T extends ApplicationEvent> void subscribe(
            Class<T> eventType,
            Consumer<T> handler,
            boolean runOnFxThread) {

        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(new EventHandler<>(handler, runOnFxThread));

        if (enableLogging) {
            LOGGER.info("Subscribed to " + eventType.getSimpleName());
        }
    }

    /**
     * Publish an event to all subscribers
     */
    public void publish(ApplicationEvent event) {
        if (event == null) {
            return;
        }

        if (enableLogging) {
            LOGGER.info("Publishing: " + event.getSource());
        }

        Class<? extends ApplicationEvent> eventType = event.getClass();
        List<EventHandler<?>> eventHandlers = handlers.get(eventType);

        if (eventHandlers == null || eventHandlers.isEmpty()) {
            if (enableLogging) {
                LOGGER.warning("No handlers for event: " + eventType.getSimpleName());
            }
            return;
        }

        for (EventHandler<?> handler : eventHandlers) {
            try {
                handler.handle(event);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error handling event: " + eventType.getSimpleName(), e);
            }
        }
    }

    /**
     * Unsubscribe a specific handler from an event type
     */
    public <T extends ApplicationEvent> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
        List<EventHandler<?>> eventHandlers = handlers.get(eventType);
        if (eventHandlers != null) {
            eventHandlers.removeIf(h -> h.getHandler() == handler);
        }
    }

    /**
     * Clear all handlers for a specific event type
     */
    public void clearHandlers(Class<? extends ApplicationEvent> eventType) {
        handlers.remove(eventType);
    }

    /**
     * Clear all handlers
     */
    public void clearAllHandlers() {
        handlers.clear();
    }

    /**
     * Get count of handlers for an event type (useful for debugging)
     */
    public int getHandlerCount(Class<? extends ApplicationEvent> eventType) {
        List<EventHandler<?>> eventHandlers = handlers.get(eventType);
        return eventHandlers != null ? eventHandlers.size() : 0;
    }

    /**
     * Internal wrapper for event handlers
     */
    private static class EventHandler<T extends ApplicationEvent> {
        private final Consumer<T> handler;
        private final boolean runOnFxThread;

        EventHandler(Consumer<T> handler, boolean runOnFxThread) {
            this.handler = handler;
            this.runOnFxThread = runOnFxThread;
        }

        @SuppressWarnings("unchecked")
        void handle(ApplicationEvent event) {
            if (runOnFxThread && !Platform.isFxApplicationThread()) {
                Platform.runLater(() -> handler.accept((T) event));
            } else {
                handler.accept((T) event);
            }
        }

        Consumer<T> getHandler() {
            return handler;
        }
    }
}
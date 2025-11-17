package com.botmaker.di;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Simple dependency injection container.
 * Manages service lifecycle and provides dependency resolution.
 * Phase 3: Fixed ConcurrentModificationException
 */
public class DependencyContainer {

    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();

    /**
     * Register a singleton instance
     */
    public <T> void registerSingleton(Class<T> type, T instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Instance cannot be null");
        }
        singletons.put(type, instance);
    }

    /**
     * Register a factory for creating instances
     */
    public <T> void registerFactory(Class<T> type, Supplier<T> factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }
        factories.put(type, factory);
    }

    /**
     * Register a lazy singleton using a factory
     * The instance will be created on first access and cached
     */
    public <T> void registerLazySingleton(Class<T> type, Supplier<T> factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }
        factories.put(type, factory);
    }

    /**
     * Resolve a dependency
     */
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        // Check if we have a singleton instance
        Object instance = singletons.get(type);
        if (instance != null) {
            return (T) instance;
        }

        // Check if we have a factory
        Supplier<?> factory = factories.get(type);
        if (factory != null) {
            // PHASE 3 FIX: Create instance first, then cache it
            // This avoids ConcurrentModificationException
            T newInstance = (T) factory.get();

            // Cache the instance for future use
            singletons.put(type, newInstance);

            // Remove the factory since we don't need it anymore
            // (optional - keeps memory clean)
            factories.remove(type);

            return newInstance;
        }

        throw new IllegalStateException("No registration found for type: " + type.getName());
    }

    /**
     * Check if a type is registered
     */
    public boolean isRegistered(Class<?> type) {
        return singletons.containsKey(type) || factories.containsKey(type);
    }

    /**
     * Remove a registration
     */
    public void unregister(Class<?> type) {
        singletons.remove(type);
        factories.remove(type);
    }

    /**
     * Clear all registrations
     */
    public void clear() {
        singletons.clear();
        factories.clear();
    }

    /**
     * Get count of registered types
     */
    public int getRegistrationCount() {
        return singletons.size() + factories.size();
    }
}
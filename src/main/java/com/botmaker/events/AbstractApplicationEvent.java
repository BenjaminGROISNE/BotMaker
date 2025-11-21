package com.botmaker.events;

/**
 * Abstract base class for events that provides common functionality
 */
public abstract class AbstractApplicationEvent implements ApplicationEvent {
    private final long timestamp;
    private final String source;

    protected AbstractApplicationEvent() {
        this(null);
    }

    protected AbstractApplicationEvent(String source) {
        this.timestamp = System.currentTimeMillis();
        this.source = source != null ? source : getClass().getSimpleName();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getSource() {
        return source;
    }
}
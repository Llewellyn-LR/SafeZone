package com.safezone.app.ui.common;

public final class Event<T> {
    private final T content;
    private boolean handled;

    public Event(T content) { this.content = content; }

    public T consume() {
        if (handled) return null;
        handled = true;
        return content;
    }
}

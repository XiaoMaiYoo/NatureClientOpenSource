package myau.events;

import myau.event.events.Event;

public class KeyEvent
implements Event {
    private final int keyCode;
    private final boolean pressed;

    public KeyEvent(int key, boolean pressed) {
        this.keyCode = key;
        this.pressed = pressed;
    }

    public int getKey() {
        return this.keyCode;
    }

    public boolean isPressed() {
        return this.pressed;
    }

    public boolean isReleased() {
        return !this.pressed;
    }
}

package myau.module.modules;

import myau.event.EventTarget;
import myau.events.Render2DEvent;
import myau.module.Module;
import myau.ui.NotificationManager;

public class NotificationModule
extends Module {
    public static final NotificationManager MANAGER = new NotificationManager();

    public NotificationModule() {
        super("Notifications", true, true);
    }

    @EventTarget
    public void onRender2DEvent(Render2DEvent event) {
        if (!this.enabled) {
            return;
        }
        MANAGER.render();
    }
}

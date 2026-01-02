package myau.ui;

import java.util.ArrayList;
import java.util.List;
import myau.config.Config;
import myau.ui.Notification;
import net.minecraft.client.gui.ScaledResolution;

public class NotificationManager {
    private final List<Notification> notifications = new ArrayList<Notification>();
    private static final int MAX_NOTIFICATIONS = 5;

    public void add(String text) {
        this.notifications.add(new Notification(text));
        while (this.notifications.size() > 5) {
            this.notifications.remove(0);
        }
    }

    public void render() {
        if (this.notifications.isEmpty()) {
            return;
        }
        ScaledResolution sr = new ScaledResolution(Config.mc);
        int startY = sr.func_78328_b() - 20;
        for (int i = 0; i < this.notifications.size(); ++i) {
            Notification n = this.notifications.get(i);
            n.render(10, startY - i * 16);
        }
        this.notifications.removeIf(Notification::isExpired);
    }
}

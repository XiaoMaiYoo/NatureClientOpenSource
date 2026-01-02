package myau.management;

import java.util.LinkedHashMap;
import myau.enums.FloatModules;
import myau.event.EventTarget;
import myau.events.PlayerUpdateEvent;
import net.minecraft.client.Minecraft;

public class FloatManager {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final LinkedHashMap<FloatModules, Boolean> activeMap = new LinkedHashMap();
    private boolean floating = false;

    public boolean isPredicted() {
        return this.floating;
    }

    public boolean isFalling() {
        return FloatManager.mc.field_71439_g.field_70122_E && FloatManager.mc.field_71439_g.field_70163_u - FloatManager.mc.field_71439_g.field_70137_T < 0.0 && FloatManager.mc.field_71439_g.field_70181_x < 0.0;
    }

    public boolean hasActiveModule() {
        return this.activeMap.containsValue(true);
    }

    public void setFloatState(boolean state, FloatModules floatModules) {
        this.activeMap.put(floatModules, state);
    }

    @EventTarget
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if ((this.hasActiveModule() || this.isPredicted()) && this.isFalling()) {
            FloatManager.mc.field_71439_g.func_70107_b(FloatManager.mc.field_71439_g.field_70165_t, FloatManager.mc.field_71439_g.field_70163_u + 0.001, FloatManager.mc.field_71439_g.field_70161_v);
            this.floating = true;
        } else {
            this.floating = false;
        }
    }
}

package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.StrafeEvent;
import myau.events.UpdateEvent;
import myau.module.Module;
import myau.property.properties.FloatProperty;
import myau.util.KeyBindUtil;
import myau.util.MoveUtil;
import net.minecraft.client.Minecraft;

public class Fly
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private double verticalMotion = 0.0;
    public final FloatProperty hSpeed = new FloatProperty("horizontal-speed", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(100.0f));
    public final FloatProperty vSpeed = new FloatProperty("vertical-speed", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(100.0f));

    public Fly() {
        super("Fly", false);
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (this.isEnabled()) {
            if (Fly.mc.field_71439_g.field_70163_u % 1.0 != 0.0) {
                Fly.mc.field_71439_g.field_70181_x = this.verticalMotion;
            }
            MoveUtil.setSpeed(0.0);
            event.setFriction((float)MoveUtil.getBaseMoveSpeed() * ((Float)this.hSpeed.getValue()).floatValue());
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            this.verticalMotion = 0.0;
            if (Fly.mc.field_71462_r == null) {
                if (KeyBindUtil.isKeyDown(Fly.mc.field_71474_y.field_74314_A.func_151463_i())) {
                    this.verticalMotion += ((Float)this.vSpeed.getValue()).doubleValue() * (double)0.42f;
                }
                if (KeyBindUtil.isKeyDown(Fly.mc.field_71474_y.field_74311_E.func_151463_i())) {
                    this.verticalMotion -= ((Float)this.vSpeed.getValue()).doubleValue() * (double)0.42f;
                }
                KeyBindUtil.setKeyBindState(Fly.mc.field_71474_y.field_74311_E.func_151463_i(), false);
            }
        }
    }

    @Override
    public void onDisabled() {
        Fly.mc.field_71439_g.field_70181_x = 0.0;
        MoveUtil.setSpeed(0.0);
        KeyBindUtil.updateKeyState(Fly.mc.field_71474_y.field_74311_E.func_151463_i());
    }
}

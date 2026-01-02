package myau.module.modules;

import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.SafeWalkEvent;
import myau.events.UpdateEvent;
import myau.module.Module;
import myau.module.modules.Scaffold;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.util.ItemUtil;
import myau.util.MoveUtil;
import myau.util.PlayerUtil;
import net.minecraft.client.Minecraft;

public class SafeWalk
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final FloatProperty motion = new FloatProperty("motion", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(1.0f));
    public final FloatProperty speedMotion = new FloatProperty("speed-motion", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(1.5f));
    public final BooleanProperty air = new BooleanProperty("air", false);
    public final BooleanProperty directionCheck = new BooleanProperty("direction-check", true);
    public final BooleanProperty pitCheck = new BooleanProperty("pitch-check", true);
    public final BooleanProperty requirePress = new BooleanProperty("require-press", false);
    public final BooleanProperty blocksOnly = new BooleanProperty("blocks-only", true);

    private boolean canSafeWalk() {
        Scaffold scaffold = (Scaffold)Myau.moduleManager.modules.get(Scaffold.class);
        if (scaffold.isEnabled()) {
            return false;
        }
        if (((Boolean)this.directionCheck.getValue()).booleanValue() && SafeWalk.mc.field_71474_y.field_74351_w.func_151470_d()) {
            return false;
        }
        if (((Boolean)this.pitCheck.getValue()).booleanValue() && SafeWalk.mc.field_71439_g.field_70125_A < 69.0f) {
            return false;
        }
        if (((Boolean)this.blocksOnly.getValue()).booleanValue() && !ItemUtil.isHoldingBlock()) {
            return false;
        }
        return ((Boolean)this.requirePress.getValue() == false || SafeWalk.mc.field_71474_y.field_74313_G.func_151470_d()) && (SafeWalk.mc.field_71439_g.field_70122_E && PlayerUtil.canMove(SafeWalk.mc.field_71439_g.field_70159_w, SafeWalk.mc.field_71439_g.field_70179_y, -1.0) || (Boolean)this.air.getValue() != false && PlayerUtil.canMove(SafeWalk.mc.field_71439_g.field_70159_w, SafeWalk.mc.field_71439_g.field_70179_y, -2.0));
    }

    public SafeWalk() {
        super("SafeWalk", false);
    }

    @EventTarget
    public void onMove(SafeWalkEvent event) {
        if (this.isEnabled() && this.canSafeWalk()) {
            event.setSafeWalk(true);
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE && SafeWalk.mc.field_71439_g.field_70122_E && MoveUtil.isForwardPressed() && this.canSafeWalk()) {
            if (MoveUtil.getSpeedLevel() <= 0) {
                if (((Float)this.motion.getValue()).floatValue() != 1.0f) {
                    MoveUtil.setSpeed(MoveUtil.getSpeed() * (double)((Float)this.motion.getValue()).floatValue());
                }
            } else if (((Float)this.speedMotion.getValue()).floatValue() != 1.0f) {
                MoveUtil.setSpeed(MoveUtil.getSpeed() * (double)((Float)this.speedMotion.getValue()).floatValue());
            }
        }
    }
}

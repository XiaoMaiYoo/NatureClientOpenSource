package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.util.ItemUtil;
import myau.util.KeyBindUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

public class AutoTool
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int currentToolSlot = -1;
    private int previousSlot = -1;
    private int tickDelayCounter = 0;
    public final IntProperty switchDelay = new IntProperty("delay", 0, 0, 5);
    public final BooleanProperty switchBack = new BooleanProperty("switch-back", true);
    public final BooleanProperty sneakOnly = new BooleanProperty("sneak-only", true);

    public AutoTool() {
        super("AutoTool", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.currentToolSlot != -1 && this.currentToolSlot != AutoTool.mc.field_71439_g.field_71071_by.field_70461_c) {
                this.currentToolSlot = -1;
                this.previousSlot = -1;
            }
            if (AutoTool.mc.field_71476_x != null && AutoTool.mc.field_71476_x.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK && AutoTool.mc.field_71474_y.field_74312_F.func_151470_d() && !AutoTool.mc.field_71439_g.func_71039_bw()) {
                int slot;
                if (this.tickDelayCounter >= (Integer)this.switchDelay.getValue() && (!((Boolean)this.sneakOnly.getValue()).booleanValue() || KeyBindUtil.isKeyDown(AutoTool.mc.field_71474_y.field_74311_E.func_151463_i())) && AutoTool.mc.field_71439_g.field_71071_by.field_70461_c != (slot = ItemUtil.findInventorySlot(AutoTool.mc.field_71439_g.field_71071_by.field_70461_c, AutoTool.mc.field_71441_e.func_180495_p(AutoTool.mc.field_71476_x.func_178782_a()).func_177230_c()))) {
                    if (this.previousSlot == -1) {
                        this.previousSlot = AutoTool.mc.field_71439_g.field_71071_by.field_70461_c;
                    }
                    AutoTool.mc.field_71439_g.field_71071_by.field_70461_c = this.currentToolSlot = slot;
                }
                ++this.tickDelayCounter;
            } else {
                if (((Boolean)this.switchBack.getValue()).booleanValue() && this.previousSlot != -1) {
                    AutoTool.mc.field_71439_g.field_71071_by.field_70461_c = this.previousSlot;
                }
                this.currentToolSlot = -1;
                this.previousSlot = -1;
                this.tickDelayCounter = 0;
            }
        }
    }

    @Override
    public void onDisabled() {
        this.currentToolSlot = -1;
        this.previousSlot = -1;
        this.tickDelayCounter = 0;
    }
}

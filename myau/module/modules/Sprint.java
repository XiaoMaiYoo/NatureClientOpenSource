package myau.module.modules;

import myau.event.EventTarget;
import myau.events.TickEvent;
import myau.mixin.IAccessorEntityLivingBase;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.util.KeyBindUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public class Sprint
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private boolean wasSprinting = false;
    public final BooleanProperty foxFix = new BooleanProperty("fov-fix", true);

    public Sprint() {
        super("Sprint", true, true);
    }

    public boolean shouldApplyFovFix(IAttributeInstance attribute) {
        if (!((Boolean)this.foxFix.getValue()).booleanValue()) {
            return false;
        }
        AttributeModifier attributeModifier = ((IAccessorEntityLivingBase)Sprint.mc.field_71439_g).getSprintingSpeedBoostModifier();
        return attribute.func_111127_a(attributeModifier.func_111167_a()) == null && this.wasSprinting;
    }

    public boolean shouldKeepFov(boolean boolean2) {
        return (Boolean)this.foxFix.getValue() != false && !boolean2 && this.wasSprinting;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            switch (event.getType()) {
                case PRE: {
                    KeyBindUtil.setKeyBindState(Sprint.mc.field_71474_y.field_151444_V.func_151463_i(), true);
                    break;
                }
                case POST: {
                    this.wasSprinting = Sprint.mc.field_71439_g.func_70051_ag();
                }
            }
        }
    }

    @Override
    public void onDisabled() {
        this.wasSprinting = false;
        KeyBindUtil.updateKeyState(Sprint.mc.field_71474_y.field_151444_V.func_151463_i());
    }
}

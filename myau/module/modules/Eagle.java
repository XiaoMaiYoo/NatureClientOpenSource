package myau.module.modules;

import java.util.Objects;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.MoveInputEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.util.ItemUtil;
import myau.util.MoveUtil;
import myau.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

public class Eagle
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int sneakDelay = 0;
    public final IntProperty minDelay = new IntProperty("min-delay", 2, 0, 10);
    public final IntProperty maxDelay = new IntProperty("max-delay", 3, 0, 10);
    public final BooleanProperty directionCheck = new BooleanProperty("direction-check", true);
    public final BooleanProperty pitchCheck = new BooleanProperty("pitch-check", true);
    public final BooleanProperty blocksOnly = new BooleanProperty("blocks-only", true);
    public final BooleanProperty sneakOnly = new BooleanProperty("sneaking-only", false);

    private boolean canMoveSafely() {
        double[] offset = MoveUtil.predictMovement();
        return PlayerUtil.canMove(Eagle.mc.field_71439_g.field_70159_w + offset[0], Eagle.mc.field_71439_g.field_70179_y + offset[1]);
    }

    private boolean shouldSneak() {
        if (((Boolean)this.directionCheck.getValue()).booleanValue() && Eagle.mc.field_71474_y.field_74351_w.func_151470_d()) {
            return false;
        }
        if (((Boolean)this.pitchCheck.getValue()).booleanValue() && Eagle.mc.field_71439_g.field_70125_A < 69.0f) {
            return false;
        }
        if (((Boolean)this.sneakOnly.getValue()).booleanValue() && !Keyboard.isKeyDown((int)Eagle.mc.field_71474_y.field_74311_E.func_151463_i())) {
            return false;
        }
        return ((Boolean)this.blocksOnly.getValue() == false || ItemUtil.isHoldingBlock()) && Eagle.mc.field_71439_g.field_70122_E;
    }

    public Eagle() {
        super("Eagle", false);
    }

    @EventTarget(value=4)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.sneakDelay > 0) {
                --this.sneakDelay;
            }
            if (this.sneakDelay == 0 && this.canMoveSafely()) {
                this.sneakDelay = RandomUtils.nextInt((Integer)this.minDelay.getValue(), (Integer)this.maxDelay.getValue() + 1);
            }
        }
    }

    @EventTarget(value=4)
    public void onMoveInput(MoveInputEvent event) {
        if (this.isEnabled() && Eagle.mc.field_71462_r == null) {
            if (((Boolean)this.sneakOnly.getValue()).booleanValue() && Keyboard.isKeyDown((int)Eagle.mc.field_71474_y.field_74311_E.func_151463_i()) && this.shouldSneak()) {
                Eagle.mc.field_71439_g.field_71158_b.field_78899_d = false;
                Eagle.mc.field_71439_g.field_71158_b.field_78900_b /= 0.3f;
                Eagle.mc.field_71439_g.field_71158_b.field_78902_a /= 0.3f;
            }
            if (!Eagle.mc.field_71439_g.field_71158_b.field_78899_d && this.shouldSneak() && (this.sneakDelay > 0 || this.canMoveSafely())) {
                Eagle.mc.field_71439_g.field_71158_b.field_78899_d = true;
                Eagle.mc.field_71439_g.field_71158_b.field_78902_a *= 0.3f;
                Eagle.mc.field_71439_g.field_71158_b.field_78900_b *= 0.3f;
            }
        }
    }

    @Override
    public void onDisabled() {
        this.sneakDelay = 0;
    }

    @Override
    public void verifyValue(String name) {
        switch (name) {
            case "min-delay": {
                if ((Integer)this.minDelay.getValue() <= (Integer)this.maxDelay.getValue()) break;
                this.maxDelay.setValue(this.minDelay.getValue());
                break;
            }
            case "max-delay": {
                if ((Integer)this.minDelay.getValue() <= (Integer)this.maxDelay.getValue()) break;
                this.minDelay.setValue(this.maxDelay.getValue());
            }
        }
    }

    @Override
    public String[] getSuffix() {
        String[] stringArray;
        if (Objects.equals(this.minDelay.getValue(), this.maxDelay.getValue())) {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = ((Integer)this.minDelay.getValue()).toString();
        } else {
            String[] stringArray3 = new String[1];
            stringArray = stringArray3;
            stringArray3[0] = String.format("%d-%d", this.minDelay.getValue(), this.maxDelay.getValue());
        }
        return stringArray;
    }
}

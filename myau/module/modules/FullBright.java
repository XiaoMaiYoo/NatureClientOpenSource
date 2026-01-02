package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class FullBright
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private float prevGamma = Float.NaN;
    private boolean appliedNightVision = false;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"GAMMA", "EFFECT"});

    public FullBright() {
        super("Fullbright", true, true);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST) {
            switch ((Integer)this.mode.getValue()) {
                case 0: {
                    FullBright.mc.field_71474_y.field_74333_Y = 1000.0f;
                    break;
                }
                case 1: {
                    FullBright.mc.field_71439_g.func_70690_d(new PotionEffect(Potion.field_76439_r.field_76415_H, 25940, 0));
                }
            }
        }
    }

    @Override
    public void onEnabled() {
        switch ((Integer)this.mode.getValue()) {
            case 0: {
                this.prevGamma = FullBright.mc.field_71474_y.field_74333_Y;
                break;
            }
            case 1: {
                this.appliedNightVision = true;
            }
        }
    }

    @Override
    public void onDisabled() {
        if (!Float.isNaN(this.prevGamma)) {
            FullBright.mc.field_71474_y.field_74333_Y = this.prevGamma;
            this.prevGamma = Float.NaN;
        }
        if (this.appliedNightVision) {
            if (FullBright.mc.field_71439_g != null) {
                FullBright.mc.field_71439_g.func_70618_n(Potion.field_76439_r.field_76415_H);
            }
            this.appliedNightVision = false;
        }
    }

    @Override
    public void verifyValue(String mode) {
        if (this.isEnabled()) {
            this.onDisabled();
            this.onEnabled();
        }
    }
}

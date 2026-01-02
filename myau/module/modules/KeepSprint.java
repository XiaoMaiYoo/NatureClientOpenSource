package myau.module.modules;

import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.PercentProperty;
import net.minecraft.client.Minecraft;

public class KeepSprint
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final PercentProperty slowdown = new PercentProperty("slowdown", 0);
    public final BooleanProperty groundOnly = new BooleanProperty("ground-only", false);
    public final BooleanProperty reachOnly = new BooleanProperty("reach-only", false);

    public KeepSprint() {
        super("KeepSprint", false);
    }

    public boolean shouldKeepSprint() {
        if (((Boolean)this.groundOnly.getValue()).booleanValue() && !KeepSprint.mc.field_71439_g.field_70122_E) {
            return false;
        }
        return (Boolean)this.reachOnly.getValue() == false || KeepSprint.mc.field_71476_x.field_72307_f.func_72438_d(mc.func_175606_aa().func_174824_e(1.0f)) > 3.0;
    }
}

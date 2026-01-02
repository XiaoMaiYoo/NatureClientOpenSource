package myau.module.modules;

import myau.event.EventTarget;
import myau.events.Render2DEvent;
import myau.module.Module;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.TextProperty;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;

public class Spammer
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final TimerUtil timer = new TimerUtil();
    private int charOffset = 19968;
    public final TextProperty text = new TextProperty("text", "meow");
    public final FloatProperty delay = new FloatProperty("delay", Float.valueOf(3.5f), Float.valueOf(0.0f), Float.valueOf(3600.0f));
    public final IntProperty random = new IntProperty("random", 0, 0, 10);

    public Spammer() {
        super("Spammer", false);
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (this.isEnabled() && this.timer.hasTimeElapsed((long)(((Float)this.delay.getValue()).floatValue() * 1000.0f))) {
            this.timer.reset();
            String text = (String)this.text.getValue();
            if ((Integer)this.random.getValue() > 0) {
                text = String.format("%s ", text);
                for (int i = 0; i < (Integer)this.random.getValue(); ++i) {
                    text = String.format("%s%s", text, Character.valueOf((char)this.charOffset));
                    ++this.charOffset;
                    if (this.charOffset <= 40959) continue;
                    this.charOffset = 19968;
                }
            }
            Spammer.mc.field_71439_g.func_71165_d(text);
        }
    }
}

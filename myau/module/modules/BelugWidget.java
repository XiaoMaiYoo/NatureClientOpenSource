package myau.module.modules;

import java.awt.Color;
import myau.event.EventTarget;
import myau.events.Render2DEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ColorProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.render.BlurShadowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class BelugWidget
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final BooleanProperty enabled = new BooleanProperty("Enabled", true);
    public final ModeProperty posX = new ModeProperty("Position-X", 1, new String[]{"Left", "Center", "Right"});
    public final ModeProperty posY = new ModeProperty("Position-Y", 1, new String[]{"Top", "Center", "Bottom"});
    public final IntProperty offsetX = new IntProperty("X-Offset", 0, -200, 200);
    public final IntProperty offsetY = new IntProperty("Y-Offset", 0, -200, 200);
    public final FloatProperty scale = new FloatProperty("Scale", Float.valueOf(1.0f), Float.valueOf(0.6f), Float.valueOf(2.0f));
    public final IntProperty blurStrength = new IntProperty("Blur Strength", 6, 1, 10);
    public final IntProperty cornerRadius = new IntProperty("Corner Radius", 8, 5, 20);
    public final IntProperty backgroundAlpha = new IntProperty("Background Alpha", 160, 0, 255);
    public final ColorProperty textColor = new ColorProperty("Text Color", Color.WHITE.getRGB());

    public BelugWidget() {
        super("Belug", false, false);
    }

    @EventTarget
    public void onRender2DEvent(Render2DEvent event) {
        if (!((Boolean)this.enabled.getValue()).booleanValue() || !this.isEnabled()) {
            return;
        }
        ScaledResolution sr = new ScaledResolution(mc);
        float scaleFactor = ((Float)this.scale.getValue()).floatValue();
        float baseX = 0.0f;
        float baseY = 0.0f;
        switch ((Integer)this.posX.getValue()) {
            case 0: {
                baseX = 10.0f;
                break;
            }
            case 1: {
                baseX = (float)sr.func_78326_a() / 2.0f;
                break;
            }
            case 2: {
                baseX = sr.func_78326_a() - 10;
            }
        }
        switch ((Integer)this.posY.getValue()) {
            case 0: {
                baseY = 10.0f;
                break;
            }
            case 1: {
                baseY = (float)sr.func_78328_b() / 2.0f;
                break;
            }
            case 2: {
                baseY = sr.func_78328_b() - 10;
            }
        }
        baseX += (float)((Integer)this.offsetX.getValue()).intValue();
        baseY += (float)((Integer)this.offsetY.getValue()).intValue();
        int fps = mc.func_175610_ah();
        String text = "FPS " + fps;
        int textWidth = BelugWidget.mc.field_71466_p.func_78256_a(text);
        int textHeight = BelugWidget.mc.field_71466_p.field_78288_b;
        float w = textWidth + 12;
        float h = textHeight + 6;
        float radius = ((Integer)this.cornerRadius.getValue()).intValue();
        GlStateManager.func_179094_E();
        GlStateManager.func_179152_a((float)scaleFactor, (float)scaleFactor, (float)1.0f);
        float drawX = baseX / scaleFactor;
        float drawY = baseY / scaleFactor;
        BlurShadowRenderer.renderFrostedGlass(drawX - w / 2.0f, drawY - h / 2.0f, w, h, radius, (Integer)this.blurStrength.getValue(), (Integer)this.backgroundAlpha.getValue());
        int color = (Integer)this.textColor.getValue();
        BelugWidget.mc.field_71466_p.func_175063_a(text, drawX - (float)textWidth / 2.0f, drawY - (float)textHeight / 2.0f, color);
        GlStateManager.func_179121_F();
    }
}

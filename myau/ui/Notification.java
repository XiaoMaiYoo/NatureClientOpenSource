package myau.ui;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class Notification {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final String text;
    private float alpha = 0.0f;
    private final long startTime = System.currentTimeMillis();
    private static final int DISPLAY_DURATION_MS = 1000;
    private static final int FADE_DURATION_MS = 250;
    private static final int PADDING = 4;
    private static final int TEXT_COLOR = 0xFFFFFF;

    public Notification(String text) {
        this.text = text != null ? text : "";
    }

    public void render(int x, int y) {
        long time = System.currentTimeMillis() - this.startTime;
        if (time < 250L) {
            this.alpha = (float)time / 250.0f;
        } else if (time < 1250L) {
            this.alpha = 1.0f;
        } else if (time < 1500L) {
            this.alpha = 1.0f - (float)(time - 1000L - 250L) / 250.0f;
        } else {
            this.alpha = 0.0f;
            return;
        }
        int textWidth = Notification.mc.field_71466_p.func_78256_a(this.text);
        int boxWidth = textWidth + 8;
        int boxHeight = Notification.mc.field_71466_p.field_78288_b + 8;
        this.renderBlur(x, y, boxWidth, boxHeight, 8);
        int textColor = 0xFFFFFF | (int)(this.alpha * 255.0f) << 24;
        Notification.mc.field_71466_p.func_175063_a(this.text, (float)(x + 4), (float)(y + 4), textColor);
    }

    public boolean isExpired() {
        return this.alpha <= 0.0f;
    }

    private void renderBlur(float x, float y, float w, float h, int radius) {
        if (radius <= 0) {
            return;
        }
        GlStateManager.func_179090_x();
        GlStateManager.func_179147_l();
        GlStateManager.func_179112_b((int)770, (int)771);
        GlStateManager.func_179118_c();
        for (int i = 0; i < radius; ++i) {
            float a = (1.0f - (float)i / (float)radius) * 0.15f;
            int color = new Color(0, 0, 0, (int)(255.0f * a)).getRGB();
            this.drawRect(x - (float)i, y, x + w + (float)i, y + h, color);
            this.drawRect(x, y - (float)i, x + w, y + h + (float)i, color);
        }
        GlStateManager.func_179141_d();
        GlStateManager.func_179098_w();
        GlStateManager.func_179084_k();
    }

    private void drawRect(float x1, float y1, float x2, float y2, int color) {
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        GlStateManager.func_179147_l();
        GlStateManager.func_179090_x();
        GlStateManager.func_179120_a((int)770, (int)771, (int)1, (int)0);
        GlStateManager.func_179131_c((float)red, (float)green, (float)blue, (float)alpha);
        GL11.glBegin((int)7);
        GL11.glVertex2f((float)x1, (float)y1);
        GL11.glVertex2f((float)x2, (float)y1);
        GL11.glVertex2f((float)x2, (float)y2);
        GL11.glVertex2f((float)x1, (float)y2);
        GL11.glEnd();
        GlStateManager.func_179098_w();
        GlStateManager.func_179084_k();
    }
}

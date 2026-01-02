package myau.ui.components;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;
import myau.enums.ChatColors;
import myau.property.properties.ColorProperty;
import myau.ui.Component;
import myau.ui.components.ModuleComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class ColorSliderComponent
implements Component {
    private final ModuleComponent parentModule;
    private final ColorProperty property;
    private int offsetY;
    private boolean draggingHue;
    private boolean draggingSat;
    private boolean draggingBri;
    private float hue;
    private float saturation;
    private float brightness;

    public ColorSliderComponent(ColorProperty property, ModuleComponent parentModule, int offsetY) {
        this.parentModule = parentModule;
        this.offsetY = offsetY;
        this.property = property;
        Color c = new Color((Integer)property.getValue());
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    @Override
    public void draw(AtomicInteger offset) {
        int x = this.parentModule.category.getX() + 4;
        int y = this.parentModule.category.getY() + this.offsetY;
        int width = this.parentModule.category.getWidth() - 8;
        GL11.glPushMatrix();
        GL11.glScaled((double)0.5, (double)0.5, (double)0.5);
        Minecraft.func_71410_x().field_71466_p.func_175063_a(this.property.getName().replace("-", " ") + ": " + ChatColors.formatColor(this.property.formatValue()), (float)(x * 2), (float)((int)((float)(this.parentModule.category.getY() + this.offsetY + 3) * 2.0f)), -1);
        GL11.glPopMatrix();
        if (!(this.draggingHue || this.draggingSat || this.draggingBri)) {
            Color color = new Color((Integer)this.property.getValue());
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            this.hue = hsb[0];
            this.saturation = hsb[1];
            this.brightness = hsb[2];
        }
        int colorPreviewSize = 6;
        int colorPreviewX = x + width - colorPreviewSize;
        int colorPreviewY = y + 2;
        int previewColor = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
        Gui.func_73734_a((int)(colorPreviewX - 6), (int)colorPreviewY, (int)(colorPreviewX + colorPreviewSize), (int)(colorPreviewY + colorPreviewSize), (int)previewColor);
        int baseY = y + 10;
        int satY = baseY + 4 + 2;
        int briY = satY + 4 + 2;
        this.drawHueBar(x, baseY, width);
        this.drawPointer(x, baseY, width, this.hue);
        this.drawGradientRect(x, satY, x + width, satY + 4, Color.WHITE.getRGB(), Color.getHSBColor(this.hue, 1.0f, 1.0f).getRGB());
        this.drawPointer(x, satY, width, this.saturation);
        this.drawGradientRect(x, briY, x + width, briY + 4, Color.BLACK.getRGB(), Color.getHSBColor(this.hue, this.saturation, 1.0f).getRGB());
        this.drawPointer(x, briY, width, this.brightness);
    }

    private void drawHueBar(int x, int y, int width) {
        for (int i = 0; i < width; ++i) {
            float hue = (float)i / (float)width;
            int color = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            Gui.func_73734_a((int)(x + i), (int)y, (int)(x + i + 1), (int)(y + 4), (int)color);
        }
    }

    private void drawPointer(int x, int y, int width, float value) {
        int posX = x + (int)((float)width * value);
        Gui.func_73734_a((int)(posX - 1), (int)y, (int)posX, (int)(y + 4), (int)new Color(0, 0, 0, 200).getRGB());
    }

    @Override
    public void update(int mouseX, int mouseY) {
        int baseX = this.parentModule.category.getX() + 4;
        int width = this.parentModule.category.getWidth() - 8;
        boolean changed = false;
        if (this.draggingHue) {
            this.hue = this.getSliderValue(mouseX, baseX, width);
            changed = true;
        }
        if (this.draggingSat) {
            this.saturation = this.getSliderValue(mouseX, baseX, width);
            changed = true;
        }
        if (this.draggingBri) {
            this.brightness = this.getSliderValue(mouseX, baseX, width);
            changed = true;
        }
        if (changed) {
            int signed = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
            this.property.setValue(new Color(signed).getRGB());
        }
    }

    private float getSliderValue(int mouseX, int startX, int width) {
        double d = Math.min(width, Math.max(0, mouseX - startX));
        return (float)ColorSliderComponent.roundToPrecision(d / (double)width, 3);
    }

    private static double roundToPrecision(double v, int precision) {
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(precision, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (button != 0 || !this.parentModule.panelExpand) {
            return;
        }
        int baseY = this.parentModule.category.getY() + this.offsetY + 10;
        if (this.isHovered(mouseX, mouseY, baseY)) {
            this.draggingHue = true;
        } else if (this.isHovered(mouseX, mouseY, baseY + 4 + 2)) {
            this.draggingSat = true;
        } else if (this.isHovered(mouseX, mouseY, baseY + 12)) {
            this.draggingBri = true;
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        this.draggingBri = false;
        this.draggingSat = false;
        this.draggingHue = false;
    }

    private boolean isHovered(int mx, int my, int sliderY) {
        int startX = this.parentModule.category.getX() + 4;
        int endX = startX + this.parentModule.category.getWidth() - 8;
        return mx >= startX && mx <= endX && my >= sliderY && my <= sliderY + 4;
    }

    @Override
    public boolean isVisible() {
        return this.property.isVisible();
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
    }

    @Override
    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 27;
    }

    private void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float sa = (float)(startColor >> 24 & 0xFF) / 255.0f;
        float sr = (float)(startColor >> 16 & 0xFF) / 255.0f;
        float sg = (float)(startColor >> 8 & 0xFF) / 255.0f;
        float sb = (float)(startColor & 0xFF) / 255.0f;
        float ea = (float)(endColor >> 24 & 0xFF) / 255.0f;
        float er = (float)(endColor >> 16 & 0xFF) / 255.0f;
        float eg = (float)(endColor >> 8 & 0xFF) / 255.0f;
        float eb = (float)(endColor & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.func_178181_a();
        WorldRenderer world = tessellator.func_178180_c();
        GL11.glDisable((int)3553);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3008);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glShadeModel((int)7425);
        world.func_181668_a(7, DefaultVertexFormats.field_181706_f);
        world.func_181662_b((double)right, (double)top, 0.0).func_181666_a(er, eg, eb, ea).func_181675_d();
        world.func_181662_b((double)left, (double)top, 0.0).func_181666_a(sr, sg, sb, sa).func_181675_d();
        world.func_181662_b((double)left, (double)bottom, 0.0).func_181666_a(sr, sg, sb, sa).func_181675_d();
        world.func_181662_b((double)right, (double)bottom, 0.0).func_181666_a(er, eg, eb, ea).func_181675_d();
        tessellator.func_78381_a();
        GL11.glShadeModel((int)7424);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)3008);
        GL11.glEnable((int)3553);
    }
}

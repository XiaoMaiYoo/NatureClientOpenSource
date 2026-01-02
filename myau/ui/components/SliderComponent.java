package myau.ui.components;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;
import myau.ui.Component;
import myau.ui.components.ModuleComponent;
import myau.ui.dataset.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

public class SliderComponent
implements Component {
    private final Slider slider;
    private final ModuleComponent parentModule;
    private int offsetY;
    private int x;
    private int y;
    private boolean dragging = false;
    private double sliderWidth;

    public SliderComponent(Slider slider, ModuleComponent parentModule, int offsetY) {
        this.slider = slider;
        this.parentModule = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    @Override
    public void draw(AtomicInteger offset) {
        Gui.func_73734_a((int)(this.parentModule.category.getX() + 4), (int)(this.parentModule.category.getY() + this.offsetY + 11), (int)(this.parentModule.category.getX() + 4 + this.parentModule.category.getWidth() - 8), (int)(this.parentModule.category.getY() + this.offsetY + 15), (int)-12302777);
        int sliderStart = this.parentModule.category.getX() + 4;
        int sliderEnd = this.parentModule.category.getX() + 4 + (int)this.sliderWidth;
        if (sliderEnd - sliderStart > 84) {
            sliderEnd = sliderStart + 84;
        }
        long time = System.currentTimeMillis() + (long)(offset.get() * 200);
        float hue = (float)(time % 2000L) / 2000.0f;
        int rainbowColor = new Color(Color.HSBtoRGB(hue, 0.8f, 0.9f)).getRGB();
        Gui.func_73734_a((int)sliderStart, (int)(this.parentModule.category.getY() + this.offsetY + 11), (int)sliderEnd, (int)(this.parentModule.category.getY() + this.offsetY + 15), (int)rainbowColor);
        GL11.glPushMatrix();
        GL11.glScaled((double)0.5, (double)0.5, (double)0.5);
        Minecraft.func_71410_x().field_71466_p.func_175063_a(this.slider.getName() + ": " + this.slider.getValueString(), (float)((int)((float)(this.parentModule.category.getX() + 4) * 2.0f)), (float)((int)((float)(this.parentModule.category.getY() + this.offsetY + 3) * 2.0f)), -1);
        GL11.glPopMatrix();
    }

    @Override
    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();
        double d = Math.min(this.parentModule.category.getWidth() - 8, Math.max(0, mousePosX - this.x));
        this.sliderWidth = (double)(this.parentModule.category.getWidth() - 8) * (this.slider.getInput() - this.slider.getMin()) / (this.slider.getMax() - this.slider.getMin());
        if (this.dragging) {
            if (d == 0.0) {
                this.slider.setValue(this.slider.getMin());
            } else {
                double rawValue = d / (double)(this.parentModule.category.getWidth() - 8) * (this.slider.getMax() - this.slider.getMin()) + this.slider.getMin();
                double increment = this.slider.getIncrement();
                if (increment > 0.0) {
                    rawValue = (double)Math.round(rawValue / increment) * increment;
                }
                double n = SliderComponent.roundToPrecision(rawValue, 2);
                n = Math.max(this.slider.getMin(), Math.min(this.slider.getMax(), n));
                this.slider.setValue(n);
            }
        }
    }

    private static double roundToPrecision(double v, int precision) {
        if (precision < 0) {
            return 0.0;
        }
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(precision, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void mouseDown(int x, int y, int button) {
        if (this.isLeftHalfHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            this.dragging = true;
        }
        if (this.isRightHalfHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            this.dragging = true;
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        this.dragging = false;
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
    }

    public boolean isLeftHalfHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth() / 2 + 1 && y > this.y && y < this.y + 16;
    }

    public boolean isRightHalfHovered(int x, int y) {
        return x > this.x + this.parentModule.category.getWidth() / 2 && x < this.x + this.parentModule.category.getWidth() && y > this.y && y < this.y + 16;
    }

    @Override
    public boolean isVisible() {
        return this.slider.isVisible();
    }
}

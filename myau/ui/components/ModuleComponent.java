package myau.ui.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import myau.Myau;
import myau.module.Module;
import myau.property.Property;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ColorProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.property.properties.TextProperty;
import myau.ui.Component;
import myau.ui.components.BindComponent;
import myau.ui.components.CategoryComponent;
import myau.ui.components.CheckBoxComponent;
import myau.ui.components.ColorSliderComponent;
import myau.ui.components.ModeComponent;
import myau.ui.components.SliderComponent;
import myau.ui.components.TextComponent;
import myau.ui.dataset.impl.FloatSlider;
import myau.ui.dataset.impl.IntSlider;
import myau.ui.dataset.impl.PercentageSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class ModuleComponent
implements Component {
    public Module mod;
    public CategoryComponent category;
    public int offsetY;
    private final ArrayList<Component> settings;
    public boolean panelExpand;

    public ModuleComponent(Module mod, CategoryComponent category, int offsetY) {
        this.mod = mod;
        this.category = category;
        this.offsetY = offsetY;
        this.settings = new ArrayList();
        this.panelExpand = false;
        int y = offsetY + 12;
        if (!Myau.propertyManager.properties.get(mod.getClass()).isEmpty()) {
            for (Property<?> baseProperty : Myau.propertyManager.properties.get(mod.getClass())) {
                Component c;
                Property property;
                if (baseProperty instanceof BooleanProperty) {
                    property = (BooleanProperty)baseProperty;
                    c = new CheckBoxComponent((BooleanProperty)property, this, y);
                    this.settings.add(c);
                    y += ((CheckBoxComponent)c).getHeight();
                    continue;
                }
                if (baseProperty instanceof FloatProperty) {
                    property = (FloatProperty)baseProperty;
                    c = new SliderComponent(new FloatSlider((FloatProperty)property), this, y);
                    this.settings.add(c);
                    y += ((SliderComponent)c).getHeight();
                    continue;
                }
                if (baseProperty instanceof IntProperty) {
                    property = (IntProperty)baseProperty;
                    c = new SliderComponent(new IntSlider((IntProperty)property), this, y);
                    this.settings.add(c);
                    y += ((SliderComponent)c).getHeight();
                    continue;
                }
                if (baseProperty instanceof PercentProperty) {
                    property = (PercentProperty)baseProperty;
                    c = new SliderComponent(new PercentageSlider((PercentProperty)property), this, y);
                    this.settings.add(c);
                    y += ((SliderComponent)c).getHeight();
                    continue;
                }
                if (baseProperty instanceof ModeProperty) {
                    property = (ModeProperty)baseProperty;
                    c = new ModeComponent((ModeProperty)property, this, y);
                    this.settings.add(c);
                    y += ((ModeComponent)c).getHeight();
                    continue;
                }
                if (baseProperty instanceof ColorProperty) {
                    property = (ColorProperty)baseProperty;
                    c = new ColorSliderComponent((ColorProperty)property, this, y);
                    this.settings.add(c);
                    y += ((ColorSliderComponent)c).getHeight();
                    continue;
                }
                if (!(baseProperty instanceof TextProperty)) continue;
                property = (TextProperty)baseProperty;
                c = new TextComponent((TextProperty)property, this, y);
                this.settings.add(c);
                y += ((TextComponent)c).getHeight();
            }
        }
        this.settings.add(new BindComponent(this, y));
    }

    @Override
    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
        int y = this.offsetY + 16;
        for (Component c : this.settings) {
            c.setComponentStartAt(y);
            if (!c.isVisible()) continue;
            y += c.getHeight();
        }
    }

    @Override
    public void draw(AtomicInteger offset) {
        int textColor = this.mod.isEnabled() ? new Color(102, 204, 255).getRGB() : new Color(170, 170, 170).getRGB();
        Minecraft.func_71410_x().field_71466_p.func_175063_a(this.mod.getName(), (float)(this.category.getX() + 8), (float)(this.category.getY() + this.offsetY + 4), textColor);
        if (this.panelExpand && !this.settings.isEmpty()) {
            this.drawSettingsPanel(offset);
        }
    }

    private void drawSettingsPanel(AtomicInteger offset) {
        int x = this.category.getX();
        int y = this.category.getY() + this.offsetY + 16;
        int width = this.category.getWidth();
        int height = this.getHeight() - 16;
        int panelBg = new Color(20, 20, 25, 200).getRGB();
        Gui.func_73734_a((int)x, (int)y, (int)(x + width), (int)(y + height), (int)panelBg);
        int offsetY = 0;
        for (Component c : this.settings) {
            if (!c.isVisible()) continue;
            c.draw(offset);
            offsetY += c.getHeight();
        }
    }

    @Override
    public int getHeight() {
        if (!this.panelExpand) {
            return 16;
        }
        int h = 16;
        for (Component c : this.settings) {
            if (!c.isVisible()) continue;
            h += c.getHeight();
        }
        return h;
    }

    @Override
    public void update(int mousePosX, int mousePosY) {
        if (!this.panelExpand) {
            return;
        }
        if (!this.settings.isEmpty()) {
            for (Component c : this.settings) {
                if (!c.isVisible()) continue;
                c.update(mousePosX, mousePosY);
            }
        }
    }

    @Override
    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0) {
            this.mod.toggle();
        }
        if (this.isHovered(x, y) && button == 1) {
            boolean bl = this.panelExpand = !this.panelExpand;
        }
        if (!this.panelExpand) {
            return;
        }
        for (Component c : this.settings) {
            if (!c.isVisible()) continue;
            c.mouseDown(x, y, button);
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        if (!this.panelExpand) {
            return;
        }
        for (Component c : this.settings) {
            if (!c.isVisible()) continue;
            c.mouseReleased(x, y, button);
        }
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
        if (!this.panelExpand) {
            return;
        }
        for (Component c : this.settings) {
            if (!c.isVisible()) continue;
            c.keyTyped(chatTyped, keyCode);
        }
    }

    public boolean isHovered(int x, int y) {
        return x > this.category.getX() && x < this.category.getX() + this.category.getWidth() && y > this.category.getY() + this.offsetY && y < this.category.getY() + 16 + this.offsetY;
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}

package myau.ui.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import myau.module.Module;
import myau.ui.Component;
import myau.ui.components.ModuleComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public class CategoryComponent {
    private final int MAX_HEIGHT = 300;
    public ArrayList<Component> modulesInCategory = new ArrayList();
    public String categoryName;
    private boolean categoryOpened;
    private int width;
    private int y;
    private int x;
    private final int bh;
    public boolean dragging;
    public int xx;
    public int yy;
    public boolean pin = false;
    private double marginY;
    private double marginX;
    private int scroll = 0;
    private double animScroll = 0.0;
    private int height = 0;

    public CategoryComponent(String category, List<Module> modules) {
        this.categoryName = category;
        this.width = 92;
        this.x = 5;
        this.y = 5;
        this.bh = 13;
        this.xx = 0;
        this.categoryOpened = false;
        this.dragging = false;
        int tY = this.bh + 3;
        this.marginX = 80.0;
        this.marginY = 4.5;
        for (Module mod : modules) {
            ModuleComponent b = new ModuleComponent(mod, this, tY);
            this.modulesInCategory.add(b);
            tY += 16;
        }
    }

    public ArrayList<Component> getModules() {
        return this.modulesInCategory;
    }

    public void setX(int n) {
        this.x = n;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void mousePressed(boolean d) {
        this.dragging = d;
    }

    public boolean isPin() {
        return this.pin;
    }

    public void setPin(boolean on) {
        this.pin = on;
    }

    public boolean isOpened() {
        return this.categoryOpened;
    }

    public void setOpened(boolean on) {
        this.categoryOpened = on;
    }

    public void render(FontRenderer renderer) {
        this.width = 92;
        this.update();
        this.height = 0;
        for (Component moduleRenderManager : this.modulesInCategory) {
            this.height += moduleRenderManager.getHeight();
        }
        int maxScroll = Math.max(0, this.height - 300);
        if (this.scroll > maxScroll) {
            this.scroll = maxScroll;
        }
        if (this.animScroll > (double)maxScroll) {
            this.animScroll = maxScroll;
        }
        this.animScroll += ((double)this.scroll - this.animScroll) * 0.2;
        if (!this.modulesInCategory.isEmpty() && this.categoryOpened) {
            int displayHeight = Math.min(this.height, 300);
            Gui.func_73734_a((int)(this.x - 1), (int)this.y, (int)(this.x + this.width + 1), (int)(this.y + this.bh + displayHeight + 4), (int)new Color(0, 0, 0, 100).getRGB());
        }
        Gui.func_73734_a((int)(this.x - 2), (int)this.y, (int)(this.x + this.width + 2), (int)(this.y + this.bh + 3), (int)new Color(0, 0, 0, 200).getRGB());
        renderer.func_175065_a(this.categoryName, (float)(this.x + 2), (float)(this.y + 4), -1, false);
        renderer.func_175065_a(this.categoryOpened ? "-" : "+", (float)((double)this.x + this.marginX), (float)((double)this.y + this.marginY), Color.white.getRGB(), false);
        if (this.categoryOpened && !this.modulesInCategory.isEmpty()) {
            int renderHeight = 0;
            ScaledResolution sr = new ScaledResolution(Minecraft.func_71410_x());
            double scale = sr.func_78325_e();
            int bottom = this.y + this.bh + 300 + 3;
            GL11.glEnable((int)3089);
            GL11.glScissor((int)((int)((double)this.x * scale)), (int)((int)((double)(sr.func_78328_b() - bottom) * scale)), (int)((int)((double)this.width * scale)), (int)((int)(300.0 * scale)));
            for (Component c2 : this.modulesInCategory) {
                int compHeight = c2.getHeight();
                if ((double)(renderHeight + compHeight) > this.animScroll && (double)renderHeight < this.animScroll + 300.0) {
                    int drawY = (int)((double)renderHeight - this.animScroll);
                    c2.setComponentStartAt(this.bh + 3 + drawY);
                    c2.draw(new AtomicInteger(0));
                }
                renderHeight += compHeight;
            }
            GL11.glDisable((int)3089);
            if (this.height > 300) {
                float scrollY = (float)this.y + (float)this.bh + 3.0f + (float)(this.animScroll * 300.0 / (double)this.height);
                Gui.func_73734_a((int)(this.x + this.width - 2), (int)((int)scrollY), (int)(this.x + this.width), (int)((int)(scrollY + 90000.0f / (float)this.height)), (int)new Color(255, 255, 255, 60).getRGB());
            }
        }
    }

    public void update() {
        int offset = this.bh + 3;
        for (Component component : this.modulesInCategory) {
            component.setComponentStartAt(offset);
            offset += component.getHeight();
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public void handleDrag(int x, int y) {
        if (this.dragging) {
            this.setX(x - this.xx);
            this.setY(y - this.yy);
        }
    }

    public boolean isHovered(int x, int y) {
        return x >= this.x + 92 - 13 && x <= this.x + this.width && (float)y >= (float)this.y + 2.0f && y <= this.y + this.bh + 1;
    }

    public boolean mousePressed(int x, int y) {
        return x >= this.x + 77 && x <= this.x + this.width - 6 && (float)y >= (float)this.y + 2.0f && y <= this.y + this.bh + 1;
    }

    public boolean insideArea(int x, int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.bh;
    }

    public String getName() {
        return this.categoryName;
    }

    public void setLocation(int parseInt, int parseInt1) {
        this.x = parseInt;
        this.y = parseInt1;
    }

    public void onScroll(int mouseX, int mouseY, int scrollAmount) {
        if (!this.categoryOpened || this.height <= 300) {
            return;
        }
        int areaTop = this.y + this.bh;
        int areaBottom = this.y + this.bh + 300;
        if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= areaTop && mouseY <= areaBottom) {
            this.scroll -= scrollAmount * 12;
            this.scroll = Math.max(0, Math.min(this.scroll, this.height - 300));
        }
    }
}

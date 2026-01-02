package myau.ui.components;

import java.util.concurrent.atomic.AtomicInteger;
import myau.enums.ChatColors;
import myau.property.properties.ModeProperty;
import myau.ui.Component;
import myau.ui.components.ModuleComponent;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class ModeComponent
implements Component {
    private final ModeProperty property;
    private final ModuleComponent parentModule;
    private int x;
    private int y;
    private int offsetY;

    public ModeComponent(ModeProperty desc, ModuleComponent parentModule, int offsetY) {
        this.property = desc;
        this.parentModule = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    @Override
    public void draw(AtomicInteger offset) {
        GL11.glPushMatrix();
        GL11.glScaled((double)0.5, (double)0.5, (double)0.5);
        String mode = this.property.getModeString();
        mode = mode.replace("_", " ");
        int bruhWidth = (int)((double)Minecraft.func_71410_x().field_71466_p.func_78256_a(this.property.getName() + ": ") * 0.5);
        Minecraft.func_71410_x().field_71466_p.func_175065_a(this.property.getName() + ": ", (float)((this.parentModule.category.getX() + 4) * 2), (float)((this.parentModule.category.getY() + this.offsetY + 4) * 2), -1, true);
        Minecraft.func_71410_x().field_71466_p.func_175065_a(ChatColors.formatColor("&9" + mode.substring(0, 1).toUpperCase() + mode.substring(1).toLowerCase()), (float)((this.parentModule.category.getX() + 4 + bruhWidth) * 2), (float)((this.parentModule.category.getY() + this.offsetY + 4) * 2), -1, true);
        GL11.glPopMatrix();
    }

    @Override
    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();
    }

    @Override
    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 12;
    }

    @Override
    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y)) {
            if (button == 0) {
                this.property.nextMode();
            } else if (button == 1) {
                this.property.previousMode();
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
    }

    private boolean isHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth() && y > this.y && y < this.y + 11;
    }

    @Override
    public boolean isVisible() {
        return this.property.isVisible();
    }
}

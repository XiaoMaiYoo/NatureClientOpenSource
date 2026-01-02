package myau.module.modules;

import myau.module.Module;
import myau.ui.ClickGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class GuiModule
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private ClickGui clickGui;

    public GuiModule() {
        super("ClickGui", false);
        this.setKey(54);
    }

    @Override
    public void onEnabled() {
        this.setEnabled(false);
        if (this.clickGui == null) {
            this.clickGui = new ClickGui();
        }
        mc.func_147108_a((GuiScreen)this.clickGui);
    }
}

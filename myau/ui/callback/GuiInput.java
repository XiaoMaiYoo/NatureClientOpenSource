package myau.ui.callback;

import java.io.IOException;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiInput
extends GuiScreen {
    private final String title;
    private final String defaultValue;
    private final Consumer<String> callback;
    private GuiTextField textField;
    private GuiButton buttonOk;
    private GuiScreen caller;

    public GuiInput(String title, String defaultValue, Consumer<String> callback, GuiScreen caller) {
        this.title = title;
        this.defaultValue = defaultValue;
        this.callback = callback;
        this.caller = caller;
    }

    public static void prompt(String title, String defaultValue, Consumer<String> callback, GuiScreen caller) {
        Minecraft.func_71410_x().func_147108_a((GuiScreen)new GuiInput(title, defaultValue, callback, caller));
    }

    public void func_73866_w_() {
        int centerX = this.field_146294_l / 2;
        int centerY = this.field_146295_m / 2;
        this.textField = new GuiTextField(0, this.field_146289_q, centerX - 100, centerY - 10, 200, 20);
        this.textField.func_146180_a(this.defaultValue);
        this.textField.func_146195_b(true);
        this.buttonOk = new GuiButton(0, centerX - 100, centerY + 20, 95, 20, "Confirm");
        this.field_146292_n.add(this.buttonOk);
        this.field_146292_n.add(new GuiButton(1, centerX + 5, centerY + 20, 95, 20, "Cancel"));
    }

    protected void func_146284_a(GuiButton button) {
        if (button == this.buttonOk && this.callback != null) {
            this.callback.accept(this.textField.func_146179_b());
        }
        this.field_146297_k.func_147108_a(this.caller);
    }

    protected void func_73869_a(char typedChar, int keyCode) {
        this.textField.func_146201_a(typedChar, keyCode);
    }

    protected void func_73864_a(int mouseX, int mouseY, int mouseButton) {
        try {
            super.func_73864_a(mouseX, mouseY, mouseButton);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.textField.func_146192_a(mouseX, mouseY, mouseButton);
    }

    public void func_73863_a(int mouseX, int mouseY, float partialTicks) {
        this.func_146276_q_();
        this.func_73732_a(this.field_146289_q, this.title, this.field_146294_l / 2, this.field_146295_m / 2 - 35, 0xFFFFFF);
        this.textField.func_146194_f();
        super.func_73863_a(mouseX, mouseY, partialTicks);
    }

    public void func_73876_c() {
        this.textField.func_146178_a();
    }
}

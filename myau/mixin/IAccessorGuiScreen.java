package myau.mixin;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@SideOnly(value=Side.CLIENT)
@Mixin(value={GuiScreen.class})
public interface IAccessorGuiScreen {
    @Invoker(value="mouseClicked")
    public void callMouseClicked(int var1, int var2, int var3);
}

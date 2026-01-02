package myau.mixin;

import myau.Myau;
import myau.module.modules.NickHider;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SideOnly(value=Side.CLIENT)
@Mixin(value={FontRenderer.class})
public abstract class MixinFontRenderer {
    @ModifyVariable(method={"renderString"}, at=@At(value="HEAD"), ordinal=0, argsOnly=true)
    private String renderString(String string) {
        if (Myau.moduleManager == null) {
            return string;
        }
        NickHider nickHider = (NickHider)Myau.moduleManager.modules.get(NickHider.class);
        return nickHider != null && nickHider.isEnabled() ? nickHider.replaceNick(string) : string;
    }

    @ModifyVariable(method={"getStringWidth"}, at=@At(value="HEAD"), ordinal=0, argsOnly=true)
    private String getStringWidth(String string) {
        if (Myau.moduleManager == null) {
            return string;
        }
        NickHider nickHider = (NickHider)Myau.moduleManager.modules.get(NickHider.class);
        return nickHider != null && nickHider.isEnabled() ? nickHider.replaceNick(string) : string;
    }
}

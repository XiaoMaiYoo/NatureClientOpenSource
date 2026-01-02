package myau.mixin;

import java.nio.IntBuffer;
import myau.Myau;
import myau.module.modules.Xray;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(value=Side.CLIENT)
@Mixin(value={WorldRenderer.class})
public abstract class MixinWorldRenderer {
    @Redirect(method={"putColorMultiplier"}, at=@At(value="INVOKE", target="java/nio/IntBuffer.put(II)Ljava/nio/IntBuffer;", remap=false))
    private IntBuffer putColorMultiplier(IntBuffer intBuffer, int integer2, int integer3) {
        if (Myau.moduleManager == null) {
            return intBuffer.put(integer2, integer3);
        }
        Xray xray = (Xray)Myau.moduleManager.modules.get(Xray.class);
        return xray.isEnabled() ? intBuffer.put(integer2, integer3 & 0xFFFFFF | (int)((float)((Integer)xray.opacity.getValue()).intValue() * 255.0f / 100.0f) << 24) : intBuffer.put(integer2, integer3);
    }
}

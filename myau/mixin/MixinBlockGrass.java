package myau.mixin;

import myau.Myau;
import myau.module.modules.Xray;
import net.minecraft.block.BlockGrass;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(value=Side.CLIENT)
@Mixin(value={BlockGrass.class})
public abstract class MixinBlockGrass {
    @Inject(method={"getBlockLayer"}, at={@At(value="HEAD")}, cancellable=true)
    private void getBlockLayer(CallbackInfoReturnable<EnumWorldBlockLayer> callbackInfoReturnable) {
        if (Myau.moduleManager != null && Myau.moduleManager.modules.get(Xray.class).isEnabled()) {
            callbackInfoReturnable.setReturnValue(EnumWorldBlockLayer.TRANSLUCENT);
        }
    }
}

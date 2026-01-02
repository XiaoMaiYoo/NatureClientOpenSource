package myau.mixin;

import myau.Myau;
import myau.module.modules.Xray;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(value=Side.CLIENT)
@Mixin(value={Block.class})
public abstract class MixinBlock {
    @Inject(method={"shouldSideBeRendered"}, at={@At(value="HEAD")}, cancellable=true)
    private void shouldSideBeRendered(IBlockAccess iBlockAccess, BlockPos blockPos, EnumFacing enumFacing, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        BlockPos block;
        Xray xray;
        if (Myau.moduleManager != null && (xray = (Xray)Myau.moduleManager.modules.get(Xray.class)).isEnabled() && (Integer)xray.mode.getValue() == 1 && xray.shouldRenderSide(Block.func_149682_b((Block)((Block)this))) && xray.checkBlock(block = new BlockPos(blockPos.func_177958_n() - enumFacing.func_176730_m().func_177958_n(), blockPos.func_177956_o() - enumFacing.func_176730_m().func_177956_o(), blockPos.func_177952_p() - enumFacing.func_176730_m().func_177952_p()))) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Inject(method={"getBlockLayer"}, at={@At(value="HEAD")}, cancellable=true)
    private void getBlockLayer(CallbackInfoReturnable<EnumWorldBlockLayer> callbackInfoReturnable) {
        int id;
        Xray xray;
        if (Myau.moduleManager != null && (xray = (Xray)Myau.moduleManager.modules.get(Xray.class)).isEnabled() && (!xray.shouldRenderSide(id = Block.func_149682_b((Block)((Block)this))) || (Integer)xray.mode.getValue() == 0 && !xray.isXrayBlock(id))) {
            callbackInfoReturnable.setReturnValue(EnumWorldBlockLayer.TRANSLUCENT);
        }
    }
}

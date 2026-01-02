package myau.mixin;

import myau.Myau;
import myau.module.modules.Jesus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(value=Side.CLIENT)
@Mixin(value={World.class})
public abstract class MixinWorld {
    @Redirect(method={"handleMaterialAcceleration"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;isPushedByWater()Z"))
    private boolean handleMaterialAcceleration(Entity entity) {
        Jesus jesus;
        if (entity instanceof EntityPlayerSP && Myau.moduleManager != null && (jesus = (Jesus)Myau.moduleManager.modules.get(Jesus.class)) != null && jesus.isEnabled() && ((Boolean)jesus.noPush.getValue()).booleanValue()) {
            return false;
        }
        return entity.func_96092_aw();
    }

    @Redirect(method={"rayTraceBlocks(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;ZZZ)Lnet/minecraft/util/MovingObjectPosition;"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/BlockPos;)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState rayTraceBlocks(World world, BlockPos blockPos) {
        return world.func_180495_p(blockPos);
    }
}

package myau.mixin;

import myau.Myau;
import myau.mixin.MixinEntityLivingBase;
import myau.module.modules.KeepSprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(value=Side.CLIENT)
@Mixin(value={EntityPlayer.class})
public abstract class MixinEntityPlayer
extends MixinEntityLivingBase {
    @ModifyConstant(method={"attackTargetEntityWithCurrentItem"}, constant={@Constant(doubleValue=0.6)})
    private double attackTargetEntityWithCurrentItem(double speed) {
        if (Myau.moduleManager == null) {
            return speed;
        }
        KeepSprint keepSprint = (KeepSprint)Myau.moduleManager.modules.get(KeepSprint.class);
        return keepSprint.isEnabled() && keepSprint.shouldKeepSprint() ? speed + (1.0 - speed) * (1.0 - ((Integer)keepSprint.slowdown.getValue()).doubleValue() / 100.0) : speed;
    }

    @Redirect(method={"attackTargetEntityWithCurrentItem"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/EntityPlayer;setSprinting(Z)V"))
    private void setSprinnt(EntityPlayer entityPlayer, boolean boolean2) {
        KeepSprint keepSprint;
        if (!(Myau.moduleManager == null || (keepSprint = (KeepSprint)Myau.moduleManager.modules.get(KeepSprint.class)).isEnabled() && keepSprint.shouldKeepSprint())) {
            entityPlayer.func_70031_b(boolean2);
        }
    }
}

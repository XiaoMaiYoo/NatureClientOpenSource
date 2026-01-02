package myau.mixin;

import myau.Myau;
import myau.module.modules.Sprint;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(value=Side.CLIENT)
@Pseudo
@Mixin(targets={"club.sk1er.patcher.util.fov.FovHandler"})
public abstract class MixinFovHandler {
    @Redirect(method={"fovChange"}, remap=false, at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/EntityPlayer;func_70051_ag()Z", remap=false))
    @Dynamic(value="Patcher")
    private boolean fovChange(EntityPlayer entityPlayer) {
        boolean sprinting = entityPlayer.func_70051_ag();
        if (entityPlayer instanceof EntityPlayerSP && Myau.moduleManager != null) {
            Sprint sprint = (Sprint)Myau.moduleManager.modules.get(Sprint.class);
            return sprint.isEnabled() && sprint.shouldKeepFov(sprinting) || sprinting;
        }
        return sprinting;
    }
}

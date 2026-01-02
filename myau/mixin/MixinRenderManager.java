package myau.mixin;

import myau.management.RotationState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(value=Side.CLIENT)
@Mixin(value={RenderManager.class})
public abstract class MixinRenderManager {
    @Unique
    private float _prevRenderYawOffset;
    @Unique
    private float _renderYawOffset;
    @Unique
    private float _prevRotationYawHead;
    @Unique
    private float _rotationYawHead;
    @Unique
    private float _prevRotationPitch;
    @Unique
    private float _rotationPitch;

    @Inject(method={"renderEntityStatic"}, at={@At(value="HEAD")})
    private void renderEntityStatic(Entity entity, float float2, boolean boolean3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (entity instanceof EntityPlayerSP && RotationState.isRotated(1)) {
            EntityPlayerSP entityPlayerSP = (EntityPlayerSP)entity;
            this._prevRenderYawOffset = entityPlayerSP.field_70760_ar;
            this._renderYawOffset = entityPlayerSP.field_70761_aq;
            this._prevRotationYawHead = entityPlayerSP.field_70758_at;
            this._rotationYawHead = entityPlayerSP.field_70759_as;
            this._prevRotationPitch = entityPlayerSP.field_70127_C;
            this._rotationPitch = entityPlayerSP.field_70125_A;
            entityPlayerSP.field_70760_ar = RotationState.getPrevRenderYawOffset();
            entityPlayerSP.field_70761_aq = RotationState.getRenderYawOffset();
            entityPlayerSP.field_70758_at = RotationState.getPrevRotationYawHead();
            entityPlayerSP.field_70759_as = RotationState.getRotationYawHead();
            entityPlayerSP.field_70127_C = RotationState.getPrevRotationPitch();
            entityPlayerSP.field_70125_A = RotationState.getRotationPitch();
        }
    }

    @Inject(method={"renderEntityStatic"}, at={@At(value="RETURN")})
    private void renderEntityStaticPost(Entity entity, float float2, boolean boolean3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (entity instanceof EntityPlayerSP && RotationState.isRotated(1)) {
            EntityPlayerSP entityPlayerSP = (EntityPlayerSP)entity;
            entityPlayerSP.field_70760_ar = this._prevRenderYawOffset;
            entityPlayerSP.field_70761_aq = this._renderYawOffset;
            entityPlayerSP.field_70758_at = this._prevRotationYawHead;
            entityPlayerSP.field_70759_as = this._rotationYawHead;
            entityPlayerSP.field_70127_C = this._prevRotationPitch;
            entityPlayerSP.field_70125_A = this._rotationPitch;
        }
    }
}

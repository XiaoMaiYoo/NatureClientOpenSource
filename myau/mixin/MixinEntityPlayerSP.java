package myau.mixin;

import myau.Myau;
import myau.event.EventManager;
import myau.event.types.EventType;
import myau.events.LivingUpdateEvent;
import myau.events.MoveInputEvent;
import myau.events.PlayerUpdateEvent;
import myau.events.UpdateEvent;
import myau.management.RotationState;
import myau.mixin.IAccessorEntityLivingBase;
import myau.mixin.MixinEntityPlayer;
import myau.module.modules.NoSlow;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(value=Side.CLIENT)
@Mixin(value={EntityPlayerSP.class})
public abstract class MixinEntityPlayerSP
extends MixinEntityPlayer {
    @Unique
    private float overrideYaw = Float.NaN;
    @Unique
    private float overridePitch = Float.NaN;
    @Unique
    private float pendingYaw;
    @Unique
    private float pendingPitch;
    @Shadow
    private float field_175164_bL;
    @Shadow
    private float field_175165_bM;
    @Shadow
    public float field_71154_f;
    @Shadow
    public float field_71163_h;

    @Inject(method={"onUpdate"}, at={@At(value="HEAD")})
    private void onUpdate(CallbackInfo callbackInfo) {
        if (this.field_70170_p.func_175667_e(new BlockPos(this.field_70165_t, 0.0, this.field_70161_v))) {
            UpdateEvent event = new UpdateEvent(EventType.PRE, this.field_175164_bL, this.field_175165_bM, this.field_70177_z, this.field_70125_A);
            EventManager.call(event);
            RotationState.applyState(event.isRotated() && !this.func_70115_ae(), event.getNewYaw(), event.getNewPitch(), event.getPreYaw(), event.isRotating());
            if (event.isRotated()) {
                this.pendingYaw = this.field_70177_z;
                this.pendingPitch = this.field_70125_A;
                this.overrideYaw = event.getNewYaw();
                this.overridePitch = event.getNewPitch();
            } else {
                this.pendingYaw = Float.NaN;
                this.pendingPitch = Float.NaN;
                this.overrideYaw = Float.NaN;
                this.overridePitch = Float.NaN;
            }
        }
    }

    @Inject(method={"onUpdate"}, at={@At(value="RETURN")})
    private void postUpdate(CallbackInfo callbackInfo) {
        if (this.field_70170_p.func_175667_e(new BlockPos(this.field_70165_t, 0.0, this.field_70161_v))) {
            if (!Float.isNaN(this.pendingYaw) && !Float.isNaN(this.pendingPitch)) {
                this.field_175164_bL = this.field_70177_z;
                this.field_175165_bM = this.field_70125_A;
                this.field_70177_z += MathHelper.func_76142_g((float)(this.pendingYaw - this.field_70177_z));
                this.field_70125_A = this.pendingPitch;
                this.field_70126_B = this.field_70177_z;
                this.field_70127_C = this.field_70125_A;
                this.field_71163_h = this.field_70177_z - (this.field_71154_f - this.field_71163_h) * 2.0f;
                this.field_71154_f = this.field_70177_z;
            }
            EventManager.call(new UpdateEvent(EventType.POST, this.field_175164_bL, this.field_175165_bM, this.field_70177_z, this.field_70125_A));
        }
    }

    @Redirect(method={"onUpdate"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;isRiding()Z"))
    private boolean onRidding(EntityPlayerSP entityPlayerSP) {
        if (!Float.isNaN(this.overrideYaw) && !Float.isNaN(this.overridePitch)) {
            this.field_70177_z = this.overrideYaw;
            this.field_70125_A = this.overridePitch;
        }
        return entityPlayerSP.func_70115_ae();
    }

    @Inject(method={"onUpdate"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V")})
    private void onMotionUpdate(CallbackInfo callbackInfo) {
        EventManager.call(new PlayerUpdateEvent());
    }

    @Inject(method={"onLivingUpdate"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/entity/AbstractClientPlayer;onLivingUpdate()V")})
    private void onLivingUpdate(CallbackInfo callbackInfo) {
        EventManager.call(new LivingUpdateEvent());
    }

    @Inject(method={"onLivingUpdate"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V", shift=At.Shift.AFTER)})
    private void updateMove(CallbackInfo callbackInfo) {
        EventManager.call(new MoveInputEvent());
    }

    @Redirect(method={"onLivingUpdate"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;isUsingItem()Z"))
    private boolean isUsing(EntityPlayerSP entityPlayerSP) {
        NoSlow noSlow = (NoSlow)Myau.moduleManager.modules.get(NoSlow.class);
        return (!noSlow.isEnabled() || !noSlow.isAnyActive()) && entityPlayerSP.func_71039_bw();
    }

    @Redirect(method={"onLivingUpdate"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean checkPotion(EntityPlayerSP entityPlayerSP, Potion potion) {
        return ((IAccessorEntityLivingBase)entityPlayerSP).getActivePotionsMap().containsKey(potion.field_76415_H);
    }
}

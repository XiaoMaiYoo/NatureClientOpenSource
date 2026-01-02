package myau.mixin;

import myau.Myau;
import myau.event.EventManager;
import myau.events.KnockbackEvent;
import myau.events.SafeWalkEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(value=Side.CLIENT)
@Mixin(value={Entity.class})
public abstract class MixinEntity {
    @Shadow
    public World field_70170_p;
    @Shadow
    public double field_70165_t;
    @Shadow
    public double field_70163_u;
    @Shadow
    public double field_70161_v;
    @Shadow
    public double field_70159_w;
    @Shadow
    public double field_70181_x;
    @Shadow
    public double field_70179_y;
    @Shadow
    public float field_70177_z;
    @Shadow
    public float field_70125_A;
    @Shadow
    public float field_70126_B;
    @Shadow
    public float field_70127_C;
    @Shadow
    public boolean field_70122_E;

    @Shadow
    public boolean func_70115_ae() {
        return false;
    }

    @Inject(method={"setVelocity"}, at={@At(value="HEAD")}, cancellable=true)
    private void setVelocity(double double1, double double2, double double3, CallbackInfo callbackInfo) {
        if ((Entity)this instanceof EntityPlayerSP) {
            KnockbackEvent event = new KnockbackEvent(double1, double2, double3);
            EventManager.call(event);
            if (event.isCancelled()) {
                callbackInfo.cancel();
                this.field_70159_w = event.getX();
                this.field_70181_x = event.getY();
                this.field_70179_y = event.getZ();
            }
        }
    }

    @Inject(method={"setAngles"}, at={@At(value="HEAD")}, cancellable=true)
    private void setAngles(CallbackInfo callbackInfo) {
        if ((Entity)this instanceof EntityPlayerSP && Myau.rotationManager != null && Myau.rotationManager.isRotated()) {
            callbackInfo.cancel();
        }
    }

    @ModifyVariable(method={"moveEntity"}, ordinal=0, at=@At(value="STORE"), name={"flag"})
    private boolean moveEntity(boolean boolean1) {
        if ((Entity)this instanceof EntityPlayerSP) {
            SafeWalkEvent event = new SafeWalkEvent(boolean1);
            EventManager.call(event);
            return event.isSafeWalk();
        }
        return boolean1;
    }
}

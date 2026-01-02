package myau.mixin;

import java.util.List;
import myau.Myau;
import myau.data.Box;
import myau.event.EventManager;
import myau.events.PickEvent;
import myau.events.RaytraceEvent;
import myau.events.Render3DEvent;
import myau.mixin.IAccessorEntityLivingBase;
import myau.mixin.IAccessorEntityPlayer;
import myau.module.modules.AntiDebuff;
import myau.module.modules.Block;
import myau.module.modules.GhostHand;
import myau.module.modules.KillAura;
import myau.module.modules.NoHurtCam;
import myau.module.modules.Scaffold;
import myau.module.modules.ViewClip;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SideOnly(value=Side.CLIENT)
@Mixin(value={EntityRenderer.class})
public abstract class MixinEntityRenderer {
    @Unique
    private Box<Integer> slot = null;
    @Unique
    private Box<ItemStack> using = null;
    @Unique
    private Box<Integer> useCount = null;
    @Shadow
    private Minecraft field_78531_r;
    @Shadow
    private float field_78490_B;

    @Inject(method={"updateCameraAndRender"}, at={@At(value="HEAD")})
    private void updateCameraAndRender(float float1, long long2, CallbackInfo callbackInfo) {
        if (this.field_78531_r.field_71439_g != null) {
            KillAura killAura;
            int slot;
            Scaffold scaffold = (Scaffold)Myau.moduleManager.modules.get(Scaffold.class);
            if (scaffold.isEnabled() && ((Boolean)scaffold.itemSpoof.getValue()).booleanValue() && (slot = scaffold.getSlot()) >= 0) {
                this.slot = new Box<Integer>(this.field_78531_r.field_71439_g.field_71071_by.field_70461_c);
                this.field_78531_r.field_71439_g.field_71071_by.field_70461_c = slot;
            }
            if ((killAura = (KillAura)Myau.moduleManager.modules.get(KillAura.class)).isEnabled() && killAura.isBlocking()) {
                this.using = new Box<ItemStack>(((IAccessorEntityPlayer)this.field_78531_r.field_71439_g).getItemInUse());
                ((IAccessorEntityPlayer)this.field_78531_r.field_71439_g).setItemInUse(this.field_78531_r.field_71439_g.field_71071_by.func_70448_g());
                this.useCount = new Box<Integer>(((IAccessorEntityPlayer)this.field_78531_r.field_71439_g).getItemInUseCount());
                ((IAccessorEntityPlayer)this.field_78531_r.field_71439_g).setItemInUseCount(69000);
            }
        }
    }

    @Inject(method={"updateCameraAndRender"}, at={@At(value="RETURN")})
    private void postUpdateCameraAndRender(float float1, long long2, CallbackInfo callbackInfo) {
        if (this.slot != null) {
            this.field_78531_r.field_71439_g.field_71071_by.field_70461_c = (Integer)this.slot.value;
            this.slot = null;
        }
        if (this.using != null) {
            ((IAccessorEntityPlayer)this.field_78531_r.field_71439_g).setItemInUse((ItemStack)this.using.value);
            this.using = null;
        }
        if (this.useCount != null) {
            ((IAccessorEntityPlayer)this.field_78531_r.field_71439_g).setItemInUseCount((Integer)this.useCount.value);
            this.useCount = null;
        }
    }

    @Inject(method={"updateRenderer"}, at={@At(value="HEAD")})
    private void updateRenderer(CallbackInfo callbackInfo) {
        int slot;
        Block autoBlockIn;
        int slot2;
        Scaffold scaffold = (Scaffold)Myau.moduleManager.modules.get(Scaffold.class);
        if (scaffold.isEnabled() && ((Boolean)scaffold.itemSpoof.getValue()).booleanValue() && (slot2 = scaffold.getSlot()) >= 0) {
            this.slot = new Box<Integer>(this.field_78531_r.field_71439_g.field_71071_by.field_70461_c);
            this.field_78531_r.field_71439_g.field_71071_by.field_70461_c = slot2;
        }
        if ((autoBlockIn = (Block)Myau.moduleManager.modules.get(Block.class)).isEnabled() && ((Boolean)autoBlockIn.itemSpoof.getValue()).booleanValue() && (slot = autoBlockIn.getSlot()) >= 0) {
            this.slot = new Box<Integer>(this.field_78531_r.field_71439_g.field_71071_by.field_70461_c);
            this.field_78531_r.field_71439_g.field_71071_by.field_70461_c = slot;
        }
    }

    @Inject(method={"updateRenderer"}, at={@At(value="RETURN")})
    private void postUpdateRenderer(CallbackInfo callbackInfo) {
        if (this.slot != null) {
            this.field_78531_r.field_71439_g.field_71071_by.field_70461_c = (Integer)this.slot.value;
            this.slot = null;
        }
    }

    @Inject(method={"renderWorldPass"}, at={@At(value="FIELD", target="Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", shift=At.Shift.BEFORE)})
    private void renderWorldPass(int integer, float float2, long long3, CallbackInfo callbackInfo) {
        EventManager.call(new Render3DEvent(float2));
    }

    @ModifyConstant(method={"hurtCameraEffect"}, constant={@Constant(floatValue=14.0f, ordinal=0)})
    private float hurtCameraEffect(float float1) {
        if (Myau.moduleManager == null) {
            return float1;
        }
        NoHurtCam noHurtCam = (NoHurtCam)Myau.moduleManager.modules.get(NoHurtCam.class);
        return noHurtCam.isEnabled() ? float1 * (float)((Integer)noHurtCam.multiplier.getValue()).intValue() / 100.0f : float1;
    }

    @ModifyConstant(method={"getMouseOver"}, constant={@Constant(doubleValue=3.0, ordinal=1)})
    private double getMouseOver(double range) {
        PickEvent event = new PickEvent(range);
        EventManager.call(event);
        return event.getRange();
    }

    @ModifyVariable(method={"getMouseOver"}, at=@At(value="STORE"), name={"d0"})
    private double storeMouseOver(double range) {
        RaytraceEvent event = new RaytraceEvent(range);
        EventManager.call(event);
        return event.getRange();
    }

    @Inject(method={"getMouseOver"}, at={@At(value="INVOKE", target="Ljava/util/List;size()I", ordinal=0)}, locals=LocalCapture.CAPTURE_FAILSOFT)
    private void a(float float1, CallbackInfo callbackInfo, Entity entity, double double4, double double5, Vec3 vec36, boolean boolean7, int integer8, Vec3 vec39, Vec3 vec310, Vec3 vec311, float float12, List<Entity> list, double double14, int integer15) {
        GhostHand event;
        if (Myau.moduleManager != null && (event = (GhostHand)Myau.moduleManager.modules.get(GhostHand.class)).isEnabled()) {
            list.removeIf(event::shouldSkip);
        }
    }

    @Redirect(method={"orientCamera"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"))
    private double v(Vec3 vec31, Vec3 vec32) {
        if (Myau.moduleManager == null) {
            return vec31.func_72438_d(vec32);
        }
        return Myau.moduleManager.modules.get(ViewClip.class).isEnabled() ? (double)this.field_78490_B : vec31.func_72438_d(vec32);
    }

    @Redirect(method={"setupFog"}, at=@At(value="INVOKE", target="Lnet/minecraft/block/Block;getMaterial()Lnet/minecraft/block/material/Material;"))
    private Material x(net.minecraft.block.Block block) {
        if (Myau.moduleManager == null) {
            return block.func_149688_o();
        }
        return Myau.moduleManager.modules.get(ViewClip.class).isEnabled() ? Material.field_151579_a : block.func_149688_o();
    }

    @Redirect(method={"updateFogColor"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean y(EntityLivingBase entityLivingBase, Potion potion) {
        AntiDebuff antiDebuff;
        if (potion == Potion.field_76440_q && Myau.moduleManager != null && (antiDebuff = (AntiDebuff)Myau.moduleManager.modules.get(AntiDebuff.class)).isEnabled() && ((Boolean)antiDebuff.blindness.getValue()).booleanValue()) {
            return false;
        }
        return ((IAccessorEntityLivingBase)entityLivingBase).getActivePotionsMap().containsKey(potion.field_76415_H);
    }

    @Redirect(method={"setupFog"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean q(EntityLivingBase entityLivingBase, Potion potion) {
        AntiDebuff antiDebuff;
        if (potion == Potion.field_76440_q && Myau.moduleManager != null && (antiDebuff = (AntiDebuff)Myau.moduleManager.modules.get(AntiDebuff.class)).isEnabled() && ((Boolean)antiDebuff.blindness.getValue()).booleanValue()) {
            return false;
        }
        return ((IAccessorEntityLivingBase)entityLivingBase).getActivePotionsMap().containsKey(potion.field_76415_H);
    }

    @Redirect(method={"setupCameraTransform"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean c(EntityPlayerSP entityPlayerSP, Potion potion) {
        AntiDebuff antiDebuff;
        if (potion == Potion.field_76431_k && Myau.moduleManager != null && (antiDebuff = (AntiDebuff)Myau.moduleManager.modules.get(AntiDebuff.class)).isEnabled() && ((Boolean)antiDebuff.nausea.getValue()).booleanValue()) {
            return false;
        }
        return ((IAccessorEntityLivingBase)entityPlayerSP).getActivePotionsMap().containsKey(potion.field_76415_H);
    }
}

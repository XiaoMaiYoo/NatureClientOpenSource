package myau.module.modules;

import com.google.common.base.CaseFormat;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.KeyEvent;
import myau.events.KnockbackEvent;
import myau.events.MoveInputEvent;
import myau.events.PacketEvent;
import myau.events.StrafeEvent;
import myau.events.TickEvent;
import myau.events.UpdateEvent;
import myau.management.RotationState;
import myau.mixin.IAccessorPlayerControllerMP;
import myau.module.Module;
import myau.property.properties.FloatProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.ChatUtil;
import myau.util.MoveUtil;
import myau.util.PacketUtil;
import myau.util.RandomUtil;
import myau.util.RotationUtil;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class LongJump
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final TimerUtil fireballTimer = new TimerUtil();
    private final TimerUtil jumpTimer = new TimerUtil();
    private boolean isJumping = false;
    private int tickCounter = 0;
    private int jumpModeStage = 0;
    private boolean readyToUseFireball = false;
    private boolean fireballLaunched = false;
    private int savedHotbarSlot = -1;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"FIREBALL", "FIREBALL_MANUAL", "FIREBALL_HIGH", "FIREBALL_FLAT"});
    public final FloatProperty motion = new FloatProperty("motion", Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public final FloatProperty speedMotion = new FloatProperty("speed-motion", Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public final PercentProperty strafe = new PercentProperty("strafe", 0);

    private int findFireballInHotbar() {
        if (LongJump.mc.field_71439_g == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = LongJump.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (stack == null || !(stack.func_77973_b() instanceof ItemFireball)) continue;
            return i;
        }
        return -1;
    }

    private double getMotionFactor() {
        return MoveUtil.getSpeedLevel() > 0 ? (double)((Float)this.speedMotion.getValue()).floatValue() : (double)((Float)this.motion.getValue()).floatValue();
    }

    public LongJump() {
        super("LongJump", false);
    }

    public boolean isAutoMode() {
        return (Integer)this.mode.getValue() == 0 || (Integer)this.mode.getValue() == 2 || (Integer)this.mode.getValue() == 3;
    }

    public boolean isManualMode() {
        return (Integer)this.mode.getValue() == 1;
    }

    public boolean isLongJumpMode() {
        return this.isAutoMode() || this.isManualMode();
    }

    public boolean canStartJump() {
        return !this.fireballTimer.hasTimeElapsed(1000L) && !this.isJumping;
    }

    public boolean isJumping() {
        return this.isJumping;
    }

    @EventTarget(value=0)
    public void onKnockback(KnockbackEvent event) {
        if (this.isEnabled() && !event.isCancelled() && (this.isManualMode() || this.isAutoMode()) && this.canStartJump()) {
            event.setCancelled(true);
            this.isJumping = true;
            this.tickCounter = 0;
        }
    }

    @EventTarget(value=0)
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            switch (event.getType()) {
                case PRE: {
                    int slot;
                    if (!this.isAutoMode() || this.fireballLaunched || !this.readyToUseFireball || (slot = this.findFireballInHotbar()) == -1) break;
                    this.savedHotbarSlot = LongJump.mc.field_71439_g.field_71071_by.field_70461_c;
                    LongJump.mc.field_71439_g.field_71071_by.field_70461_c = slot;
                    ((IAccessorPlayerControllerMP)LongJump.mc.field_71442_b).callSyncCurrentPlayItem();
                    PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(LongJump.mc.field_71439_g.func_70694_bm()));
                    this.fireballTimer.reset();
                    this.fireballLaunched = true;
                    break;
                }
                case POST: {
                    if (this.savedHotbarSlot == -1) break;
                    LongJump.mc.field_71439_g.field_71071_by.field_70461_c = this.savedHotbarSlot;
                    this.savedHotbarSlot = -1;
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.isLongJumpMode() && this.isJumping) {
                ++this.tickCounter;
                if (this.tickCounter == 1) {
                    switch ((Integer)this.mode.getValue()) {
                        case 0: 
                        case 1: {
                            this.jumpModeStage = 0;
                            break;
                        }
                        case 2: {
                            this.jumpModeStage = 1;
                            break;
                        }
                        case 3: {
                            int n = this.jumpModeStage = MoveUtil.isForwardPressed() ? 2 : 1;
                        }
                    }
                }
                if (this.tickCounter == 2 && MoveUtil.isForwardPressed()) {
                    MoveUtil.setSpeed(MoveUtil.getSpeed() * this.getMotionFactor());
                }
                if (this.tickCounter >= 1 && this.tickCounter <= 30) {
                    switch (this.jumpModeStage) {
                        case 1: {
                            if (this.tickCounter == 1) {
                                LongJump.mc.field_71439_g.field_70181_x *= 0.75;
                                break;
                            }
                            double motion = LongJump.mc.field_71439_g.field_70181_x / (double)0.98f + 0.055;
                            if (!(motion > 0.0)) break;
                            LongJump.mc.field_71439_g.field_70181_x = motion;
                            break;
                        }
                        case 2: {
                            if (this.tickCounter == 1) {
                                LongJump.mc.field_71439_g.field_70181_x *= 0.75;
                                break;
                            }
                            LongJump.mc.field_71439_g.field_70181_x = 0.01 + (double)this.tickCounter * 0.003;
                        }
                    }
                }
                if (this.tickCounter >= 30) {
                    this.isJumping = false;
                    this.tickCounter = 0;
                    this.jumpModeStage = 0;
                    if (this.isAutoMode()) {
                        this.setEnabled(false);
                    }
                    return;
                }
            }
            if (this.isAutoMode() && !this.isJumping) {
                if (this.jumpTimer.hasTimeElapsed(1500L)) {
                    this.setEnabled(false);
                    return;
                }
                this.readyToUseFireball = true;
                float yaw = RotationUtil.quantizeAngle(LongJump.mc.field_71439_g.field_70177_z - 180.0f - RandomUtil.nextFloat(0.0f, 1.0f));
                float pitch = RotationUtil.quantizeAngle(89.0f + RandomUtil.nextFloat(-0.25f, 0.25f));
                event.setRotation(yaw, pitch, 4);
                event.setPervRotation(yaw, 4);
            }
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (this.isEnabled() && RotationState.isActived() && RotationState.getPriority() == 4.0f && MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (this.isEnabled() && this.isLongJumpMode() && this.isJumping && this.tickCounter >= 5 && this.tickCounter <= 30 && (Integer)this.strafe.getValue() > 0) {
            double speed = MoveUtil.getSpeed();
            MoveUtil.setSpeed(speed * (double)((float)(100 - (Integer)this.strafe.getValue()) / 100.0f), MoveUtil.getDirectionYaw());
            MoveUtil.addSpeed(speed * (double)((float)((Integer)this.strafe.getValue()).intValue() / 100.0f), MoveUtil.getMoveYaw());
            MoveUtil.setSpeed(speed);
        }
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        ItemStack stack;
        if (event.getKey() == LongJump.mc.field_71474_y.field_74313_G.func_151463_i() && (stack = LongJump.mc.field_71439_g.field_71071_by.func_70448_g()) != null && stack.func_77973_b() instanceof ItemFireball) {
            this.fireballTimer.reset();
        }
    }

    @EventTarget(value=1)
    public void onPacket(PacketEvent event) {
        if (event.getType() == EventType.RECEIVE && !event.isCancelled() && event.getPacket() instanceof S08PacketPlayerPosLook) {
            this.isJumping = false;
            this.tickCounter = 0;
            this.jumpModeStage = 0;
            if (this.isAutoMode()) {
                this.setEnabled(false);
            }
        }
    }

    @Override
    public void onEnabled() {
        this.jumpTimer.reset();
        if (this.isAutoMode() && this.findFireballInHotbar() == -1) {
            this.setEnabled(false);
            ChatUtil.sendFormatted(String.format("%s%s: &cNo fireball found in your hotbar!&r", Myau.clientName, this.getName()));
        }
    }

    @Override
    public void onDisabled() {
        this.isJumping = false;
        this.tickCounter = 0;
        this.jumpModeStage = 0;
        this.readyToUseFireball = false;
        this.fireballLaunched = false;
    }

    @Override
    public String[] getSuffix() {
        String[] stringArray;
        String mode = this.mode.getModeString();
        if (mode.contains("FIREBALL")) {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = "Fireball";
        } else {
            String[] stringArray3 = new String[1];
            stringArray = stringArray3;
            stringArray3[0] = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, mode);
        }
        return stringArray;
    }
}

package myau.module.modules;

import com.google.common.base.CaseFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import myau.Myau;
import myau.enums.BlinkModules;
import myau.enums.DelayModules;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.AttackEvent;
import myau.events.KnockbackEvent;
import myau.events.LivingUpdateEvent;
import myau.events.LoadWorldEvent;
import myau.events.MoveInputEvent;
import myau.events.PacketEvent;
import myau.events.UpdateEvent;
import myau.mixin.IAccessorEntity;
import myau.module.Module;
import myau.module.modules.KillAura;
import myau.module.modules.LongJump;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.ChatUtil;
import myau.util.ItemUtil;
import myau.util.MoveUtil;
import myau.util.PacketUtil;
import myau.util.RayCastUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

public class Velocity
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int chanceCounter = 0;
    private int delayChanceCounter = 0;
    private boolean pendingExplosion = false;
    private boolean allowNext = true;
    private boolean reverseFlag = false;
    private boolean delayActive = false;
    private long lastAttackTime = 0L;
    private long blinkStartTime = System.currentTimeMillis();
    private final long blinkDuration = 95L;
    private long reverseStartTime = 0L;
    private boolean jumpFlag = false;
    private int rotatoTickCounter = 0;
    private float[] targetRotation = null;
    private double knockbackX = 0.0;
    private double knockbackZ = 0.0;
    private boolean isSmartRotActive = false;
    private int smartRotDuration = 2;
    private boolean isAirSmartRot = false;
    private boolean attackReduceTriggered = false;
    private boolean attackReduceSuccess = false;
    private boolean arcaneHasReceivedVelocity = false;
    private boolean arcaneNoAttack = true;
    private Entity arcaneTarget = null;
    private int arcaneHitsCount = 0;
    private int arcaneTicksCount = 0;
    private boolean arcaneIsFallDamage = false;
    private boolean isSmartRotating = false;
    private float smartRotStartYaw = 0.0f;
    private float smartRotTargetYaw = 0.0f;
    private int smartRotProgress = 0;
    private double knockbackStrength = 0.0;
    private double knockbackDirectionAngle = 0.0;
    private final Queue<S12PacketEntityVelocity> cachedVelocityPackets = new LinkedList<S12PacketEntityVelocity>();
    private int packetDelayCounter = 0;
    private boolean isProcessingCachedPacket = false;
    private boolean buffer2Active = false;
    private int buffer2RemainingTicks = 0;
    private boolean buffer2ShouldJumpReset = false;
    private boolean buffer2ShouldSmartRot = false;
    private boolean buffer2AttackBlocked = false;
    private final LinkedList<double[]> recentKnockbacks = new LinkedList();
    private static final int MAX_KNOCKBACKS = 2;
    private boolean isDelayingPackets = false;
    private boolean isSendingQueuedPackets = false;
    private final ArrayList<Packet<?>> delayedPackets = new ArrayList();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"VANILLA", "JUMP", "Hypixel"});
    public final ModeProperty rotationMode = new ModeProperty("Rotation", 0, new String[]{"Hypixel", "Hypixel3"}, () -> (Integer)this.mode.getValue() == 2 && ((Boolean)this.smartRotJumpReset.getValue() != false || (Boolean)this.buffer.getValue() != false || (Boolean)this.buffer2.getValue() != false || (Boolean)this.buffer3.getValue() != false));
    public final BooleanProperty smartRotJumpReset = new BooleanProperty("SmartRot", false, () -> (Integer)this.mode.getValue() == 2);
    public final IntProperty rotateTicks = new IntProperty("RotTicks", 2, 1, 10, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.smartRotJumpReset.getValue() != false);
    public final BooleanProperty autoJump = new BooleanProperty("AutoJump", true, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.smartRotJumpReset.getValue() != false);
    public final BooleanProperty autoMove = new BooleanProperty("AutoMove", false, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.smartRotJumpReset.getValue() != false);
    public final BooleanProperty airRotate = new BooleanProperty("AirRotate", false, () -> (Integer)this.mode.getValue() == 2 && ((Boolean)this.smartRotJumpReset.getValue() != false || (Boolean)this.buffer.getValue() != false || (Boolean)this.buffer2.getValue() != false || (Boolean)this.buffer3.getValue() != false));
    public final IntProperty airRotateTicks = new IntProperty("AirRotTicks", 2, 1, 10, () -> (Integer)this.mode.getValue() == 2 && ((Boolean)this.smartRotJumpReset.getValue() != false || (Boolean)this.buffer.getValue() != false || (Boolean)this.buffer2.getValue() != false || (Boolean)this.buffer3.getValue() != false) && (Boolean)this.airRotate.getValue() != false);
    public final IntProperty airDelayTick = new IntProperty("AirDelayTick", 2, 0, 10, () -> (Integer)this.mode.getValue() == 2);
    public final BooleanProperty useIntaveFactor = new BooleanProperty("Factor", false, () -> (Integer)this.mode.getValue() == 2);
    public final FloatProperty intaveFactor = new FloatProperty("ReduceFactor", Float.valueOf(0.78f), Float.valueOf(0.1f), Float.valueOf(1.0f), () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.useIntaveFactor.getValue() != false);
    public final BooleanProperty keepVerticalIntave = new BooleanProperty("KeepVertical", true, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.useIntaveFactor.getValue() != false);
    public final BooleanProperty attackReduce = new BooleanProperty("AttackReduce", false, () -> (Integer)this.mode.getValue() == 2);
    public final BooleanProperty alwaysApplyAttackReduce = new BooleanProperty("Always Apply AR", false, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.attackReduce.getValue() != false);
    public final BooleanProperty buffer = new BooleanProperty("Buffer", false, () -> (Integer)this.mode.getValue() == 2);
    public final BooleanProperty buffer2 = new BooleanProperty("Buffer2", false, () -> (Integer)this.mode.getValue() == 2);
    public final BooleanProperty buffer3 = new BooleanProperty("Buffer3", false, () -> (Integer)this.mode.getValue() == 2);
    public final ModeProperty reduceType = new ModeProperty("Reduce-Type", 0, new String[]{"Normal", "RayCast", "AttackOnVelo"}, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.reduceMode.getValue() != false);
    public final FloatProperty reduceFactor = new FloatProperty("Reduce-Factor", Float.valueOf(0.6f), Float.valueOf(0.1f), Float.valueOf(1.0f), () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.reduceMode.getValue() != false);
    public final IntProperty reduceHurtTime = new IntProperty("Reduce-HurtTime", 9, 1, 10, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.reduceMode.getValue() != false);
    public final FloatProperty rayCastRange = new FloatProperty("RayCast-Range", Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(6.0f), () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.reduceMode.getValue() != false && (Integer)this.reduceType.getValue() != 0);
    public final BooleanProperty reduceSprintReset = new BooleanProperty("SprintReset", true, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.reduceMode.getValue() != false);
    public final BooleanProperty reduceDebug = new BooleanProperty("Reduce-Debug", false, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.reduceMode.getValue() != false);
    public final BooleanProperty reduceMode = new BooleanProperty("Reduce", false, () -> (Integer)this.mode.getValue() == 2);
    public final BooleanProperty watchdogReduce = new BooleanProperty("WatchdogReduce", false, () -> (Integer)this.mode.getValue() == 2);
    public final IntProperty watchdogChance = new IntProperty("WD-Chance", 100, 0, 100, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.watchdogReduce.getValue() != false);
    public final BooleanProperty watchdogLegitTiming = new BooleanProperty("WD-Legit", false, () -> (Integer)this.mode.getValue() == 2 && (Boolean)this.watchdogReduce.getValue() != false);
    public final PercentProperty chance = new PercentProperty("chance", 100);
    public final PercentProperty horizontal = new PercentProperty("horizontal", 100);
    public final PercentProperty vertical = new PercentProperty("vertical", 100);
    public final PercentProperty explosionHorizontal = new PercentProperty("explosions-horizontal", 100);
    public final PercentProperty explosionVertical = new PercentProperty("explosions-vertical", 100);
    public final BooleanProperty fakeCheck = new BooleanProperty("fake-check", true);
    public final BooleanProperty debugLog = new BooleanProperty("debug-log", false);
    public final IntProperty delayTicks = new IntProperty("Re", 3, 1, 20, () -> (Integer)this.mode.getValue() == 2);
    public final PercentProperty delayChance = new PercentProperty("Chance", 100, () -> (Integer)this.mode.getValue() == 2);
    public final BooleanProperty jumpReset = new BooleanProperty("Alink", true, () -> (Integer)this.mode.getValue() == 2);
    public final IntProperty hurt = new IntProperty("Hurt", 10, 1, 10, () -> (Integer)this.mode.getValue() == 2);
    public final FloatProperty astolftor = new FloatProperty("Astolftor", Float.valueOf(0.6f), Float.valueOf(0.1f), Float.valueOf(1.0f), () -> (Integer)this.mode.getValue() == 2);
    public final BooleanProperty test = new BooleanProperty("Bool", true, () -> (Integer)this.mode.getValue() == 2);
    public final BooleanProperty USerDP = new BooleanProperty("USer", false, () -> (Integer)this.mode.getValue() == 1);
    public final IntProperty ExhIemDP = new IntProperty("ExhIemDP", 1, 1, 5, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.USerDP.getValue() != false);

    private boolean isInLiquidOrWeb() {
        return Velocity.mc.field_71439_g != null && (Velocity.mc.field_71439_g.func_70090_H() || Velocity.mc.field_71439_g.func_180799_ab() || ((IAccessorEntity)Velocity.mc.field_71439_g).getIsInWeb());
    }

    private boolean canDelay() {
        if (((Boolean)this.buffer.getValue()).booleanValue() || ((Boolean)this.buffer3.getValue()).booleanValue()) {
            return true;
        }
        Module killAura = Myau.moduleManager.getModule(KillAura.class);
        return Velocity.mc.field_71439_g.field_70122_E && (!killAura.isEnabled() || !((KillAura)killAura).shouldAutoBlock());
    }

    public Velocity() {
        super("Velocity", false);
    }

    @EventTarget
    public void onKnockback(KnockbackEvent event) {
        if (!this.isEnabled() || event.isCancelled() || Velocity.mc.field_71439_g == null) {
            this.pendingExplosion = false;
            this.allowNext = true;
            this.attackReduceTriggered = false;
            return;
        }
        if (((Boolean)this.watchdogReduce.getValue()).booleanValue() && event.getY() > 0.0) {
            if ((Integer)this.watchdogChance.getValue() < 100) {
                if (new Random().nextInt(100) < (Integer)this.watchdogChance.getValue()) {
                    this.activateWatchdogReduce();
                }
            } else {
                this.activateWatchdogReduce();
            }
        }
        if ((Integer)this.mode.getValue() == 2) {
            if (Velocity.mc.field_71439_g.field_70737_aN > 0) {
                if (this.pendingExplosion) {
                    this.pendingExplosion = false;
                    this.handleExplosion(event);
                    return;
                }
                boolean isAirborne = !Velocity.mc.field_71439_g.field_70122_E;
                double kbX = event.getX();
                double kbZ = event.getZ();
                double verticalStrength = Math.abs(event.getY());
                this.knockbackStrength = Math.sqrt(kbX * kbX + kbZ * kbZ);
                this.knockbackDirectionAngle = Math.toDegrees(Math.atan2(-kbX, -kbZ));
                if (((Boolean)this.smartRotJumpReset.getValue()).booleanValue() && event.getY() > 0.0) {
                    boolean shouldActivateSmartRot;
                    boolean bl = shouldActivateSmartRot = !isAirborne || isAirborne && (Boolean)this.airRotate.getValue() != false;
                    if (shouldActivateSmartRot) {
                        this.smartRotStartYaw = Velocity.mc.field_71439_g.field_70177_z;
                        this.smartRotTargetYaw = (float)this.knockbackDirectionAngle;
                        this.smartRotProgress = 0;
                        this.isSmartRotating = true;
                        this.smartRotDuration = isAirborne && (Boolean)this.airRotate.getValue() != false ? ((Integer)this.airRotateTicks.getValue()).intValue() : ((Integer)this.rotateTicks.getValue()).intValue();
                        this.isAirSmartRot = isAirborne;
                        if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                            ChatUtil.sendFormatted(String.format("%s[SmartRot] Start: %.1f\u00b0 -> %.1f\u00b0, Strength: %.3f&r", Myau.clientName, Float.valueOf(this.smartRotStartYaw), Float.valueOf(this.smartRotTargetYaw), this.knockbackStrength));
                        }
                    }
                }
                if (((Boolean)this.jumpReset.getValue()).booleanValue() && event.getY() > 0.0) {
                    this.jumpFlag = true;
                    boolean bl = this.attackReduceSuccess = (Boolean)this.attackReduce.getValue() != false && this.attackReduceTriggered;
                    if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                        ChatUtil.sendFormatted(String.format("%s[JumpReset] Triggered&r", Myau.clientName));
                    }
                }
                if (((Boolean)this.attackReduce.getValue()).booleanValue()) {
                    boolean shouldApply;
                    boolean bl = shouldApply = (Boolean)this.alwaysApplyAttackReduce.getValue() != false || this.attackReduceTriggered;
                    if (shouldApply) {
                        if ((Integer)this.horizontal.getValue() > 0) {
                            event.setX(event.getX() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                            event.setZ(event.getZ() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                        } else {
                            event.setX(Velocity.mc.field_71439_g.field_70159_w);
                            event.setZ(Velocity.mc.field_71439_g.field_70179_y);
                        }
                        if ((Integer)this.vertical.getValue() > 0) {
                            event.setY(event.getY() * (double)((Integer)this.vertical.getValue()).intValue() / 100.0);
                        } else {
                            event.setY(Velocity.mc.field_71439_g.field_70181_x);
                        }
                        if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                            ChatUtil.sendFormatted(Myau.clientName + "Successfully AttackReduce " + this.horizontal.getValue() + "%");
                        }
                        if (!((Boolean)this.alwaysApplyAttackReduce.getValue()).booleanValue()) {
                            this.attackReduceTriggered = false;
                        }
                    }
                }
                if (((Boolean)this.buffer.getValue()).booleanValue() && !this.attackReduceTriggered) {
                    int calculatedDelay;
                    if (isAirborne) {
                        calculatedDelay = this.knockbackStrength > 0.25 || verticalStrength > 0.3 ? (Integer)this.airDelayTick.getValue() : Math.max(1, (Integer)this.airDelayTick.getValue() - 1);
                        if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                            ChatUtil.sendFormatted(String.format("%s[Air-Delay] Strength: %.3f, Delay: %dt&r", Myau.clientName, this.knockbackStrength, calculatedDelay));
                        }
                    } else {
                        calculatedDelay = this.knockbackStrength > 0.2 ? 2 : 1;
                        if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                            ChatUtil.sendFormatted(String.format("%s[Ground-Delay] Strength: %.3f, Delay: %dt&r", Myau.clientName, this.knockbackStrength, calculatedDelay));
                        }
                    }
                    if (!(calculatedDelay <= 0 || this.reverseFlag || this.isInLiquidOrWeb() || this.pendingExplosion)) {
                        Myau.delayManager.setDelayState(true, DelayModules.VELOCITY);
                        this.reverseFlag = true;
                        this.reverseStartTime = System.currentTimeMillis();
                    }
                }
                if (!(((Boolean)this.attackReduce.getValue()).booleanValue() || ((Boolean)this.useIntaveFactor.getValue()).booleanValue() || ((Boolean)this.buffer.getValue()).booleanValue())) {
                    if ((Integer)this.horizontal.getValue() > 0) {
                        event.setX(event.getX() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                        event.setZ(event.getZ() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                    } else {
                        event.setX(Velocity.mc.field_71439_g.field_70159_w);
                        event.setZ(Velocity.mc.field_71439_g.field_70179_y);
                    }
                    if ((Integer)this.vertical.getValue() > 0) {
                        event.setY(event.getY() * (double)((Integer)this.vertical.getValue()).intValue() / 100.0);
                    } else {
                        event.setY(Velocity.mc.field_71439_g.field_70181_x);
                    }
                }
                if (((Boolean)this.reduceMode.getValue()).booleanValue()) {
                    int reduceTypeVal = (Integer)this.reduceType.getValue();
                    double reduceValue = ((Float)this.reduceFactor.getValue()).floatValue();
                    if (reduceTypeVal == 0) {
                        if (Velocity.mc.field_71439_g.field_70737_aN >= (Integer)this.reduceHurtTime.getValue()) {
                            double originalX = event.getX();
                            double originalZ = event.getZ();
                            double originalKB = Math.sqrt(originalX * originalX + originalZ * originalZ);
                            event.setX(event.getX() * reduceValue);
                            event.setZ(event.getZ() * reduceValue);
                            double reducedKB = Math.sqrt(event.getX() * event.getX() + event.getZ() * event.getZ());
                            if (((Boolean)this.reduceSprintReset.getValue()).booleanValue() && Velocity.mc.field_71439_g.func_70051_ag()) {
                                Velocity.mc.field_71439_g.func_70031_b(false);
                            }
                            if (((Boolean)this.reduceDebug.getValue()).booleanValue()) {
                                ChatUtil.sendFormatted(String.format("%s[Reduce-Normal] Tick:%d Factor:%.2f KB:%.3f->%.3f&r", Myau.clientName, Velocity.mc.field_71439_g.field_70737_aN, reduceValue, originalKB, reducedKB));
                            }
                        }
                    } else if (reduceTypeVal == 1) {
                        Entity raycastTarget = RayCastUtil.raycastEntity(((Float)this.rayCastRange.getValue()).floatValue(), Velocity.mc.field_71439_g.field_70177_z, Velocity.mc.field_71439_g.field_70125_A, new RayCastUtil.IEntityFilter(){

                            @Override
                            public boolean canRaycast(Entity entity) {
                                return entity != mc.field_71439_g && entity instanceof EntityLivingBase;
                            }
                        });
                        if (raycastTarget != null) {
                            if (Velocity.mc.field_71439_g.field_70737_aN >= (Integer)this.reduceHurtTime.getValue()) {
                                double originalX = event.getX();
                                double originalZ = event.getZ();
                                double originalKB = Math.sqrt(originalX * originalX + originalZ * originalZ);
                                event.setX(event.getX() * reduceValue);
                                event.setZ(event.getZ() * reduceValue);
                                double reducedKB = Math.sqrt(event.getX() * event.getX() + event.getZ() * event.getZ());
                                if (((Boolean)this.reduceSprintReset.getValue()).booleanValue() && Velocity.mc.field_71439_g.func_70051_ag()) {
                                    Velocity.mc.field_71439_g.func_70031_b(false);
                                }
                                if (((Boolean)this.reduceDebug.getValue()).booleanValue()) {
                                    ChatUtil.sendFormatted(String.format("%s[Reduce-RayCast] Tick:%d Target:%s KB:%.3f->%.3f&r", Myau.clientName, Velocity.mc.field_71439_g.field_70737_aN, raycastTarget.func_70005_c_(), originalKB, reducedKB));
                                }
                            }
                        } else if (((Boolean)this.reduceDebug.getValue()).booleanValue()) {
                            ChatUtil.sendFormatted(String.format("%s[Reduce-RayCast] No target in %.1f range&r", Myau.clientName, this.rayCastRange.getValue()));
                        }
                    } else if (reduceTypeVal == 2) {
                        boolean isMoving;
                        boolean bl = isMoving = Math.abs(Velocity.mc.field_71439_g.field_70701_bs) > 0.1f || Math.abs(Velocity.mc.field_71439_g.field_70702_br) > 0.1f;
                        if (isMoving && Velocity.mc.field_71439_g.func_70051_ag()) {
                            Module killAura = Myau.moduleManager.getModule(KillAura.class);
                            EntityLivingBase target = null;
                            if (killAura != null && killAura.isEnabled()) {
                                target = ((KillAura)killAura).getTarget();
                            }
                            if (target != null && target instanceof EntityLivingBase && target != Velocity.mc.field_71439_g) {
                                if (this.arcaneHasReceivedVelocity) {
                                    if (Velocity.mc.field_71439_g.field_70737_aN >= (Integer)this.reduceHurtTime.getValue()) {
                                        double originalX = event.getX();
                                        double originalZ = event.getZ();
                                        double originalKB = Math.sqrt(originalX * originalX + originalZ * originalZ);
                                        event.setX(event.getX() * reduceValue);
                                        event.setZ(event.getZ() * reduceValue);
                                        double reducedKB = Math.sqrt(event.getX() * event.getX() + event.getZ() * event.getZ());
                                        ++this.arcaneHitsCount;
                                        Velocity.mc.field_71439_g.func_71038_i();
                                        Velocity.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new C02PacketUseEntity((Entity)target, C02PacketUseEntity.Action.ATTACK));
                                        if (((Boolean)this.reduceSprintReset.getValue()).booleanValue()) {
                                            Velocity.mc.field_71439_g.func_70031_b(false);
                                        }
                                        if (((Boolean)this.reduceDebug.getValue()).booleanValue()) {
                                            ChatUtil.sendFormatted(String.format("%s[Reduce-AttackOnVelo] Tick:%d Attack:%d KB:%.3f->%.3f&r", Myau.clientName, Velocity.mc.field_71439_g.field_70737_aN, this.arcaneHitsCount, originalKB, reducedKB));
                                        }
                                        this.arcaneHasReceivedVelocity = false;
                                    } else if (((Boolean)this.reduceDebug.getValue()).booleanValue()) {
                                        ChatUtil.sendFormatted(String.format("%s[Reduce-AttackOnVelo] Skip - HurtTime:%d < %d&r", Myau.clientName, Velocity.mc.field_71439_g.field_70737_aN, this.reduceHurtTime.getValue()));
                                    }
                                } else if (((Boolean)this.reduceDebug.getValue()).booleanValue()) {
                                    ChatUtil.sendFormatted(String.format("%s[Reduce-AttackOnVelo] Waiting for velocity&r", Myau.clientName));
                                }
                            } else if (((Boolean)this.reduceDebug.getValue()).booleanValue()) {
                                ChatUtil.sendFormatted(String.format("%s[Reduce-AttackOnVelo] No valid target&r", Myau.clientName));
                            }
                        } else if (((Boolean)this.reduceDebug.getValue()).booleanValue()) {
                            String reason = !isMoving ? "Not moving" : "Not sprinting";
                            ChatUtil.sendFormatted(String.format("%s[Reduce-AttackOnVelo] %s&r", Myau.clientName, reason));
                        }
                    }
                }
                if (((Boolean)this.useIntaveFactor.getValue()).booleanValue() && !this.attackReduceTriggered) {
                    this.applyIntaveFactorReduction(event);
                }
                if (((Boolean)this.buffer3.getValue()).booleanValue()) {
                    this.handleBuffer3Mode(event);
                    return;
                }
                if (((Boolean)this.buffer2.getValue()).booleanValue() && !this.attackReduceTriggered) {
                    this.handleBuffer2Mode(event, isAirborne);
                    return;
                }
            }
            return;
        }
        if (!this.allowNext || !((Boolean)this.fakeCheck.getValue()).booleanValue()) {
            this.allowNext = true;
            if (this.pendingExplosion) {
                this.pendingExplosion = false;
                this.handleExplosion(event);
            } else {
                this.chanceCounter = this.chanceCounter % 100 + (Integer)this.chance.getValue();
                if (this.chanceCounter >= 100) {
                    boolean bl = this.jumpFlag = (Integer)this.mode.getValue() == 1 && event.getY() > 0.0;
                    if ((Integer)this.mode.getValue() == 1 && event.getY() > 0.0) {
                        if ((Integer)this.horizontal.getValue() > 0) {
                            event.setX(event.getX() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                            event.setZ(event.getZ() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                        } else {
                            event.setX(Velocity.mc.field_71439_g.field_70159_w);
                            event.setZ(Velocity.mc.field_71439_g.field_70179_y);
                        }
                        if ((Integer)this.vertical.getValue() > 0) {
                            event.setY(event.getY() * (double)((Integer)this.vertical.getValue()).intValue() / 100.0);
                        } else {
                            event.setY(Velocity.mc.field_71439_g.field_70181_x);
                        }
                    } else {
                        this.applyVanilla(event);
                    }
                    this.chanceCounter = 0;
                }
            }
        }
    }

    private void activateWatchdogReduce() {
        this.isDelayingPackets = true;
        if (((Boolean)this.debugLog.getValue()).booleanValue()) {
            ChatUtil.sendFormatted(String.format("%s[WatchdogReduce] Activated on knockback&r", Myau.clientName));
        }
    }

    private void handleBuffer3Mode(KnockbackEvent event) {
        if (!this.allowNext || !((Boolean)this.fakeCheck.getValue()).booleanValue()) {
            this.allowNext = true;
            if (this.pendingExplosion) {
                this.pendingExplosion = false;
                this.handleExplosion(event);
                return;
            }
            if (((Boolean)this.jumpReset.getValue()).booleanValue() && event.getY() > 0.0) {
                this.jumpFlag = true;
                boolean bl = this.attackReduceSuccess = (Boolean)this.attackReduce.getValue() != false && this.attackReduceTriggered;
                if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                    if (this.attackReduceSuccess) {
                        ChatUtil.sendFormatted(String.format("%s[Buffer3-AttackReduce] Jump reset&r", Myau.clientName));
                    } else {
                        ChatUtil.sendFormatted(String.format("%s[Buffer3-JumpReset] Jump reset&r", Myau.clientName));
                    }
                }
            }
            if (((Boolean)this.attackReduce.getValue()).booleanValue()) {
                boolean shouldApply;
                boolean bl = shouldApply = (Boolean)this.alwaysApplyAttackReduce.getValue() != false || this.attackReduceTriggered;
                if (shouldApply) {
                    if ((Integer)this.horizontal.getValue() > 0) {
                        event.setX(event.getX() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                        event.setZ(event.getZ() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                    } else {
                        event.setX(Velocity.mc.field_71439_g.field_70159_w);
                        event.setZ(Velocity.mc.field_71439_g.field_70179_y);
                    }
                    if ((Integer)this.vertical.getValue() > 0) {
                        event.setY(event.getY() * (double)((Integer)this.vertical.getValue()).intValue() / 100.0);
                    } else {
                        event.setY(Velocity.mc.field_71439_g.field_70181_x);
                    }
                    if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                        ChatUtil.sendFormatted(Myau.clientName + "[Buffer3-AttackReduce] Attack reduce Successfully");
                    }
                    if (!((Boolean)this.alwaysApplyAttackReduce.getValue()).booleanValue()) {
                        this.attackReduceTriggered = false;
                    }
                }
            }
            if (!((Boolean)this.attackReduce.getValue()).booleanValue()) {
                if ((Integer)this.horizontal.getValue() > 0) {
                    event.setX(event.getX() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                    event.setZ(event.getZ() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                } else {
                    event.setX(Velocity.mc.field_71439_g.field_70159_w);
                    event.setZ(Velocity.mc.field_71439_g.field_70179_y);
                }
                if ((Integer)this.vertical.getValue() > 0) {
                    event.setY(event.getY() * (double)((Integer)this.vertical.getValue()).intValue() / 100.0);
                } else {
                    event.setY(Velocity.mc.field_71439_g.field_70181_x);
                }
            }
        }
    }

    private void handleBuffer2Mode(KnockbackEvent event, boolean isAirborne) {
        double kbX = event.getX();
        double kbZ = event.getZ();
        double kbMagnitude = Math.sqrt(kbX * kbX + kbZ * kbZ);
        double currentSpeed = Math.sqrt(Velocity.mc.field_71439_g.field_70159_w * Velocity.mc.field_71439_g.field_70159_w + Velocity.mc.field_71439_g.field_70179_y * Velocity.mc.field_71439_g.field_70179_y);
        double verticalMotion = Math.abs(event.getY());
        boolean shouldTriggerBuffer2 = false;
        int calculatedDelayTicks = (Integer)this.delayTicks.getValue();
        if (isAirborne) {
            if (kbMagnitude > 0.15 || verticalMotion > 0.25) {
                shouldTriggerBuffer2 = true;
                calculatedDelayTicks = (Integer)this.airDelayTick.getValue();
                this.buffer2ShouldSmartRot = true;
                if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s[Buffer2-Air] Triggered: KB=%.3f, Y=%.3f, Speed=%.3f&r", Myau.clientName, kbMagnitude, verticalMotion, currentSpeed));
                }
            }
        } else if (kbMagnitude > 0.2 || currentSpeed < 0.1 && kbMagnitude > 0.1) {
            shouldTriggerBuffer2 = true;
            calculatedDelayTicks = kbMagnitude > 0.3 ? Math.min((Integer)this.delayTicks.getValue() + 1, 5) : (Integer)this.delayTicks.getValue();
            this.buffer2ShouldJumpReset = (Boolean)this.jumpReset.getValue() != false && event.getY() > 0.0;
            boolean bl = this.buffer2ShouldSmartRot = (Boolean)this.smartRotJumpReset.getValue() != false && event.getY() > 0.0;
            if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                ChatUtil.sendFormatted(String.format("%s[Buffer2-Ground] Triggered: KB=%.3f, Y=%.3f, Speed=%.3f, Delay=%d&r", Myau.clientName, kbMagnitude, verticalMotion, currentSpeed, calculatedDelayTicks));
            }
        }
        if (shouldTriggerBuffer2) {
            this.knockbackX = kbX;
            this.knockbackZ = kbZ;
            this.buffer2Active = true;
            this.buffer2RemainingTicks = calculatedDelayTicks;
            if (isAirborne) {
                this.buffer2AttackBlocked = true;
            }
            if (!(this.reverseFlag || this.isInLiquidOrWeb() || this.pendingExplosion)) {
                Myau.delayManager.setDelayState(true, DelayModules.VELOCITY);
                this.reverseFlag = true;
                this.reverseStartTime = System.currentTimeMillis();
                if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s[Buffer2] Delay set to %d ticks&r", Myau.clientName, calculatedDelayTicks));
                }
            }
            if (this.buffer2ShouldJumpReset && event.getY() > 0.0) {
                this.jumpFlag = true;
                if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s[Buffer2] JumpReset triggered (Y=%.2f)&r", Myau.clientName, event.getY()));
                }
            }
            if ((Integer)this.horizontal.getValue() > 0) {
                event.setX(event.getX() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                event.setZ(event.getZ() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
            }
            if ((Integer)this.vertical.getValue() > 0) {
                event.setY(event.getY() * (double)((Integer)this.vertical.getValue()).intValue() / 100.0);
            }
            this.attackReduceTriggered = true;
            if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                ChatUtil.sendFormatted(String.format("%s[Buffer2] Applied reduction: X=%.3f, Z=%.3f, Y=%.3f&r", Myau.clientName, event.getX(), event.getZ(), event.getY()));
            }
            return;
        }
        if (((Boolean)this.useIntaveFactor.getValue()).booleanValue()) {
            this.applyIntaveFactorReduction(event);
        } else {
            if ((Integer)this.horizontal.getValue() > 0) {
                event.setX(event.getX() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                event.setZ(event.getZ() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
            }
            if ((Integer)this.vertical.getValue() > 0) {
                event.setY(event.getY() * (double)((Integer)this.vertical.getValue()).intValue() / 100.0);
            }
        }
    }

    private void applyIntaveFactorReduction(KnockbackEvent event) {
        float factor = ((Float)this.intaveFactor.getValue()).floatValue();
        event.setX(event.getX() * (double)factor);
        event.setZ(event.getZ() * (double)factor);
        Velocity.mc.field_71439_g.field_70159_w *= (double)factor;
        Velocity.mc.field_71439_g.field_70179_y *= (double)factor;
        if (((Boolean)this.keepVerticalIntave.getValue()).booleanValue()) {
            if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                ChatUtil.sendFormatted(String.format("%s[Intave] Factor %.2f: Event(X=%.3f,Z=%.3f), Motion(X=%.3f,Z=%.3f), KeepY=%.3f&r", Myau.clientName, Float.valueOf(factor), event.getX(), event.getZ(), Velocity.mc.field_71439_g.field_70159_w, Velocity.mc.field_71439_g.field_70179_y, event.getY()));
            }
        } else {
            event.setY(event.getY() * (double)factor);
            Velocity.mc.field_71439_g.field_70181_x *= (double)factor;
            if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                ChatUtil.sendFormatted(String.format("%s[Intave] Factor %.2f to all axes&r", Myau.clientName, Float.valueOf(factor)));
            }
        }
    }

    private void applyVanilla(KnockbackEvent event) {
        if ((Integer)this.horizontal.getValue() > 0) {
            event.setX(event.getX() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
            event.setZ(event.getZ() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
        } else {
            event.setX(Velocity.mc.field_71439_g.field_70159_w);
            event.setZ(Velocity.mc.field_71439_g.field_70179_y);
        }
        if ((Integer)this.vertical.getValue() > 0) {
            event.setY(event.getY() * (double)((Integer)this.vertical.getValue()).intValue() / 100.0);
        } else {
            event.setY(Velocity.mc.field_71439_g.field_70181_x);
        }
    }

    private void handleExplosion(KnockbackEvent event) {
        if ((Integer)this.explosionHorizontal.getValue() > 0) {
            event.setX(event.getX() * (double)((Integer)this.explosionHorizontal.getValue()).intValue() / 100.0);
            event.setZ(event.getZ() * (double)((Integer)this.explosionHorizontal.getValue()).intValue() / 100.0);
        } else {
            event.setX(Velocity.mc.field_71439_g.field_70159_w);
            event.setZ(Velocity.mc.field_71439_g.field_70179_y);
        }
        if ((Integer)this.explosionVertical.getValue() > 0) {
            event.setY(event.getY() * (double)((Integer)this.explosionVertical.getValue()).intValue() / 100.0);
        } else {
            event.setY(Velocity.mc.field_71439_g.field_70181_x);
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (event.getType() == EventType.PRE && this.isEnabled() && Velocity.mc.field_71439_g != null && this.isSmartRotating && this.smartRotProgress < this.smartRotDuration) {
            float progress = (float)this.smartRotProgress / (float)this.smartRotDuration;
            float smoothedYaw = this.smartRotStartYaw + (this.smartRotTargetYaw - this.smartRotStartYaw) * progress;
            event.setRotation(smoothedYaw, event.getPitch(), 2);
            event.setPervRotation(smoothedYaw, 2);
            if (((Boolean)this.debugLog.getValue()).booleanValue() && this.smartRotProgress == 0) {
                ChatUtil.sendFormatted(String.format("%s[SmartRot] Smoothing: %.1f -> %.1f (%.0f%%)&r", Myau.clientName, Float.valueOf(this.smartRotStartYaw), Float.valueOf(smoothedYaw), Float.valueOf(progress * 100.0f)));
            }
            ++this.smartRotProgress;
            if (((Boolean)this.reduceMode.getValue()).booleanValue() && (Integer)this.reduceType.getValue() == 2) {
                if (Velocity.mc.field_71439_g.field_70737_aN == 9) {
                    ++this.arcaneHitsCount;
                }
                ++this.arcaneTicksCount;
            }
            if (((Boolean)this.autoMove.getValue()).booleanValue()) {
                Velocity.mc.field_71439_g.field_71158_b.field_78900_b = 1.0f;
            }
        }
        if (event.getType() == EventType.POST && this.isEnabled() && Velocity.mc.field_71439_g != null) {
            long requiredTicks;
            long currentDelay;
            if (!this.cachedVelocityPackets.isEmpty() && this.packetDelayCounter > 0) {
                --this.packetDelayCounter;
                if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s[CachedPacket] Delay counter: %d&r", Myau.clientName, this.packetDelayCounter));
                }
                if (this.packetDelayCounter <= 0 && !this.isProcessingCachedPacket) {
                    this.isProcessingCachedPacket = true;
                    S12PacketEntityVelocity packet = this.cachedVelocityPackets.poll();
                    if (packet != null) {
                        this.applyCachedVelocity(packet);
                    }
                    this.isProcessingCachedPacket = false;
                }
            }
            if (this.isSmartRotating && this.smartRotProgress >= this.smartRotDuration) {
                this.isSmartRotating = false;
                this.smartRotProgress = 0;
                this.isAirSmartRot = false;
                if (((Boolean)this.autoJump.getValue()).booleanValue() && Velocity.mc.field_71439_g.field_70122_E && Velocity.mc.field_71439_g.func_70051_ag() && !this.isInLiquidOrWeb()) {
                    Velocity.mc.field_71439_g.func_70664_aZ();
                    if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                        ChatUtil.sendFormatted(String.format("%s[SmartRot] AutoJump executed&r", Myau.clientName));
                    }
                }
                if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s[SmartRot] Rotation completed&r", Myau.clientName));
                }
            }
            if (((Boolean)this.watchdogReduce.getValue()).booleanValue()) {
                boolean shouldSendDelayedPackets = false;
                if (Velocity.mc.field_71439_g.field_82175_bq && this.isDelayingPackets) {
                    shouldSendDelayedPackets = true;
                } else if (Velocity.mc.field_71439_g.field_70737_aN < 3 && this.isDelayingPackets) {
                    shouldSendDelayedPackets = true;
                } else if (Velocity.mc.field_71439_g.func_71039_bw() && ItemUtil.isHoldingSword() && this.isDelayingPackets) {
                    shouldSendDelayedPackets = true;
                }
                if (shouldSendDelayedPackets) {
                    this.isDelayingPackets = false;
                    this.isSendingQueuedPackets = true;
                    for (Packet<?> packet : this.delayedPackets) {
                        PacketUtil.sendPacket(packet);
                    }
                    this.delayedPackets.clear();
                    this.isSendingQueuedPackets = false;
                    if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                        ChatUtil.sendFormatted(String.format("%s[Watchdog] Sent delayed packets&r", Myau.clientName));
                    }
                }
                if (Velocity.mc.field_71439_g.field_70173_aa > 20 && this.isDelayingPackets) {
                    this.isDelayingPackets = false;
                    this.isSendingQueuedPackets = true;
                    for (Packet<?> packet : this.delayedPackets) {
                        PacketUtil.sendPacket(packet);
                    }
                    this.delayedPackets.clear();
                    this.isSendingQueuedPackets = false;
                    if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                        ChatUtil.sendFormatted(String.format("%s[Watchdog] Force sent delayed packets (safety)&r", Myau.clientName));
                    }
                }
            }
            if (this.buffer2Active) {
                if (this.buffer2RemainingTicks > 0) {
                    --this.buffer2RemainingTicks;
                    if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                        ChatUtil.sendFormatted(String.format("%s[Buffer2] Remaining ticks: %d&r", Myau.clientName, this.buffer2RemainingTicks));
                    }
                } else {
                    this.buffer2Active = false;
                    this.buffer2AttackBlocked = false;
                    if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                        ChatUtil.sendFormatted(String.format("%s[Buffer2] Completed&r", Myau.clientName));
                    }
                }
            }
            if (((Float)this.astolftor.getValue()).floatValue() < 1.0f && Velocity.mc.field_71439_g.field_70737_aN == (Integer)this.hurt.getValue() && System.currentTimeMillis() - this.lastAttackTime <= 8000L) {
                Velocity.mc.field_71439_g.field_70159_w *= (double)((Float)this.astolftor.getValue()).floatValue();
                Velocity.mc.field_71439_g.field_70179_y *= (double)((Float)this.astolftor.getValue()).floatValue();
            }
            if (this.reverseFlag && (currentDelay = Myau.delayManager.getDelay()) >= (requiredTicks = (long)(((Integer)this.delayTicks.getValue() + 1) / 2))) {
                Myau.delayManager.setDelayState(false, DelayModules.VELOCITY);
                this.reverseFlag = false;
                Myau.blinkManager.setBlinkState(false, BlinkModules.BLINK);
                if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s[Buffer3] Released after %d ticks&r", Myau.clientName, currentDelay));
                }
            }
            if (this.delayActive) {
                MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
                this.delayActive = false;
            }
            if (((Boolean)this.test.getValue()).booleanValue()) {
                Myau.blinkManager.setBlinkState(false, BlinkModules.BLINK);
            }
        }
    }

    private void applyCachedVelocity(S12PacketEntityVelocity packet) {
        if (Velocity.mc.field_71439_g == null) {
            return;
        }
        double motionX = (double)packet.func_149411_d() / 8000.0;
        double motionY = (double)packet.func_149410_e() / 8000.0;
        double motionZ = (double)packet.func_149409_f() / 8000.0;
        Velocity.mc.field_71439_g.field_70159_w = motionX;
        Velocity.mc.field_71439_g.field_70181_x = motionY;
        Velocity.mc.field_71439_g.field_70179_y = motionZ;
        if (((Boolean)this.debugLog.getValue()).booleanValue()) {
            ChatUtil.sendFormatted(String.format("%s[CachedPacket] Applied: X=%.3f, Y=%.3f, Z=%.3f&r", Myau.clientName, motionX, motionY, motionZ));
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (this.isEnabled() && (Integer)this.mode.getValue() == 2 && Velocity.mc.field_71439_g != null && this.isSmartRotating && ((Boolean)this.autoMove.getValue()).booleanValue()) {
            Velocity.mc.field_71439_g.field_71158_b.field_78900_b = 1.0f;
            if (((Boolean)this.debugLog.getValue()).booleanValue() && this.smartRotProgress == 1) {
                ChatUtil.sendFormatted(String.format("%s[AutoMove] Active during SmartRot&r", Myau.clientName));
            }
        }
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (!this.isEnabled() || Velocity.mc.field_71439_g == null) {
            return;
        }
        if ((Integer)this.mode.getValue() == 2) {
            if (this.buffer2Active && this.buffer2AttackBlocked) {
                event.setCancelled(true);
                if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s[Buffer2] Blocked attack (Remaining ticks: %d)&r", Myau.clientName, this.buffer2RemainingTicks));
                }
                return;
            }
            if (((Boolean)this.attackReduce.getValue()).booleanValue() && this.attackReduceTriggered && Velocity.mc.field_71439_g.field_70737_aN > 0 && Velocity.mc.field_71439_g.field_70737_aN <= 10 && ((Boolean)this.debugLog.getValue()).booleanValue()) {
                ChatUtil.sendFormatted(Myau.clientName + "[AttackReduce] Attack event detected");
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        S27PacketExplosion packet;
        if (!this.isEnabled() || event.getType() != EventType.RECEIVE || event.isCancelled() || Velocity.mc.field_71439_g == null) {
            return;
        }
        if (((Boolean)this.watchdogReduce.getValue()).booleanValue() && event.getPacket() instanceof S08PacketPlayerPosLook) {
            S08PacketPlayerPosLook positionLookPacket = (S08PacketPlayerPosLook)event.getPacket();
            if (this.isSendingQueuedPackets || Velocity.mc.field_71439_g.field_70737_aN < 3 || Velocity.mc.field_71439_g.field_70128_L || Velocity.mc.field_71439_g.func_71039_bw() && ItemUtil.isHoldingSword()) {
                return;
            }
            if (!Velocity.mc.field_71439_g.field_82175_bq) {
                this.delayedPackets.add((Packet<?>)positionLookPacket);
                this.isDelayingPackets = true;
                event.setCancelled(true);
                if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s[Watchdog] Delayed S08 packet&r", Myau.clientName));
                }
            }
            return;
        }
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity packet2 = (S12PacketEntityVelocity)event.getPacket();
            if (packet2.func_149412_c() != Velocity.mc.field_71439_g.func_145782_y()) {
                return;
            }
            if ((Integer)this.mode.getValue() == 2 && ((Boolean)this.reduceMode.getValue()).booleanValue() && (Integer)this.reduceType.getValue() == 2) {
                this.arcaneHasReceivedVelocity = true;
                double velocityX = (double)packet2.func_149411_d() / 8000.0;
                double velocityY = (double)packet2.func_149410_e() / 8000.0;
                double velocityZ = (double)packet2.func_149409_f() / 8000.0;
                boolean bl = this.arcaneIsFallDamage = velocityX == 0.0 && velocityZ == 0.0 && velocityY < 0.0;
                if (((Boolean)this.reduceDebug.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s[AttackReduce] Velocity received, fallDamage: %b&r", Myau.clientName, this.arcaneIsFallDamage));
                }
            }
            double velX = (double)packet2.func_149411_d() / 8000.0;
            double velZ = (double)packet2.func_149409_f() / 8000.0;
            double strength = Math.sqrt(velX * velX + velZ * velZ);
            if ((Integer)this.mode.getValue() == 2 && ((Boolean)this.buffer.getValue()).booleanValue()) {
                boolean shouldCache = false;
                int delayTicks = 0;
                if (Velocity.mc.field_71439_g.field_70122_E) {
                    if (strength > 0.15) {
                        shouldCache = true;
                        delayTicks = 1;
                    }
                } else if (strength > 0.2) {
                    shouldCache = true;
                    delayTicks = (Integer)this.airDelayTick.getValue();
                } else if (strength > 0.1) {
                    shouldCache = true;
                    delayTicks = Math.max(1, (Integer)this.airDelayTick.getValue() - 1);
                }
                if (shouldCache) {
                    this.cachedVelocityPackets.offer(packet2);
                    this.packetDelayCounter = delayTicks;
                    event.setCancelled(true);
                    if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                        ChatUtil.sendFormatted(String.format("%s[CachedPacket] Strength: %.3f, Delay: %dt&r", Myau.clientName, strength, delayTicks));
                    }
                    return;
                }
            }
            if ((Integer)this.mode.getValue() == 2) {
                boolean canStartJump;
                Module longJump = Myau.moduleManager.getModule("LongJump");
                boolean bl = canStartJump = longJump != null && longJump.isEnabled() && ((LongJump)longJump).canStartJump();
                if (((Boolean)this.buffer3.getValue()).booleanValue()) {
                    boolean shouldCheckGround;
                    boolean bl2 = shouldCheckGround = (Boolean)this.buffer.getValue() == false && (Boolean)this.buffer3.getValue() == false;
                    if (!(this.reverseFlag || this.isInLiquidOrWeb() || this.pendingExplosion || this.allowNext && ((Boolean)this.fakeCheck.getValue()).booleanValue() || canStartJump)) {
                        if (shouldCheckGround && !Velocity.mc.field_71439_g.field_70122_E) {
                            if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                                ChatUtil.sendFormatted(String.format("%s[Buffer3] In air, skipping delay&r", Myau.clientName));
                            }
                        } else {
                            this.delayChanceCounter = this.delayChanceCounter % 100 + (Integer)this.delayChance.getValue();
                            if (this.delayChanceCounter >= 100) {
                                Myau.delayManager.setDelayState(true, DelayModules.VELOCITY);
                                this.reverseFlag = true;
                                this.reverseStartTime = System.currentTimeMillis();
                                this.delayChanceCounter = 0;
                                return;
                            }
                        }
                    }
                } else if (!(!((Boolean)this.buffer2.getValue()).booleanValue() || this.reverseFlag || this.isInLiquidOrWeb() || this.pendingExplosion || this.allowNext || canStartJump)) {
                    this.delayChanceCounter = this.delayChanceCounter % 100 + (Integer)this.delayChance.getValue();
                    if (this.delayChanceCounter >= 100) {
                        Myau.delayManager.setDelayState(true, DelayModules.VELOCITY);
                        this.reverseFlag = true;
                        this.reverseStartTime = System.currentTimeMillis();
                        this.delayChanceCounter = 0;
                        return;
                    }
                }
            }
        } else if (event.getPacket() instanceof S19PacketEntityStatus) {
            Entity entity;
            S19PacketEntityStatus packet3 = (S19PacketEntityStatus)event.getPacket();
            if (Velocity.mc.field_71441_e != null && (entity = packet3.func_149161_a((World)Velocity.mc.field_71441_e)) != null && entity.equals((Object)Velocity.mc.field_71439_g) && packet3.func_149160_c() == 2) {
                this.allowNext = false;
                if ((Integer)this.mode.getValue() == 2 && ((Boolean)this.attackReduce.getValue()).booleanValue()) {
                    this.attackReduceTriggered = true;
                }
            }
        } else if (event.getPacket() instanceof S27PacketExplosion && ((packet = (S27PacketExplosion)event.getPacket()).func_149149_c() != 0.0f || packet.func_149144_d() != 0.0f || packet.func_149147_e() != 0.0f)) {
            this.pendingExplosion = true;
            if ((Integer)this.explosionHorizontal.getValue() == 0 || (Integer)this.explosionVertical.getValue() == 0) {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onLatePacket(PacketEvent event) {
    }

    @EventTarget
    public void onSendPacket(PacketEvent event) {
        C02PacketUseEntity packet;
        if (this.isEnabled() && (Integer)this.mode.getValue() == 2 && event.getType() == EventType.SEND && !event.isCancelled() && event.getPacket() instanceof C02PacketUseEntity && (packet = (C02PacketUseEntity)event.getPacket()).func_149565_c() == C02PacketUseEntity.Action.ATTACK) {
            this.lastAttackTime = System.currentTimeMillis();
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.jumpFlag) {
            this.jumpFlag = false;
            if (Velocity.mc.field_71439_g != null && Velocity.mc.field_71439_g.field_70122_E && Velocity.mc.field_71439_g.func_70051_ag() && !Velocity.mc.field_71439_g.func_70644_a(Potion.field_76430_j) && !this.isInLiquidOrWeb()) {
                Velocity.mc.field_71439_g.field_71158_b.field_78901_c = true;
                if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s[JumpReset] Executed&r", Myau.clientName));
                }
            }
            this.attackReduceSuccess = false;
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.onDisabled();
    }

    @Override
    public void onEnabled() {
        this.resetAllFlags();
    }

    @Override
    public void onDisabled() {
        this.resetAllFlags();
        if (Myau.delayManager.getDelayModule() == DelayModules.VELOCITY) {
            Myau.delayManager.setDelayState(false, DelayModules.VELOCITY);
        }
        Myau.delayManager.delayedPacket.clear();
        Myau.blinkManager.setBlinkState(false, BlinkModules.BLINK);
    }

    private void resetAllFlags() {
        this.pendingExplosion = false;
        this.allowNext = true;
        this.chanceCounter = 0;
        this.delayChanceCounter = 0;
        this.reverseFlag = false;
        this.delayActive = false;
        this.lastAttackTime = 0L;
        this.blinkStartTime = System.currentTimeMillis();
        this.reverseStartTime = 0L;
        this.jumpFlag = false;
        this.attackReduceTriggered = false;
        this.attackReduceSuccess = false;
        this.rotatoTickCounter = 0;
        this.targetRotation = null;
        this.knockbackX = 0.0;
        this.knockbackZ = 0.0;
        this.isSmartRotActive = false;
        this.isAirSmartRot = false;
        this.buffer2Active = false;
        this.buffer2RemainingTicks = 0;
        this.buffer2ShouldJumpReset = false;
        this.buffer2ShouldSmartRot = false;
        this.buffer2AttackBlocked = false;
        this.recentKnockbacks.clear();
        this.isDelayingPackets = false;
        this.isSendingQueuedPackets = false;
        this.delayedPackets.clear();
        this.isSmartRotating = false;
        this.smartRotProgress = 0;
        this.knockbackStrength = 0.0;
        this.knockbackDirectionAngle = 0.0;
        this.cachedVelocityPackets.clear();
        this.packetDelayCounter = 0;
        this.isProcessingCachedPacket = false;
        this.arcaneHasReceivedVelocity = false;
        this.arcaneNoAttack = true;
        this.arcaneTarget = null;
        this.arcaneHitsCount = 0;
        this.arcaneTicksCount = 0;
        this.arcaneIsFallDamage = false;
    }

    @Override
    public String[] getSuffix() {
        String modeName = this.mode.getModeString();
        if ((Integer)this.mode.getValue() == 2) {
            if (((Boolean)this.buffer3.getValue()).booleanValue()) {
                return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modeName), "Buffer3", this.rotationMode.getModeString()};
            }
            if (((Boolean)this.buffer2.getValue()).booleanValue()) {
                return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modeName), "Buffer2", this.rotationMode.getModeString()};
            }
            if (((Boolean)this.buffer.getValue()).booleanValue()) {
                return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modeName), "Buffer", this.rotationMode.getModeString()};
            }
            if (((Boolean)this.smartRotJumpReset.getValue()).booleanValue()) {
                if (((Boolean)this.airRotate.getValue()).booleanValue() && this.isAirSmartRot) {
                    return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modeName), "SmartRot", this.rotationMode.getModeString(), "AirT" + this.airRotateTicks.getValue()};
                }
                return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modeName), "SmartRot", this.rotationMode.getModeString(), "T" + this.rotateTicks.getValue()};
            }
            if (((Boolean)this.useIntaveFactor.getValue()).booleanValue()) {
                return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modeName), String.format("Factor%.2f", this.intaveFactor.getValue())};
            }
            if (((Boolean)this.attackReduce.getValue()).booleanValue()) {
                return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modeName), "AR"};
            }
        }
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modeName)};
    }
}

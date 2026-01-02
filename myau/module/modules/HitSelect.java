package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.events.UpdateEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class HitSelect
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"SECOND", "CRITICALS", "W_TAP", "CRIT3"});
    public final BooleanProperty autoJumpCrit = new BooleanProperty("AutoJump", true);
    public final BooleanProperty edgeJump = new BooleanProperty("EdgeJump", true);
    public final FloatProperty jumpHeight = new FloatProperty("JumpHeight", Float.valueOf(0.42f), Float.valueOf(0.3f), Float.valueOf(0.5f));
    public final BooleanProperty heightCheck = new BooleanProperty("HeightCheck", true);
    public final BooleanProperty motionPreserve = new BooleanProperty("MotionPreserve", true);
    private boolean sprintState = false;
    private boolean set = false;
    private double savedSlowdown = 0.0;
    private int critCooldown = 0;
    private boolean shouldCritJump = false;
    private boolean wasOnGround = true;
    private int groundTicks = 0;
    private int airTicks = 0;
    private double lastGroundY = 0.0;
    private int critAttempts = 0;
    private int critSuccess = 0;
    private int blockedHits = 0;
    private int allowedHits = 0;

    public HitSelect() {
        super("HitSelect", false);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getType() == EventType.POST) {
            this.resetMotion();
            if (this.critCooldown > 0) {
                --this.critCooldown;
            }
            if ((Integer)this.mode.getValue() == 3) {
                this.updateCrit3State();
            }
        }
    }

    @EventTarget(value=0)
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled() || event.getType() != EventType.SEND || event.isCancelled()) {
            return;
        }
        if (HitSelect.mc.field_71439_g == null || HitSelect.mc.field_71441_e == null) {
            return;
        }
        if (event.getPacket() instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction packet = (C0BPacketEntityAction)event.getPacket();
            switch (packet.func_180764_b()) {
                case START_SPRINTING: {
                    this.sprintState = true;
                    break;
                }
                case STOP_SPRINTING: {
                    this.sprintState = false;
                }
            }
            return;
        }
        if (event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity use = (C02PacketUseEntity)event.getPacket();
            if (use.func_149565_c() != C02PacketUseEntity.Action.ATTACK) {
                return;
            }
            Entity target = use.func_149564_a((World)HitSelect.mc.field_71441_e);
            if (target == null || target instanceof EntityLargeFireball) {
                return;
            }
            if (!(target instanceof EntityLivingBase)) {
                return;
            }
            EntityLivingBase living = (EntityLivingBase)target;
            boolean allow = true;
            switch ((Integer)this.mode.getValue()) {
                case 0: {
                    allow = this.prioritizeSecondHit((EntityLivingBase)HitSelect.mc.field_71439_g, living);
                    break;
                }
                case 1: {
                    allow = this.prioritizeCriticalHits((EntityLivingBase)HitSelect.mc.field_71439_g);
                    break;
                }
                case 2: {
                    allow = this.prioritizeWTapHits((EntityLivingBase)HitSelect.mc.field_71439_g, this.sprintState);
                    break;
                }
                case 3: {
                    this.optimizeCrit3Attack((EntityLivingBase)HitSelect.mc.field_71439_g, living);
                    allow = true;
                }
            }
            if (!allow) {
                event.setCancelled(true);
                ++this.blockedHits;
            } else {
                ++this.allowedHits;
                if ((Integer)this.mode.getValue() == 3) {
                    this.recordCritAttempt((EntityLivingBase)HitSelect.mc.field_71439_g);
                }
            }
        }
    }

    private void optimizeCrit3Attack(EntityLivingBase player, EntityLivingBase target) {
        this.critCooldown = 5;
        boolean canCrit = this.canCriticallyHit(player);
        ++this.critAttempts;
        if (canCrit) {
            ++this.critSuccess;
        }
        if (!canCrit && this.shouldAttemptCrit(player, target)) {
            this.prepareForCrit(player, target);
        }
        this.postAttackAdjustment(player, target);
    }

    private boolean canCriticallyHit(EntityLivingBase player) {
        if (player.field_70143_R <= 0.0f) {
            return false;
        }
        if (player.field_70122_E) {
            return false;
        }
        if (player.func_70090_H()) {
            return false;
        }
        if (player.func_70644_a(Potion.field_76430_j)) {
            return false;
        }
        if (player.func_70115_ae()) {
            return false;
        }
        if (player.func_70093_af()) {
            return false;
        }
        return !player.func_70644_a(Potion.field_76440_q);
    }

    private boolean shouldAttemptCrit(EntityLivingBase player, EntityLivingBase target) {
        double heightDiff;
        if (this.critCooldown > 0) {
            return false;
        }
        double distance = player.func_70032_d((Entity)target);
        if (distance > 4.0) {
            return false;
        }
        if (player.field_70737_aN > 0) {
            return false;
        }
        if (!player.field_70122_E) {
            return false;
        }
        if (((Boolean)this.heightCheck.getValue()).booleanValue() && (heightDiff = Math.abs(player.field_70163_u - target.field_70163_u)) > 1.5) {
            return false;
        }
        if (((Boolean)this.edgeJump.getValue()).booleanValue()) {
            Vec3 lookVec = player.func_70040_Z();
            double checkX = player.field_70165_t + lookVec.field_72450_a;
            double checkZ = player.field_70161_v + lookVec.field_72449_c;
            if (!HitSelect.mc.field_71441_e.func_175623_d(new BlockPos(checkX, player.field_70163_u - 0.5, checkZ))) {
                return true;
            }
        }
        return true;
    }

    private void prepareForCrit(EntityLivingBase player, EntityLivingBase target) {
        if (((Boolean)this.autoJumpCrit.getValue()).booleanValue()) {
            double distance = player.func_70032_d((Entity)target);
            double angleToTarget = this.getAngleToTarget(player, target);
            if (distance >= 2.0 && distance <= 3.5 && Math.abs(angleToTarget) < 45.0) {
                this.shouldCritJump = true;
                this.lastGroundY = player.field_70163_u;
                if (((Boolean)this.motionPreserve.getValue()).booleanValue()) {
                    this.preserveHorizontalMomentum(player);
                }
            }
        }
    }

    private void postAttackAdjustment(EntityLivingBase player, EntityLivingBase target) {
        if (this.canCriticallyHit(player) && player.field_70143_R > 0.5f) {
            Vec3 toTarget = new Vec3(target.field_70165_t - player.field_70165_t, 0.0, target.field_70161_v - player.field_70161_v).func_72432_b();
            if (((Boolean)this.motionPreserve.getValue()).booleanValue()) {
                player.field_70159_w += toTarget.field_72450_a * 0.02;
                player.field_70179_y += toTarget.field_72449_c * 0.02;
            }
        }
    }

    private void updateCrit3State() {
        if (this.shouldCritJump && HitSelect.mc.field_71439_g.field_70122_E && this.groundTicks >= 2) {
            this.executeCritJump();
            this.shouldCritJump = false;
        }
        if (HitSelect.mc.field_71439_g.field_70122_E) {
            ++this.groundTicks;
            this.airTicks = 0;
            this.wasOnGround = true;
        } else {
            ++this.airTicks;
            this.groundTicks = 0;
            this.wasOnGround = false;
        }
        if (this.shouldCritJump && this.groundTicks > 20) {
            this.shouldCritJump = false;
        }
        if (!HitSelect.mc.field_71439_g.field_70122_E && HitSelect.mc.field_71439_g.field_70143_R > 0.1f) {
            this.optimizeFall();
        }
    }

    private void executeCritJump() {
        HitSelect.mc.field_71439_g.field_70181_x = ((Float)this.jumpHeight.getValue()).floatValue();
        if (((Boolean)this.motionPreserve.getValue()).booleanValue()) {
            Vec3 lookVec = HitSelect.mc.field_71439_g.func_70040_Z();
            HitSelect.mc.field_71439_g.field_70159_w += lookVec.field_72450_a * 0.05;
            HitSelect.mc.field_71439_g.field_70179_y += lookVec.field_72449_c * 0.05;
            double speed = Math.sqrt(HitSelect.mc.field_71439_g.field_70159_w * HitSelect.mc.field_71439_g.field_70159_w + HitSelect.mc.field_71439_g.field_70179_y * HitSelect.mc.field_71439_g.field_70179_y);
            if (speed > 0.5) {
                HitSelect.mc.field_71439_g.field_70159_w *= 0.5 / speed;
                HitSelect.mc.field_71439_g.field_70179_y *= 0.5 / speed;
            }
        }
    }

    private void optimizeFall() {
        if (HitSelect.mc.field_71439_g.field_70181_x < -0.4) {
            HitSelect.mc.field_71439_g.field_70181_x = -0.4;
        }
        if (Math.abs(HitSelect.mc.field_71439_g.field_70159_w) < 0.1 && Math.abs(HitSelect.mc.field_71439_g.field_70179_y) < 0.1) {
            Vec3 lookVec = HitSelect.mc.field_71439_g.func_70040_Z();
            HitSelect.mc.field_71439_g.field_70159_w += lookVec.field_72450_a * 0.01;
            HitSelect.mc.field_71439_g.field_70179_y += lookVec.field_72449_c * 0.01;
        }
    }

    private void recordCritAttempt(EntityLivingBase player) {
        if (this.critAttempts > 1000) {
            this.critAttempts = 500;
            this.critSuccess = (int)(this.getCritRate() * 500.0);
        }
    }

    private double getAngleToTarget(EntityLivingBase player, EntityLivingBase target) {
        double dx = target.field_70165_t - player.field_70165_t;
        double dz = target.field_70161_v - player.field_70161_v;
        double targetYaw = Math.atan2(dz, dx) * 180.0 / Math.PI - 90.0;
        double playerYaw = MathHelper.func_76142_g((float)player.field_70177_z);
        return MathHelper.func_76142_g((float)((float)(targetYaw - playerYaw)));
    }

    private void preserveHorizontalMomentum(EntityLivingBase player) {
        double currentSpeed = Math.sqrt(player.field_70159_w * player.field_70159_w + player.field_70179_y * player.field_70179_y);
        if (currentSpeed > 0.1) {
            player.field_70159_w *= 1.05;
            player.field_70179_y *= 1.05;
        }
    }

    private double getCritRate() {
        if (this.critAttempts == 0) {
            return 0.0;
        }
        return (double)this.critSuccess / (double)this.critAttempts;
    }

    private boolean prioritizeSecondHit(EntityLivingBase player, EntityLivingBase target) {
        if (target.field_70737_aN != 0) {
            return true;
        }
        if (player.field_70737_aN <= player.field_70738_aO - 1) {
            return true;
        }
        double dist = player.func_70032_d((Entity)target);
        if (dist < 2.5) {
            return true;
        }
        if (!this.isMovingTowards(target, player, 60.0)) {
            return true;
        }
        if (!this.isMovingTowards(player, target, 60.0)) {
            return true;
        }
        this.fixMotion();
        return false;
    }

    private boolean prioritizeCriticalHits(EntityLivingBase player) {
        if (player.field_70122_E) {
            return true;
        }
        if (player.field_70737_aN != 0) {
            return true;
        }
        if (player.field_70143_R > 0.0f) {
            return true;
        }
        this.fixMotion();
        return false;
    }

    private boolean prioritizeWTapHits(EntityLivingBase player, boolean sprinting) {
        if (player.field_70123_F) {
            return true;
        }
        if (!HitSelect.mc.field_71474_y.field_74351_w.func_151470_d()) {
            return true;
        }
        if (sprinting) {
            return true;
        }
        this.fixMotion();
        return false;
    }

    private void fixMotion() {
        if (this.set) {
            return;
        }
        this.set = true;
    }

    private void resetMotion() {
        if (!this.set) {
            return;
        }
        this.set = false;
        this.savedSlowdown = 0.0;
    }

    private boolean isMovingTowards(EntityLivingBase source, EntityLivingBase target, double maxAngle) {
        Vec3 currentPos = source.func_174791_d();
        Vec3 lastPos = new Vec3(source.field_70142_S, source.field_70137_T, source.field_70136_U);
        Vec3 targetPos = target.func_174791_d();
        double mx = currentPos.field_72450_a - lastPos.field_72450_a;
        double mz = currentPos.field_72449_c - lastPos.field_72449_c;
        double movementLength = Math.sqrt(mx * mx + mz * mz);
        if (movementLength == 0.0) {
            return false;
        }
        mx /= movementLength;
        mz /= movementLength;
        double tx = targetPos.field_72450_a - currentPos.field_72450_a;
        double tz = targetPos.field_72449_c - currentPos.field_72449_c;
        double targetLength = Math.sqrt(tx * tx + tz * tz);
        if (targetLength == 0.0) {
            return false;
        }
        double dotProduct = mx * (tx /= targetLength) + mz * (tz /= targetLength);
        return dotProduct >= Math.cos(Math.toRadians(maxAngle));
    }

    @Override
    public void onDisabled() {
        this.resetMotion();
        this.sprintState = false;
        this.set = false;
        this.savedSlowdown = 0.0;
        this.blockedHits = 0;
        this.allowedHits = 0;
        this.critCooldown = 0;
        this.shouldCritJump = false;
        this.wasOnGround = true;
        this.groundTicks = 0;
        this.airTicks = 0;
        this.lastGroundY = 0.0;
        this.critAttempts = 0;
        this.critSuccess = 0;
    }

    @Override
    public String[] getSuffix() {
        if ((Integer)this.mode.getValue() == 3) {
            double critRate = this.getCritRate();
            String critStr = String.format("%.1f%%", critRate * 100.0);
            return new String[]{this.mode.getModeString() + " \u00a77[" + critStr + "]"};
        }
        return new String[]{this.mode.getModeString()};
    }
}

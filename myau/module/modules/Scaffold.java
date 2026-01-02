package myau.module.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.HitBlockEvent;
import myau.events.LeftClickMouseEvent;
import myau.events.LivingUpdateEvent;
import myau.events.MoveInputEvent;
import myau.events.Render2DEvent;
import myau.events.RightClickMouseEvent;
import myau.events.SafeWalkEvent;
import myau.events.StrafeEvent;
import myau.events.SwapItemEvent;
import myau.events.UpdateEvent;
import myau.management.RotationState;
import myau.module.Module;
import myau.module.modules.BedNuker;
import myau.module.modules.HUD;
import myau.module.modules.LongJump;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.BlockUtil;
import myau.util.ItemUtil;
import myau.util.MoveUtil;
import myau.util.PacketUtil;
import myau.util.PlayerUtil;
import myau.util.RandomUtil;
import myau.util.RotationUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.WorldSettings;

public class Scaffold
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final double[] placeOffsets = new double[]{0.03125, 0.09375, 0.15625, 0.21875, 0.28125, 0.34375, 0.40625, 0.46875, 0.53125, 0.59375, 0.65625, 0.71875, 0.78125, 0.84375, 0.90625, 0.96875};
    private int rotationTick = 0;
    private int lastSlot = -1;
    private int blockCount = -1;
    private float yaw = -180.0f;
    private float pitch = 0.0f;
    private boolean canRotate = false;
    private int towerTick = 0;
    private int towerDelay = 0;
    private int stage = 0;
    private int startY = 256;
    private boolean shouldKeepY = false;
    private boolean towering = false;
    private long lastYCheckLogTime = 0L;
    private boolean stopMovementNextTick = false;
    private EnumFacing targetFacing = null;
    public final ModeProperty rotationMode = new ModeProperty("rotations", 2, new String[]{"NONE", "DEFAULT", "BACKWARDS", "SIDEWAYS"});
    public final ModeProperty moveFix = new ModeProperty("move-fix", 1, new String[]{"NONE", "SILENT"});
    public final ModeProperty sprintMode = new ModeProperty("sprint", 0, new String[]{"NONE", "VANILLA"});
    public final PercentProperty groundMotion = new PercentProperty("ground-motion", 100);
    public final PercentProperty airMotion = new PercentProperty("air-motion", 100);
    public final PercentProperty speedMotion = new PercentProperty("speed-motion", 100);
    public final ModeProperty tower = new ModeProperty("tower", 0, new String[]{"NONE", "VANILLA", "EXTRA", "TELLY"});
    public final ModeProperty keepY = new ModeProperty("keep-y", 0, new String[]{"NONE", "VANILLA", "EXTRA", "TELLY"});
    public final BooleanProperty keepYonPress = new BooleanProperty("keep-y-on-press", false, () -> (Integer)this.keepY.getValue() != 0);
    public final BooleanProperty disableWhileJumpActive = new BooleanProperty("no-keep-y-on-jump-potion", false, () -> (Integer)this.keepY.getValue() != 0);
    public final BooleanProperty multiplace = new BooleanProperty("multi-place", true);
    public final BooleanProperty safeWalk = new BooleanProperty("safe-walk", true);
    public final BooleanProperty swing = new BooleanProperty("swing", true);
    public final BooleanProperty itemSpoof = new BooleanProperty("item-spoof", false);
    public final BooleanProperty blockCounter = new BooleanProperty("block-counter", true);

    private boolean shouldStopSprint() {
        if (this.isTowering()) {
            return false;
        }
        boolean stage = (Integer)this.keepY.getValue() == 1 || (Integer)this.keepY.getValue() == 2;
        return (!stage || this.stage <= 0) && (Integer)this.sprintMode.getValue() == 0;
    }

    private boolean canPlace() {
        BedNuker bedNuker = (BedNuker)Myau.moduleManager.modules.get(BedNuker.class);
        if (bedNuker.isEnabled() && bedNuker.isReady()) {
            return false;
        }
        LongJump longJump = (LongJump)Myau.moduleManager.modules.get(LongJump.class);
        return !longJump.isEnabled() || !longJump.isAutoMode() || longJump.isJumping();
    }

    private EnumFacing getBestFacing(BlockPos blockPos1, BlockPos blockPos3) {
        double offset = 0.0;
        EnumFacing enumFacing = null;
        for (EnumFacing facing : EnumFacing.field_82609_l) {
            BlockPos pos;
            if (facing == EnumFacing.DOWN || (pos = blockPos1.func_177972_a(facing)).func_177956_o() > blockPos3.func_177956_o()) continue;
            double distance = pos.func_177957_d((double)blockPos3.func_177958_n() + 0.5, (double)blockPos3.func_177956_o() + 0.5, (double)blockPos3.func_177952_p() + 0.5);
            if (enumFacing != null && !(distance < offset) && (distance != offset || facing != EnumFacing.UP)) continue;
            offset = distance;
            enumFacing = facing;
        }
        return enumFacing;
    }

    private BlockData getBlockData() {
        int startY = MathHelper.func_76128_c((double)Scaffold.mc.field_71439_g.field_70163_u);
        BlockPos targetPos = new BlockPos(MathHelper.func_76128_c((double)Scaffold.mc.field_71439_g.field_70165_t), (this.stage != 0 && !this.shouldKeepY ? Math.min(startY, this.startY) : startY) - 1, MathHelper.func_76128_c((double)Scaffold.mc.field_71439_g.field_70161_v));
        if (!BlockUtil.isReplaceable(targetPos)) {
            return null;
        }
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        for (int x = -4; x <= 4; ++x) {
            for (int y = -4; y <= 0; ++y) {
                for (int z = -4; z <= 4; ++z) {
                    BlockPos pos = targetPos.func_177982_a(x, y, z);
                    if (BlockUtil.isReplaceable(pos) || BlockUtil.isInteractable(pos) || Scaffold.mc.field_71439_g.func_70011_f((double)pos.func_177958_n() + 0.5, (double)pos.func_177956_o() + 0.5, (double)pos.func_177952_p() + 0.5) > (double)Scaffold.mc.field_71442_b.func_78757_d() || this.stage != 0 && !this.shouldKeepY && pos.func_177956_o() >= this.startY) continue;
                    for (EnumFacing facing : EnumFacing.field_82609_l) {
                        BlockPos blockPos;
                        if (facing == EnumFacing.DOWN || !BlockUtil.isReplaceable(blockPos = pos.func_177972_a(facing))) continue;
                        positions.add(pos);
                    }
                }
            }
        }
        if (positions.isEmpty()) {
            return null;
        }
        positions.sort(Comparator.comparingDouble(o -> o.func_177957_d((double)targetPos.func_177958_n() + 0.5, (double)targetPos.func_177956_o() + 0.5, (double)targetPos.func_177952_p() + 0.5)));
        BlockPos blockPos = (BlockPos)positions.get(0);
        EnumFacing facing = this.getBestFacing(blockPos, targetPos);
        return facing == null ? null : new BlockData(blockPos, facing);
    }

    private void place(BlockPos blockPos, EnumFacing enumFacing, Vec3 vec3) {
        if (ItemUtil.isHoldingBlock() && this.blockCount > 0 && Scaffold.mc.field_71442_b.func_178890_a(Scaffold.mc.field_71439_g, Scaffold.mc.field_71441_e, Scaffold.mc.field_71439_g.field_71071_by.func_70448_g(), blockPos, enumFacing, vec3)) {
            if (Scaffold.mc.field_71442_b.func_178889_l() != WorldSettings.GameType.CREATIVE) {
                --this.blockCount;
            }
            if (((Boolean)this.swing.getValue()).booleanValue()) {
                Scaffold.mc.field_71439_g.func_71038_i();
            } else {
                PacketUtil.sendPacket(new C0APacketAnimation());
            }
        }
    }

    private EnumFacing yawToFacing(float yaw) {
        if (yaw < -135.0f || yaw > 135.0f) {
            return EnumFacing.NORTH;
        }
        if (yaw < -45.0f) {
            return EnumFacing.EAST;
        }
        return yaw < 45.0f ? EnumFacing.SOUTH : EnumFacing.WEST;
    }

    private double distanceToEdge(EnumFacing enumFacing) {
        switch (enumFacing) {
            case NORTH: {
                return Scaffold.mc.field_71439_g.field_70161_v - Math.floor(Scaffold.mc.field_71439_g.field_70161_v);
            }
            case EAST: {
                return Math.ceil(Scaffold.mc.field_71439_g.field_70165_t) - Scaffold.mc.field_71439_g.field_70165_t;
            }
            case SOUTH: {
                return Math.ceil(Scaffold.mc.field_71439_g.field_70161_v) - Scaffold.mc.field_71439_g.field_70161_v;
            }
        }
        return Scaffold.mc.field_71439_g.field_70165_t - Math.floor(Scaffold.mc.field_71439_g.field_70165_t);
    }

    private float getSpeed() {
        if (!Scaffold.mc.field_71439_g.field_70122_E) {
            return (float)((Integer)this.airMotion.getValue()).intValue() / 100.0f;
        }
        return MoveUtil.getSpeedLevel() > 0 ? (float)((Integer)this.speedMotion.getValue()).intValue() / 100.0f : (float)((Integer)this.groundMotion.getValue()).intValue() / 100.0f;
    }

    private double getRandomOffset() {
        return 0.2155 - RandomUtil.nextDouble(1.0E-4, 9.0E-4);
    }

    private float getCurrentYaw() {
        return MoveUtil.adjustYaw(Scaffold.mc.field_71439_g.field_70177_z, MoveUtil.getForwardValue(), MoveUtil.getLeftValue());
    }

    private boolean isDiagonal(float yaw) {
        float absYaw = Math.abs(yaw % 90.0f);
        return absYaw > 20.0f && absYaw < 70.0f;
    }

    private boolean isTowering() {
        if (Scaffold.mc.field_71439_g.field_70122_E && MoveUtil.isForwardPressed() && !PlayerUtil.isAirAbove()) {
            boolean keepY = (Integer)this.keepY.getValue() == 3;
            boolean tower = (Integer)this.tower.getValue() == 3;
            return keepY && this.stage > 0 || tower && Scaffold.mc.field_71474_y.field_74314_A.func_151470_d();
        }
        return false;
    }

    public Scaffold() {
        super("Scaffold", false);
    }

    public int getSlot() {
        return this.lastSlot;
    }

    @EventTarget(value=1)
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.rotationTick > 0) {
                --this.rotationTick;
            }
            if (Scaffold.mc.field_71439_g.field_70122_E) {
                if (this.stage > 0) {
                    --this.stage;
                }
                if (this.stage < 0) {
                    ++this.stage;
                }
                if (!(this.stage != 0 || (Integer)this.keepY.getValue() == 0 || ((Boolean)this.keepYonPress.getValue()).booleanValue() && !PlayerUtil.isUsingItem() || ((Boolean)this.disableWhileJumpActive.getValue()).booleanValue() && Scaffold.mc.field_71439_g.func_70644_a(Potion.field_76430_j) || Scaffold.mc.field_71474_y.field_74314_A.func_151470_d())) {
                    this.stage = 1;
                }
                this.startY = this.shouldKeepY ? this.startY : MathHelper.func_76128_c((double)Scaffold.mc.field_71439_g.field_70163_u);
                this.shouldKeepY = false;
                this.towering = false;
            }
            if (this.canPlace()) {
                int nextBlockY;
                float diagonalYaw;
                ItemStack stack;
                BlockPos currentBelow;
                double nextZ;
                double nextX;
                BlockPos nextBelow;
                double currentSpeed;
                if ((Integer)this.rotationMode.getValue() == 1 && Scaffold.mc.field_71439_g.field_70122_E && (currentSpeed = MoveUtil.getSpeed()) > 0.01 && BlockUtil.isReplaceable(nextBelow = new BlockPos(nextX = Scaffold.mc.field_71439_g.field_70165_t + Scaffold.mc.field_71439_g.field_70159_w, Scaffold.mc.field_71439_g.field_70163_u - 0.01, nextZ = Scaffold.mc.field_71439_g.field_70161_v + Scaffold.mc.field_71439_g.field_70179_y).func_177977_b()) && BlockUtil.isReplaceable(nextBelow.func_177984_a()) && !BlockUtil.isReplaceable(currentBelow = new BlockPos((Entity)Scaffold.mc.field_71439_g).func_177977_b())) {
                    this.stopMovementNextTick = true;
                    if (System.currentTimeMillis() - this.lastYCheckLogTime > 2000L) {
                        this.lastYCheckLogTime = System.currentTimeMillis();
                        Scaffold.mc.field_71439_g.func_145747_a((IChatComponent)new ChatComponentText("\u00a7b[Scaffold] \u00a7fY-check triggered: unsafe edge detected."));
                    }
                }
                int count = ItemUtil.isBlock(stack = Scaffold.mc.field_71439_g.func_70694_bm()) ? stack.field_77994_a : 0;
                this.blockCount = Math.min(this.blockCount, count);
                if (this.blockCount <= 0) {
                    int slot = Scaffold.mc.field_71439_g.field_71071_by.field_70461_c;
                    if (this.blockCount == 0) {
                        --slot;
                    }
                    for (int i = slot; i > slot - 9; --i) {
                        int hotbarSlot = (i % 9 + 9) % 9;
                        ItemStack candidate = Scaffold.mc.field_71439_g.field_71071_by.func_70301_a(hotbarSlot);
                        if (!ItemUtil.isBlock(candidate)) continue;
                        Scaffold.mc.field_71439_g.field_71071_by.field_70461_c = hotbarSlot;
                        this.blockCount = candidate.field_77994_a;
                        break;
                    }
                }
                float currentYaw = this.getCurrentYaw();
                float yawDiffTo180 = RotationUtil.wrapAngleDiff(currentYaw - 180.0f, event.getYaw());
                float f = this.isDiagonal(currentYaw) ? yawDiffTo180 : (diagonalYaw = RotationUtil.wrapAngleDiff(currentYaw - 135.0f * ((currentYaw + 180.0f) % 90.0f < 45.0f ? 1.0f : -1.0f), event.getYaw()));
                if (!this.canRotate) {
                    switch ((Integer)this.rotationMode.getValue()) {
                        case 1: {
                            if (this.yaw == -180.0f && this.pitch == 0.0f) {
                                this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                                this.pitch = RotationUtil.quantizeAngle(85.0f);
                                break;
                            }
                            this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                            break;
                        }
                        case 2: {
                            if (this.yaw == -180.0f && this.pitch == 0.0f) {
                                this.yaw = RotationUtil.quantizeAngle(yawDiffTo180);
                                this.pitch = RotationUtil.quantizeAngle(85.0f);
                                break;
                            }
                            this.yaw = RotationUtil.quantizeAngle(yawDiffTo180);
                            break;
                        }
                        case 3: {
                            if (this.yaw == -180.0f && this.pitch == 0.0f) {
                                this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                                this.pitch = RotationUtil.quantizeAngle(85.0f);
                                break;
                            }
                            this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                        }
                    }
                }
                BlockData blockData = this.getBlockData();
                Vec3 hitVec = null;
                if (blockData != null) {
                    double[] x = placeOffsets;
                    double[] y = placeOffsets;
                    double[] z = placeOffsets;
                    switch (blockData.facing()) {
                        case NORTH: {
                            z = new double[]{0.0};
                            break;
                        }
                        case EAST: {
                            x = new double[]{1.0};
                            break;
                        }
                        case SOUTH: {
                            z = new double[]{1.0};
                            break;
                        }
                        case WEST: {
                            x = new double[]{0.0};
                            break;
                        }
                        case DOWN: {
                            y = new double[]{0.0};
                            break;
                        }
                        case UP: {
                            y = new double[]{1.0};
                        }
                    }
                    float bestYaw = -180.0f;
                    float bestPitch = 0.0f;
                    float bestDiff = 0.0f;
                    for (double dx : x) {
                        for (double dy : y) {
                            for (double dz : z) {
                                float baseYaw;
                                double relZ;
                                double relY;
                                double relX = (double)blockData.blockPos().func_177958_n() + dx - Scaffold.mc.field_71439_g.field_70165_t;
                                float[] rotations = RotationUtil.getRotationsTo(relX, relY = (double)blockData.blockPos().func_177956_o() + dy - Scaffold.mc.field_71439_g.field_70163_u - (double)Scaffold.mc.field_71439_g.func_70047_e(), relZ = (double)blockData.blockPos().func_177952_p() + dz - Scaffold.mc.field_71439_g.field_70161_v, baseYaw = RotationUtil.wrapAngleDiff(this.yaw, event.getYaw()), this.pitch);
                                MovingObjectPosition mop = RotationUtil.rayTrace(rotations[0], rotations[1], (double)Scaffold.mc.field_71442_b.func_78757_d(), 1.0f);
                                if (mop == null || mop.field_72313_a != MovingObjectPosition.MovingObjectType.BLOCK || !mop.func_178782_a().equals((Object)blockData.blockPos()) || mop.field_178784_b != blockData.facing()) continue;
                                float totalDiff = Math.abs(rotations[0] - baseYaw) + Math.abs(rotations[1] - this.pitch);
                                if ((bestYaw != -180.0f || bestPitch != 0.0f) && !(totalDiff < bestDiff)) continue;
                                bestYaw = rotations[0];
                                bestPitch = rotations[1];
                                bestDiff = totalDiff;
                                hitVec = mop.field_72307_f;
                            }
                        }
                    }
                    if (bestYaw != -180.0f || bestPitch != 0.0f) {
                        this.yaw = bestYaw;
                        this.pitch = bestPitch;
                        this.canRotate = true;
                        if ((Integer)this.rotationMode.getValue() == 1) {
                            float yawTarget = this.yaw;
                            float pitchTarget = this.pitch;
                            float yawCurrent = event.getYaw();
                            float pitchCurrent = event.getPitch();
                            float yawDiff = Math.abs(MathHelper.func_76142_g((float)(yawTarget - yawCurrent)));
                            float pitchDiff = Math.abs(pitchTarget - pitchCurrent);
                            if (yawDiff < 5.0f && pitchDiff < 5.0f) {
                                this.rotationTick = 0;
                            }
                        }
                    }
                }
                if (this.canRotate && MoveUtil.isForwardPressed() && Math.abs(MathHelper.func_76142_g((float)(yawDiffTo180 - this.yaw))) < 90.0f) {
                    switch ((Integer)this.rotationMode.getValue()) {
                        case 2: {
                            this.yaw = RotationUtil.quantizeAngle(yawDiffTo180);
                            break;
                        }
                        case 3: {
                            this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                        }
                    }
                }
                if ((Integer)this.rotationMode.getValue() != 0) {
                    float targetYaw = this.yaw;
                    float targetPitch = this.pitch;
                    if (this.towering && (Scaffold.mc.field_71439_g.field_70181_x > 0.0 || Scaffold.mc.field_71439_g.field_70163_u > (double)(this.startY + 1))) {
                        float tolerance;
                        float yawDiff = MathHelper.func_76142_g((float)(this.yaw - event.getYaw()));
                        float f2 = tolerance = this.rotationTick >= 2 ? RandomUtil.nextFloat(90.0f, 95.0f) : RandomUtil.nextFloat(30.0f, 35.0f);
                        if (Math.abs(yawDiff) > tolerance) {
                            float clampedYaw = RotationUtil.clampAngle(yawDiff, tolerance);
                            targetYaw = RotationUtil.quantizeAngle(event.getYaw() + clampedYaw);
                            this.rotationTick = Math.max(this.rotationTick, 1);
                        }
                    }
                    if (this.isTowering()) {
                        float yawDelta = MathHelper.func_76142_g((float)(Scaffold.mc.field_71439_g.field_70177_z - event.getYaw()));
                        targetYaw = RotationUtil.quantizeAngle(event.getYaw() + yawDelta * RandomUtil.nextFloat(0.98f, 0.99f));
                        targetPitch = RotationUtil.quantizeAngle(RandomUtil.nextFloat(30.0f, 80.0f));
                        this.rotationTick = 3;
                        this.towering = true;
                    }
                    event.setRotation(targetYaw, targetPitch, 3);
                    if ((Integer)this.moveFix.getValue() == 1) {
                        event.setPervRotation(targetYaw, 3);
                    }
                }
                if (blockData != null && hitVec != null) {
                    boolean rotationReady;
                    boolean bl = (Integer)this.rotationMode.getValue() == 1 ? this.rotationTick <= 1 : (rotationReady = this.rotationTick <= 0);
                    if (rotationReady) {
                        this.place(blockData.blockPos(), blockData.facing(), hitVec);
                        if (((Boolean)this.multiplace.getValue()).booleanValue()) {
                            for (int i = 0; i < 3 && (blockData = this.getBlockData()) != null; ++i) {
                                double dz;
                                MovingObjectPosition mop = RotationUtil.rayTrace(this.yaw, this.pitch, (double)Scaffold.mc.field_71442_b.func_78757_d(), 1.0f);
                                if (mop != null && mop.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK && mop.func_178782_a().equals((Object)blockData.blockPos()) && mop.field_178784_b == blockData.facing()) {
                                    this.place(blockData.blockPos(), blockData.facing(), mop.field_72307_f);
                                    continue;
                                }
                                hitVec = BlockUtil.getClickVec(blockData.blockPos(), blockData.facing());
                                double dx = hitVec.field_72450_a - Scaffold.mc.field_71439_g.field_70165_t;
                                double dy = hitVec.field_72448_b - Scaffold.mc.field_71439_g.field_70163_u - (double)Scaffold.mc.field_71439_g.func_70047_e();
                                float[] rotations = RotationUtil.getRotationsTo(dx, dy, dz = hitVec.field_72449_c - Scaffold.mc.field_71439_g.field_70161_v, event.getYaw(), event.getPitch());
                                if (!(Math.abs(rotations[0] - this.yaw) < 120.0f) || !(Math.abs(rotations[1] - this.pitch) < 60.0f) || (mop = RotationUtil.rayTrace(rotations[0], rotations[1], (double)Scaffold.mc.field_71442_b.func_78757_d(), 1.0f)) == null || mop.field_72313_a != MovingObjectPosition.MovingObjectType.BLOCK || !mop.func_178782_a().equals((Object)blockData.blockPos()) || mop.field_178784_b != blockData.facing()) break;
                                this.place(blockData.blockPos(), blockData.facing(), mop.field_72307_f);
                            }
                        }
                    }
                }
                if (this.targetFacing != null) {
                    if (this.rotationTick <= 0) {
                        int playerBlockX = MathHelper.func_76128_c((double)Scaffold.mc.field_71439_g.field_70165_t);
                        int playerBlockY = MathHelper.func_76128_c((double)Scaffold.mc.field_71439_g.field_70163_u);
                        int playerBlockZ = MathHelper.func_76128_c((double)Scaffold.mc.field_71439_g.field_70161_v);
                        BlockPos belowPlayer = new BlockPos(playerBlockX, playerBlockY - 1, playerBlockZ);
                        hitVec = BlockUtil.getHitVec(belowPlayer, this.targetFacing, this.yaw, this.pitch);
                        this.place(belowPlayer, this.targetFacing, hitVec);
                    }
                    this.targetFacing = null;
                } else if ((Integer)this.keepY.getValue() == 2 && this.stage > 0 && !Scaffold.mc.field_71439_g.field_70122_E && (nextBlockY = MathHelper.func_76128_c((double)(Scaffold.mc.field_71439_g.field_70163_u + Scaffold.mc.field_71439_g.field_70181_x))) <= this.startY && Scaffold.mc.field_71439_g.field_70163_u > (double)(this.startY + 1)) {
                    this.shouldKeepY = true;
                    blockData = this.getBlockData();
                    if (blockData != null && this.rotationTick <= 0) {
                        hitVec = BlockUtil.getHitVec(blockData.blockPos(), blockData.facing(), this.yaw, this.pitch);
                        this.place(blockData.blockPos(), blockData.facing(), hitVec);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (this.isEnabled()) {
            if (!Scaffold.mc.field_71439_g.field_70123_F && Scaffold.mc.field_71439_g.field_70737_aN <= 5 && !Scaffold.mc.field_71439_g.func_70644_a(Potion.field_76430_j) && Scaffold.mc.field_71474_y.field_74314_A.func_151470_d() && ItemUtil.isHoldingBlock()) {
                int yState = (int)(Scaffold.mc.field_71439_g.field_70163_u % 1.0 * 100.0);
                switch ((Integer)this.tower.getValue()) {
                    case 1: {
                        switch (this.towerTick) {
                            case 0: {
                                if (Scaffold.mc.field_71439_g.field_70122_E) {
                                    this.towerTick = 1;
                                    Scaffold.mc.field_71439_g.field_70181_x = -0.0784000015258789;
                                }
                                return;
                            }
                            case 1: {
                                if (yState == 0 && PlayerUtil.isAirBelow()) {
                                    this.startY = MathHelper.func_76128_c((double)Scaffold.mc.field_71439_g.field_70163_u);
                                    this.towerTick = 2;
                                    Scaffold.mc.field_71439_g.field_70181_x = 0.42f;
                                    if (MoveUtil.isForwardPressed()) {
                                        MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
                                    } else {
                                        MoveUtil.setSpeed(0.0);
                                        event.setForward(0.0f);
                                        event.setStrafe(0.0f);
                                    }
                                    return;
                                }
                                this.towerTick = 0;
                                return;
                            }
                            case 2: {
                                this.towerTick = 3;
                                Scaffold.mc.field_71439_g.field_70181_x = 0.75 - Scaffold.mc.field_71439_g.field_70163_u % 1.0;
                                return;
                            }
                            case 3: {
                                this.towerTick = 1;
                                Scaffold.mc.field_71439_g.field_70181_x = 1.0 - Scaffold.mc.field_71439_g.field_70163_u % 1.0;
                                return;
                            }
                        }
                        this.towerTick = 0;
                        return;
                    }
                    case 2: {
                        switch (this.towerTick) {
                            case 0: {
                                if (Scaffold.mc.field_71439_g.field_70122_E) {
                                    this.towerTick = 1;
                                    Scaffold.mc.field_71439_g.field_70181_x = -0.0784000015258789;
                                }
                                return;
                            }
                            case 1: {
                                if (yState == 0 && PlayerUtil.isAirBelow()) {
                                    this.startY = MathHelper.func_76128_c((double)Scaffold.mc.field_71439_g.field_70163_u);
                                    if (!MoveUtil.isForwardPressed()) {
                                        this.towerDelay = 2;
                                        MoveUtil.setSpeed(0.0);
                                        event.setForward(0.0f);
                                        event.setStrafe(0.0f);
                                        EnumFacing facing = this.yawToFacing(MathHelper.func_76142_g((float)(this.yaw - 180.0f)));
                                        double distance = this.distanceToEdge(facing);
                                        if (distance > 0.1) {
                                            if (Scaffold.mc.field_71439_g.field_70122_E) {
                                                Vec3i directionVec = facing.func_176730_m();
                                                double offset = Math.min(this.getRandomOffset(), distance - 0.05);
                                                double jitter = RandomUtil.nextDouble(0.02, 0.03);
                                                AxisAlignedBB nextBox = Scaffold.mc.field_71439_g.func_174813_aQ().func_72317_d((double)directionVec.func_177958_n() * (offset - jitter), 0.0, (double)directionVec.func_177952_p() * (offset - jitter));
                                                if (Scaffold.mc.field_71441_e.func_72945_a((Entity)Scaffold.mc.field_71439_g, nextBox).isEmpty()) {
                                                    Scaffold.mc.field_71439_g.field_70181_x = -0.0784000015258789;
                                                    Scaffold.mc.field_71439_g.func_70107_b(nextBox.field_72340_a + (nextBox.field_72336_d - nextBox.field_72340_a) / 2.0, nextBox.field_72338_b, nextBox.field_72339_c + (nextBox.field_72334_f - nextBox.field_72339_c) / 2.0);
                                                }
                                                return;
                                            }
                                        } else {
                                            this.towerTick = 2;
                                            this.targetFacing = facing;
                                            Scaffold.mc.field_71439_g.field_70181_x = 0.42f;
                                        }
                                        return;
                                    }
                                    this.towerTick = 2;
                                    ++this.towerDelay;
                                    Scaffold.mc.field_71439_g.field_70181_x = 0.42f;
                                    MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
                                    return;
                                }
                                this.towerTick = 0;
                                this.towerDelay = 0;
                                return;
                            }
                            case 2: {
                                this.towerTick = 3;
                                Scaffold.mc.field_71439_g.field_70181_x -= RandomUtil.nextDouble(0.00101, 0.00109);
                                return;
                            }
                            case 3: {
                                if (this.towerDelay >= 4) {
                                    this.towerTick = 4;
                                    this.towerDelay = 0;
                                } else {
                                    this.towerTick = 1;
                                    Scaffold.mc.field_71439_g.field_70181_x = 1.0 - Scaffold.mc.field_71439_g.field_70163_u % 1.0;
                                }
                                return;
                            }
                            case 4: {
                                this.towerTick = 5;
                                return;
                            }
                            case 5: {
                                if (!PlayerUtil.isAirBelow()) {
                                    this.towerTick = 0;
                                } else {
                                    this.towerTick = 1;
                                    Scaffold.mc.field_71439_g.field_70181_x -= 0.08;
                                    Scaffold.mc.field_71439_g.field_70181_x *= (double)0.98f;
                                    Scaffold.mc.field_71439_g.field_70181_x -= 0.08;
                                    Scaffold.mc.field_71439_g.field_70181_x *= (double)0.98f;
                                }
                                return;
                            }
                        }
                        this.towerTick = 0;
                        this.towerDelay = 0;
                        return;
                    }
                }
                this.towerTick = 0;
                this.towerDelay = 0;
            } else {
                this.towerTick = 0;
                this.towerDelay = 0;
            }
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (this.isEnabled()) {
            if ((Integer)this.moveFix.getValue() == 1 && RotationState.isActived() && RotationState.getPriority() == 3.0f && MoveUtil.isForwardPressed()) {
                MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
            }
            if (Scaffold.mc.field_71439_g.field_70122_E && this.stage > 0 && MoveUtil.isForwardPressed()) {
                Scaffold.mc.field_71439_g.field_71158_b.field_78901_c = true;
            }
            if (this.stopMovementNextTick) {
                Scaffold.mc.field_71439_g.field_71158_b.field_78900_b = 0.0f;
                Scaffold.mc.field_71439_g.field_71158_b.field_78902_a = 0.0f;
                this.stopMovementNextTick = false;
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled()) {
            float speed = this.getSpeed();
            if (speed != 1.0f) {
                if (Scaffold.mc.field_71439_g.field_71158_b.field_78900_b != 0.0f && Scaffold.mc.field_71439_g.field_71158_b.field_78902_a != 0.0f) {
                    Scaffold.mc.field_71439_g.field_71158_b.field_78900_b *= 1.0f / (float)Math.sqrt(2.0);
                    Scaffold.mc.field_71439_g.field_71158_b.field_78902_a *= 1.0f / (float)Math.sqrt(2.0);
                }
                Scaffold.mc.field_71439_g.field_71158_b.field_78900_b *= speed;
                Scaffold.mc.field_71439_g.field_71158_b.field_78902_a *= speed;
            }
            if (this.shouldStopSprint()) {
                Scaffold.mc.field_71439_g.func_70031_b(false);
            }
        }
    }

    @EventTarget
    public void onSafeWalk(SafeWalkEvent event) {
        if (this.isEnabled() && ((Boolean)this.safeWalk.getValue()).booleanValue() && Scaffold.mc.field_71439_g.field_70122_E && Scaffold.mc.field_71439_g.field_70181_x <= 0.0 && PlayerUtil.canMove(Scaffold.mc.field_71439_g.field_70159_w, Scaffold.mc.field_71439_g.field_70179_y, -1.0)) {
            event.setSafeWalk(true);
        }
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (this.isEnabled() && ((Boolean)this.blockCounter.getValue()).booleanValue()) {
            int count = 0;
            for (int i = 0; i < 9; ++i) {
                Block block;
                Item item;
                ItemStack stack = Scaffold.mc.field_71439_g.field_71071_by.func_70301_a(i);
                if (stack == null || stack.field_77994_a <= 0 || !((item = stack.func_77973_b()) instanceof ItemBlock) || BlockUtil.isInteractable(block = ((ItemBlock)item).func_179223_d()) || !BlockUtil.isSolid(block)) continue;
                count += stack.field_77994_a;
            }
            HUD hud = (HUD)Myau.moduleManager.modules.get(HUD.class);
            float scale = ((Float)hud.scale.getValue()).floatValue();
            GlStateManager.func_179094_E();
            GlStateManager.func_179152_a((float)scale, (float)scale, (float)0.0f);
            GlStateManager.func_179097_i();
            GlStateManager.func_179147_l();
            GlStateManager.func_179112_b((int)770, (int)771);
            Scaffold.mc.field_71466_p.func_175065_a(String.format("%d block%s left", count, count != 1 ? "s" : ""), ((float)new ScaledResolution(mc).func_78326_a() / 2.0f + (float)Scaffold.mc.field_71466_p.field_78288_b * 1.5f) / scale, (float)new ScaledResolution(mc).func_78328_b() / 2.0f / scale - (float)Scaffold.mc.field_71466_p.field_78288_b / 2.0f + 1.0f, (count > 0 ? Color.WHITE.getRGB() : new Color(255, 85, 85).getRGB()) | 0xBF000000, ((Boolean)hud.shadow.getValue()).booleanValue());
            GlStateManager.func_179084_k();
            GlStateManager.func_179126_j();
            GlStateManager.func_179121_F();
        }
    }

    @EventTarget
    public void onLeftClick(LeftClickMouseEvent event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onHitBlock(HitBlockEvent event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onSwap(SwapItemEvent event) {
        if (this.isEnabled()) {
            this.lastSlot = event.setSlot(this.lastSlot);
            event.setCancelled(true);
        }
    }

    @Override
    public void onEnabled() {
        this.lastSlot = Scaffold.mc.field_71439_g != null ? Scaffold.mc.field_71439_g.field_71071_by.field_70461_c : -1;
        this.blockCount = -1;
        this.rotationTick = 3;
        this.yaw = -180.0f;
        this.pitch = 0.0f;
        this.canRotate = false;
        this.towerTick = 0;
        this.towerDelay = 0;
        this.towering = false;
    }

    @Override
    public void onDisabled() {
        if (Scaffold.mc.field_71439_g != null && this.lastSlot != -1) {
            Scaffold.mc.field_71439_g.field_71071_by.field_70461_c = this.lastSlot;
        }
    }

    public static class BlockData {
        private final BlockPos blockPos;
        private final EnumFacing facing;

        public BlockData(BlockPos blockPos, EnumFacing enumFacing) {
            this.blockPos = blockPos;
            this.facing = enumFacing;
        }

        public BlockPos blockPos() {
            return this.blockPos;
        }

        public EnumFacing facing() {
            return this.facing;
        }
    }
}

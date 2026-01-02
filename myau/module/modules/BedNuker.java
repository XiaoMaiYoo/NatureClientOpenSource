package myau.module.modules;

import com.google.common.base.CaseFormat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import myau.Myau;
import myau.enums.ChatColors;
import myau.enums.DelayModules;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.HitBlockEvent;
import myau.events.KnockbackEvent;
import myau.events.LeftClickMouseEvent;
import myau.events.LoadWorldEvent;
import myau.events.MoveInputEvent;
import myau.events.PacketEvent;
import myau.events.PlayerUpdateEvent;
import myau.events.Render2DEvent;
import myau.events.Render3DEvent;
import myau.events.RightClickMouseEvent;
import myau.events.SwapItemEvent;
import myau.events.TickEvent;
import myau.events.UpdateEvent;
import myau.management.RotationState;
import myau.mixin.IAccessorPlayerControllerMP;
import myau.module.Module;
import myau.module.modules.BedESP;
import myau.module.modules.HUD;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.BlockUtil;
import myau.util.ColorUtil;
import myau.util.ItemUtil;
import myau.util.MoveUtil;
import myau.util.PacketUtil;
import myau.util.PlayerUtil;
import myau.util.RenderUtil;
import myau.util.RotationUtil;
import myau.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BedNuker
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final TimerUtil timer = new TimerUtil();
    private final ArrayList<BlockPos> bedWhitelist = new ArrayList();
    private final Color colorRed = new Color(ChatColors.RED.toAwtColor());
    private final Color colorYellow = new Color(ChatColors.YELLOW.toAwtColor());
    private final Color colorGreen = new Color(ChatColors.GREEN.toAwtColor());
    private BlockPos targetBed = null;
    private int breakStage = 0;
    private int tickCounter = 0;
    private float breakProgress = 0.0f;
    private boolean isBed = false;
    private int savedSlot = -1;
    private boolean readyToBreak = false;
    private boolean breaking = false;
    private boolean waitingForStart = false;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"LEGIT", "SWAP", "Hypixel"});
    public final FloatProperty range = new FloatProperty("range", Float.valueOf(4.5f), Float.valueOf(3.0f), Float.valueOf(6.0f));
    public final PercentProperty speed = new PercentProperty("speed", 0);
    public final BooleanProperty groundSpeed = new BooleanProperty("ground-spoof", false);
    public final ModeProperty ignoreVelocity = new ModeProperty("ignore-velocity", 0, new String[]{"NONE", "CANCEL", "DELAY"});
    public final BooleanProperty surroundings = new BooleanProperty("surroundings", true);
    public final BooleanProperty toolCheck = new BooleanProperty("tool-check", true);
    public final BooleanProperty whiteList = new BooleanProperty("whitelist", true);
    public final BooleanProperty swing = new BooleanProperty("swing", true);
    public final ModeProperty moveFix = new ModeProperty("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
    public final ModeProperty showTarget = new ModeProperty("show-target", 1, new String[]{"NONE", "DEFAULT", "HUD"});
    public final ModeProperty showProgress = new ModeProperty("show-progress", 1, new String[]{"NONE", "DEFAULT", "HUD"});

    private boolean isHypixelMode() {
        return (Integer)this.mode.getValue() == 2;
    }

    private void resetBreaking() {
        if (this.targetBed != null) {
            BedNuker.mc.field_71441_e.func_175715_c(BedNuker.mc.field_71439_g.func_145782_y(), this.targetBed, -1);
        }
        this.targetBed = null;
        this.breakStage = 0;
        this.tickCounter = 0;
        this.breakProgress = 0.0f;
        this.isBed = false;
        this.readyToBreak = false;
        this.breaking = false;
    }

    private float calcProgress() {
        if (this.targetBed == null) {
            return 0.0f;
        }
        float progress = this.breakProgress;
        if (((Boolean)this.groundSpeed.getValue()).booleanValue()) {
            int slot = ItemUtil.findInventorySlot(BedNuker.mc.field_71439_g.field_71071_by.field_70461_c, BedNuker.mc.field_71441_e.func_180495_p(this.targetBed).func_177230_c());
            progress = (float)this.tickCounter * this.getBreakDelta(BedNuker.mc.field_71441_e.func_180495_p(this.targetBed), this.targetBed, slot, true);
        }
        return Math.min(1.0f, progress / (1.0f - 0.3f * ((float)((Integer)this.speed.getValue()).intValue() / 100.0f)));
    }

    private void restoreSlot() {
        if (this.savedSlot != -1) {
            BedNuker.mc.field_71439_g.field_71071_by.field_70461_c = this.savedSlot;
            this.syncHeldItem();
            this.savedSlot = -1;
        }
    }

    private void syncHeldItem() {
        int currentPlayerItem = ((IAccessorPlayerControllerMP)BedNuker.mc.field_71442_b).getCurrentPlayerItem();
        if (BedNuker.mc.field_71439_g.field_71071_by.field_70461_c != currentPlayerItem) {
            BedNuker.mc.field_71439_g.func_71034_by();
        }
        ((IAccessorPlayerControllerMP)BedNuker.mc.field_71442_b).callSyncCurrentPlayItem();
    }

    private boolean hasProperTool(Block block) {
        Material material = block.func_149688_o();
        if (material != Material.field_151573_f && material != Material.field_151574_g && material != Material.field_151576_e) {
            return true;
        }
        for (int i = 0; i < 9; ++i) {
            Item item;
            ItemStack stack = BedNuker.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (stack == null || !((item = stack.func_77973_b()) instanceof ItemPickaxe)) continue;
            return true;
        }
        return false;
    }

    private EnumFacing getHitFacing(BlockPos blockPos) {
        double z;
        double y;
        double x = (double)blockPos.func_177958_n() + 0.5 - BedNuker.mc.field_71439_g.field_70165_t;
        float[] rotations = RotationUtil.getRotationsTo(x, y = (double)blockPos.func_177956_o() + 0.25 - BedNuker.mc.field_71439_g.field_70163_u - (double)BedNuker.mc.field_71439_g.func_70047_e(), z = (double)blockPos.func_177952_p() + 0.5 - BedNuker.mc.field_71439_g.field_70161_v, BedNuker.mc.field_71439_g.field_70177_z, BedNuker.mc.field_71439_g.field_70125_A);
        MovingObjectPosition mop = RotationUtil.rayTrace(rotations[0], rotations[1], 8.0, 1.0f);
        return mop == null ? EnumFacing.UP : mop.field_178784_b;
    }

    private float getDigSpeed(IBlockState iBlockState, int slot, boolean boolean5) {
        int enchantmentLevel;
        float digSpeed;
        ItemStack item = BedNuker.mc.field_71439_g.field_71071_by.func_70301_a(slot);
        float f = digSpeed = item == null ? 1.0f : item.func_77973_b().getDigSpeed(item, iBlockState);
        if (digSpeed > 1.0f && (enchantmentLevel = EnchantmentHelper.func_77506_a((int)Enchantment.field_77349_p.field_77352_x, (ItemStack)item)) > 0) {
            digSpeed += (float)(enchantmentLevel * enchantmentLevel + 1);
        }
        if (BedNuker.mc.field_71439_g.func_70644_a(Potion.field_76422_e)) {
            digSpeed *= 1.0f + (float)(BedNuker.mc.field_71439_g.func_70660_b(Potion.field_76422_e).func_76458_c() + 1) * 0.2f;
        }
        if (BedNuker.mc.field_71439_g.func_70644_a(Potion.field_76419_f)) {
            switch (BedNuker.mc.field_71439_g.func_70660_b(Potion.field_76419_f).func_76458_c()) {
                case 0: {
                    digSpeed *= 0.3f;
                    break;
                }
                case 1: {
                    digSpeed *= 0.09f;
                    break;
                }
                case 2: {
                    digSpeed *= 0.0027f;
                    break;
                }
                default: {
                    digSpeed *= 8.1E-4f;
                }
            }
        }
        if (BedNuker.mc.field_71439_g.func_70055_a(Material.field_151586_h) && !EnchantmentHelper.func_77510_g((EntityLivingBase)BedNuker.mc.field_71439_g)) {
            digSpeed /= 5.0f;
        }
        if (!boolean5) {
            digSpeed /= 5.0f;
        }
        return digSpeed;
    }

    boolean canHarvest(Block block, int slot) {
        if (block.func_149688_o().func_76229_l()) {
            return true;
        }
        ItemStack stack = BedNuker.mc.field_71439_g.field_71071_by.func_70301_a(slot);
        return stack != null && stack.func_150998_b(block);
    }

    private float getBreakDelta(IBlockState iBlockState, BlockPos blockPos, int slot, boolean boolean5) {
        Block block = iBlockState.func_177230_c();
        float hardness = block.func_176195_g((World)BedNuker.mc.field_71441_e, blockPos);
        float boost = this.canHarvest(block, slot) ? 30.0f : 100.0f;
        return hardness < 0.0f ? 0.0f : this.getDigSpeed(iBlockState, slot, boolean5) / hardness / boost;
    }

    private float calcBlockStrength(BlockPos blockPos) {
        IBlockState blockState = BedNuker.mc.field_71441_e.func_180495_p(blockPos);
        int slot = ItemUtil.findInventorySlot(BedNuker.mc.field_71439_g.field_71071_by.field_70461_c, blockState.func_177230_c());
        return this.getBreakDelta(blockState, blockPos, slot, BedNuker.mc.field_71439_g.field_70122_E);
    }

    private BlockPos validateBedPlacement(BlockPos bedPosition) {
        IBlockState blockState = BedNuker.mc.field_71441_e.func_180495_p(bedPosition);
        if (blockState.func_177230_c() instanceof BlockBed) {
            ArrayList<BlockPos> pos = new ArrayList<BlockPos>();
            BlockBed.EnumPartType partType = (BlockBed.EnumPartType)blockState.func_177229_b((IProperty)BlockBed.field_176472_a);
            EnumFacing facing = (EnumFacing)blockState.func_177229_b((IProperty)BlockBed.field_176387_N);
            for (BlockPos blockPos3 : Arrays.asList(bedPosition, bedPosition.func_177972_a(partType == BlockBed.EnumPartType.HEAD ? facing.func_176734_d() : facing))) {
                for (EnumFacing enumFacing : Arrays.asList(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST)) {
                    Block block = BedNuker.mc.field_71441_e.func_180495_p(blockPos3.func_177972_a(enumFacing)).func_177230_c();
                    if (BlockUtil.isReplaceable(block)) {
                        return null;
                    }
                    if (block instanceof BlockBed) continue;
                    pos.add(blockPos3.func_177972_a(enumFacing));
                }
            }
            if (!pos.isEmpty()) {
                pos.sort((blockPos, blockPos2) -> {
                    int o = Float.compare(this.calcBlockStrength((BlockPos)blockPos2), this.calcBlockStrength((BlockPos)blockPos));
                    return o != 0 ? o : Double.compare(blockPos.func_177957_d(BedNuker.mc.field_71439_g.field_70165_t, BedNuker.mc.field_71439_g.field_70163_u + (double)BedNuker.mc.field_71439_g.func_70047_e(), BedNuker.mc.field_71439_g.field_70161_v), blockPos2.func_177957_d(BedNuker.mc.field_71439_g.field_70165_t, BedNuker.mc.field_71439_g.field_70163_u + (double)BedNuker.mc.field_71439_g.func_70047_e(), BedNuker.mc.field_71439_g.field_70161_v));
                });
                return (BlockPos)pos.get(0);
            }
        }
        return null;
    }

    private BlockPos findNearestBed() {
        return this.findTargetBed(BedNuker.mc.field_71439_g.field_70165_t, BedNuker.mc.field_71439_g.field_70163_u + (double)BedNuker.mc.field_71439_g.func_70047_e(), BedNuker.mc.field_71439_g.field_70161_v);
    }

    private BlockPos findTargetBed(double x, double y, double z) {
        ArrayList<BlockPos> targets = new ArrayList<BlockPos>();
        int sX = MathHelper.func_76128_c((double)x);
        int sY = MathHelper.func_76128_c((double)y);
        int sZ = MathHelper.func_76128_c((double)z);
        for (int i = sX - 6; i <= sX + 6; ++i) {
            for (int j = sY - 6; j <= sY + 6; ++j) {
                for (int k = sZ - 6; k <= sZ + 6; ++k) {
                    Block block;
                    BlockPos newPos = new BlockPos(i, j, k);
                    if (((Boolean)this.whiteList.getValue()).booleanValue() && this.bedWhitelist.contains(newPos) || !((block = BedNuker.mc.field_71441_e.func_180495_p(newPos).func_177230_c()) instanceof BlockBed) || !PlayerUtil.isBlockWithinReach(newPos, x, y, z, ((Float)this.range.getValue()).doubleValue())) continue;
                    targets.add(newPos);
                }
            }
        }
        if (targets.isEmpty()) {
            return null;
        }
        targets.sort(Comparator.comparingDouble(blockPos -> blockPos.func_177957_d(BedNuker.mc.field_71439_g.field_70165_t, BedNuker.mc.field_71439_g.field_70163_u + (double)BedNuker.mc.field_71439_g.func_70047_e(), BedNuker.mc.field_71439_g.field_70161_v)));
        for (BlockPos bedPos : targets) {
            BlockPos targetBlock = null;
            if (this.isHypixelMode()) {
                targetBlock = this.validateBedPlacement(bedPos);
                if (targetBlock == null) {
                    targetBlock = bedPos;
                }
            } else {
                if (((Boolean)this.surroundings.getValue()).booleanValue()) {
                    targetBlock = this.validateBedPlacement(bedPos);
                }
                if (targetBlock == null) {
                    targetBlock = bedPos;
                }
            }
            Block block = BedNuker.mc.field_71441_e.func_180495_p(targetBlock).func_177230_c();
            if (((Boolean)this.toolCheck.getValue()).booleanValue() && !this.hasProperTool(block)) continue;
            return targetBlock;
        }
        return null;
    }

    private void doSwing() {
        if (((Boolean)this.swing.getValue()).booleanValue()) {
            BedNuker.mc.field_71439_g.func_71038_i();
        } else {
            PacketUtil.sendPacket(new C0APacketAnimation());
        }
    }

    private Color getProgressColor(int mode) {
        switch (mode) {
            case 1: {
                float progress = this.calcProgress();
                if (progress <= 0.5f) {
                    return ColorUtil.interpolate(progress / 0.5f, this.colorRed, this.colorYellow);
                }
                return ColorUtil.interpolate((progress - 0.5f) / 0.5f, this.colorYellow, this.colorGreen);
            }
            case 2: {
                return ((HUD)Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis());
            }
        }
        return new Color(-1);
    }

    public BedNuker() {
        super("BedNuker", false);
    }

    public boolean isReady() {
        return this.targetBed != null && this.readyToBreak;
    }

    public boolean isBreaking() {
        return this.targetBed != null && this.breaking;
    }

    @EventTarget(value=1)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.targetBed != null) {
                BlockPos nearestBed;
                if (BedNuker.mc.field_71441_e.func_175623_d(this.targetBed) || !PlayerUtil.canReach(this.targetBed, ((Float)this.range.getValue()).doubleValue())) {
                    this.restoreSlot();
                    this.resetBreaking();
                } else if (!this.isBed && (nearestBed = this.findNearestBed()) != null && BedNuker.mc.field_71441_e.func_180495_p(nearestBed).func_177230_c() instanceof BlockBed) {
                    this.resetBreaking();
                }
            }
            if (this.targetBed != null) {
                int slot = ItemUtil.findInventorySlot(BedNuker.mc.field_71439_g.field_71071_by.field_70461_c, BedNuker.mc.field_71441_e.func_180495_p(this.targetBed).func_177230_c());
                if ((Integer)this.mode.getValue() == 0 && this.savedSlot == -1) {
                    this.savedSlot = BedNuker.mc.field_71439_g.field_71071_by.field_70461_c;
                    BedNuker.mc.field_71439_g.field_71071_by.field_70461_c = slot;
                    this.syncHeldItem();
                }
                switch (this.breakStage) {
                    case 0: {
                        if (BedNuker.mc.field_71439_g.func_71039_bw()) break;
                        this.doSwing();
                        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, this.targetBed, this.getHitFacing(this.targetBed)));
                        this.doSwing();
                        BedNuker.mc.field_71452_i.func_180532_a(this.targetBed, this.getHitFacing(this.targetBed));
                        this.breakStage = 1;
                        break;
                    }
                    case 1: {
                        if ((Integer)this.mode.getValue() == 1) {
                            this.readyToBreak = false;
                        }
                        this.breaking = true;
                        ++this.tickCounter;
                        this.breakProgress += this.getBreakDelta(BedNuker.mc.field_71441_e.func_180495_p(this.targetBed), this.targetBed, slot, BedNuker.mc.field_71439_g.field_70122_E);
                        float tick = this.tickCounter;
                        IBlockState blockState = BedNuker.mc.field_71441_e.func_180495_p(this.targetBed);
                        boolean canBreak = BedNuker.mc.field_71439_g.field_70122_E && (Boolean)this.groundSpeed.getValue() != false;
                        BlockPos target = this.targetBed;
                        float delta = tick * this.getBreakDelta(blockState, target, slot, canBreak);
                        BedNuker.mc.field_71452_i.func_180532_a(this.targetBed, this.getHitFacing(this.targetBed));
                        if (!(this.breakProgress >= 1.0f - 0.3f * ((float)((Integer)this.speed.getValue()).intValue() / 100.0f)) && !(delta >= 1.0f - 0.3f * ((float)((Integer)this.speed.getValue()).intValue() / 100.0f))) break;
                        if ((Integer)this.mode.getValue() == 1) {
                            this.readyToBreak = true;
                            this.savedSlot = BedNuker.mc.field_71439_g.field_71071_by.field_70461_c;
                            BedNuker.mc.field_71439_g.field_71071_by.field_70461_c = slot;
                            this.syncHeldItem();
                            if (BedNuker.mc.field_71439_g.func_71039_bw()) {
                                this.savedSlot = BedNuker.mc.field_71439_g.field_71071_by.field_70461_c;
                                BedNuker.mc.field_71439_g.field_71071_by.field_70461_c = (BedNuker.mc.field_71439_g.field_71071_by.field_70461_c + 1) % 9;
                                this.syncHeldItem();
                            }
                        }
                        this.breaking = false;
                        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.targetBed, this.getHitFacing(this.targetBed)));
                        this.doSwing();
                        IBlockState blockState_ = BedNuker.mc.field_71441_e.func_180495_p(this.targetBed);
                        Block block = blockState_.func_177230_c();
                        if (block.func_149688_o() != Material.field_151579_a) {
                            BedNuker.mc.field_71441_e.func_175718_b(2001, this.targetBed, Block.func_176210_f((IBlockState)blockState_));
                            BedNuker.mc.field_71441_e.func_175698_g(this.targetBed);
                        }
                        if (block instanceof BlockBed) {
                            this.timer.reset();
                        }
                        this.breakStage = 2;
                        break;
                    }
                    case 2: {
                        this.restoreSlot();
                        this.resetBreaking();
                    }
                }
                if (this.targetBed != null) {
                    return;
                }
            }
            if (BedNuker.mc.field_71439_g.field_71075_bZ.field_75099_e && this.timer.hasTimeElapsed(500L)) {
                this.targetBed = this.findNearestBed();
                this.breakStage = 0;
                this.tickCounter = 0;
                this.breakProgress = 0.0f;
                this.isBed = this.targetBed != null && BedNuker.mc.field_71441_e.func_180495_p(this.targetBed).func_177230_c() instanceof BlockBed;
                this.restoreSlot();
                if (this.targetBed != null) {
                    this.readyToBreak = true;
                }
            }
            if (this.targetBed == null) {
                Myau.delayManager.setDelayState(false, DelayModules.BED_NUKER);
            }
        }
    }

    @EventTarget(value=4)
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE && this.isReady()) {
            double x = (double)this.targetBed.func_177958_n() + 0.5 - BedNuker.mc.field_71439_g.field_70165_t;
            double y = (double)this.targetBed.func_177956_o() + 0.5 - BedNuker.mc.field_71439_g.field_70163_u - (double)BedNuker.mc.field_71439_g.func_70047_e();
            double z = (double)this.targetBed.func_177952_p() + 0.5 - BedNuker.mc.field_71439_g.field_70161_v;
            float[] rotations = RotationUtil.getRotationsTo(x, y, z, event.getYaw(), event.getPitch());
            event.setRotation(rotations[0], rotations[1], 5);
            event.setPervRotation((Integer)this.moveFix.getValue() != 0 ? rotations[0] : BedNuker.mc.field_71439_g.field_70177_z, 5);
        }
    }

    @EventTarget
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.isEnabled() && this.isBreaking() && !Myau.playerStateManager.attacking && !Myau.playerStateManager.digging && !Myau.playerStateManager.placing && !Myau.playerStateManager.swinging) {
            this.doSwing();
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (this.isEnabled() && (Integer)this.moveFix.getValue() == 1 && RotationState.isActived() && RotationState.getPriority() == 5.0f && MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }
    }

    @EventTarget(value=1)
    public void onKnockback(KnockbackEvent event) {
        if (this.isEnabled() && !event.isCancelled() && !(event.getY() <= 0.0) && (Integer)this.ignoreVelocity.getValue() == 1 && this.targetBed != null) {
            event.setCancelled(true);
            event.setX(BedNuker.mc.field_71439_g.field_70159_w);
            event.setY(BedNuker.mc.field_71439_g.field_70181_x);
            event.setZ(BedNuker.mc.field_71439_g.field_70179_y);
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!(!this.isEnabled() || this.targetBed == null || this.isBed && ((Boolean)this.surroundings.getValue()).booleanValue() || (Integer)this.showProgress.getValue() == 0)) {
            HUD hud = (HUD)Myau.moduleManager.modules.get(HUD.class);
            float scale = ((Float)hud.scale.getValue()).floatValue();
            String text = String.format("%d%%", (int)(this.calcProgress() * 100.0f));
            GlStateManager.func_179094_E();
            GlStateManager.func_179152_a((float)scale, (float)scale, (float)0.0f);
            GlStateManager.func_179097_i();
            GlStateManager.func_179147_l();
            GlStateManager.func_179112_b((int)770, (int)771);
            int width = BedNuker.mc.field_71466_p.func_78256_a(text);
            BedNuker.mc.field_71466_p.func_175065_a(text, (float)new ScaledResolution(mc).func_78326_a() / 2.0f / scale - (float)width / 2.0f, (float)new ScaledResolution(mc).func_78328_b() / 5.0f * 2.0f / scale, this.getProgressColor((Integer)this.showProgress.getValue()).getRGB() & 0xFFFFFF | 0xBF000000, ((Boolean)hud.shadow.getValue()).booleanValue());
            GlStateManager.func_179084_k();
            GlStateManager.func_179126_j();
            GlStateManager.func_179121_F();
        }
    }

    @EventTarget(value=3)
    public void onRender3D(Render3DEvent event) {
        if (this.isEnabled() && this.targetBed != null && !BedNuker.mc.field_71441_e.func_175623_d(this.targetBed)) {
            BedNuker.mc.field_71441_e.func_175715_c(BedNuker.mc.field_71439_g.func_145782_y(), this.targetBed, (int)(this.calcProgress() * 10.0f) - 1);
            if ((Integer)this.showTarget.getValue() != 0) {
                BedESP bedESP = (BedESP)Myau.moduleManager.modules.get(BedESP.class);
                Color color = this.getProgressColor((Integer)this.showTarget.getValue());
                RenderUtil.enableRenderState();
                BlockPos target = this.targetBed;
                double newHeight = this.isBed ? bedESP.getHeight() : 1.0;
                int r = color.getRed();
                int g = color.getBlue();
                int b = color.getGreen();
                RenderUtil.drawBlockBox(target, newHeight, r, b, g);
                RenderUtil.disableRenderState();
            }
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.waitingForStart = false;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!event.isCancelled()) {
            String text;
            if (event.getPacket() instanceof S02PacketChat && ((text = ((S02PacketChat)event.getPacket()).func_148915_c().func_150254_d()).contains("\u00a7e\u00a7lProtect your bed and destroy the enemy bed") || text.contains("\u00a7e\u00a7lDestroy the enemy bed and then eliminate them"))) {
                this.waitingForStart = true;
            }
            if (event.getPacket() instanceof S08PacketPlayerPosLook && this.waitingForStart) {
                this.waitingForStart = false;
                this.bedWhitelist.clear();
                this.scheduler.schedule(() -> {
                    int sX = MathHelper.func_76128_c((double)BedNuker.mc.field_71439_g.field_70165_t);
                    int sY = MathHelper.func_76128_c((double)(BedNuker.mc.field_71439_g.field_70163_u + (double)BedNuker.mc.field_71439_g.func_70047_e()));
                    int sZ = MathHelper.func_76128_c((double)BedNuker.mc.field_71439_g.field_70161_v);
                    for (int i = sX - 25; i <= sX + 25; ++i) {
                        for (int j = sY - 25; j <= sY + 25; ++j) {
                            for (int k = sZ - 25; k <= sZ + 25; ++k) {
                                BlockPos blockPos = new BlockPos(i, j, k);
                                Block block = BedNuker.mc.field_71441_e.func_180495_p(blockPos).func_177230_c();
                                if (!(block instanceof BlockBed)) continue;
                                this.bedWhitelist.add(blockPos);
                            }
                        }
                    }
                }, 1L, TimeUnit.SECONDS);
            }
            if (this.isEnabled() && this.targetBed != null && (Integer)this.ignoreVelocity.getValue() == 2 && Myau.delayManager.getDelayModule() != DelayModules.BED_NUKER) {
                S27PacketExplosion explosion;
                S12PacketEntityVelocity packet;
                if (event.getPacket() instanceof S12PacketEntityVelocity && (packet = (S12PacketEntityVelocity)event.getPacket()).func_149412_c() == BedNuker.mc.field_71439_g.func_145782_y() && packet.func_149410_e() > 0) {
                    Myau.delayManager.delay(DelayModules.BED_NUKER);
                    Myau.delayManager.delayedPacket.offer((Packet<INetHandlerPlayClient>)packet);
                    event.setCancelled(true);
                }
                if (event.getPacket() instanceof S27PacketExplosion && ((explosion = (S27PacketExplosion)event.getPacket()).func_149149_c() != 0.0f || explosion.func_149144_d() != 0.0f || explosion.func_149147_e() != 0.0f)) {
                    Myau.delayManager.delay(DelayModules.BED_NUKER);
                    Myau.delayManager.delayedPacket.offer((Packet<INetHandlerPlayClient>)explosion);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventTarget
    public void onLeftClick(LeftClickMouseEvent event) {
        if (this.isEnabled() && (this.isReady() || this.targetBed != null && BedNuker.mc.field_71476_x != null && BedNuker.mc.field_71476_x.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK)) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isEnabled() && this.isReady()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onHitBlock(HitBlockEvent event) {
        if (this.isEnabled() && (this.isReady() || this.targetBed != null && BedNuker.mc.field_71476_x != null && BedNuker.mc.field_71476_x.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK)) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onSwap(SwapItemEvent event) {
        if (this.isEnabled() && this.savedSlot != -1) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onDisabled() {
        this.resetBreaking();
        this.savedSlot = -1;
        Myau.delayManager.setDelayState(false, DelayModules.BED_NUKER);
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}

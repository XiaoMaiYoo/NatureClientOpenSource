package myau.module.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.MoveInputEvent;
import myau.events.Render2DEvent;
import myau.events.SwapItemEvent;
import myau.events.TickEvent;
import myau.events.UpdateEvent;
import myau.management.RotationState;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class Block
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final Map<String, Integer> BLOCK_SCORE = new HashMap<String, Integer>();
    private long lastPlaceTime = 0L;
    public final FloatProperty range = new FloatProperty("range", Float.valueOf(4.5f), Float.valueOf(3.0f), Float.valueOf(6.0f));
    public final IntProperty speed = new IntProperty("speed", 20, 5, 100);
    public final IntProperty placeDelay = new IntProperty("place-delay", 50, 0, 200);
    public final IntProperty rotationTolerance = new IntProperty("rotation-tolerance", 25, 5, 100);
    public final BooleanProperty itemSpoof = new BooleanProperty("item-spoof", true);
    public final BooleanProperty showProgress = new BooleanProperty("show-progress", true);
    public final ModeProperty moveFix = new ModeProperty("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
    private float serverYaw;
    private float serverPitch;
    private float progress;
    private float aimYaw;
    private float aimPitch;
    private BlockPos targetBlock;
    private EnumFacing targetFacing;
    private Vec3 targetHitVec;
    private int lastSlot = -1;
    private static final int[][] DIRS = new int[][]{{1, 0, 0}, {0, 0, 1}, {-1, 0, 0}, {0, 0, -1}};
    private static final double INSET = 0.05;
    private static final double STEP = 0.2;
    private static final double JIT = 0.020000000000000004;
    private static final int[][] PLACEMENT_ORDER = new int[][]{{1, 0, 0}, {0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {1, 1, 0}, {0, 1, 1}, {-1, 1, 0}, {0, 1, -1}, {0, 2, 0}};

    public Block() {
        super("Block", false);
        this.BLOCK_SCORE.put("obsidian", 0);
        this.BLOCK_SCORE.put("end_stone", 1);
        this.BLOCK_SCORE.put("planks", 2);
        this.BLOCK_SCORE.put("log", 2);
        this.BLOCK_SCORE.put("glass", 3);
        this.BLOCK_SCORE.put("stained_glass", 3);
        this.BLOCK_SCORE.put("hardened_clay", 4);
        this.BLOCK_SCORE.put("stained_hardened_clay", 4);
        this.BLOCK_SCORE.put("cloth", 5);
    }

    @Override
    public void onEnabled() {
        if (Block.mc.field_71439_g != null) {
            this.serverYaw = Block.mc.field_71439_g.field_70177_z;
            this.serverPitch = Block.mc.field_71439_g.field_70125_A;
            this.aimYaw = this.serverYaw;
            this.aimPitch = this.serverPitch;
            this.progress = 0.0f;
            this.lastSlot = Block.mc.field_71439_g.field_71071_by.field_70461_c;
            this.targetBlock = null;
            this.targetFacing = null;
            this.targetHitVec = null;
            this.lastPlaceTime = 0L;
        }
    }

    @Override
    public void onDisabled() {
        if (this.lastSlot != -1 && Block.mc.field_71439_g != null && Block.mc.field_71439_g.field_71071_by.field_70461_c != this.lastSlot) {
            Block.mc.field_71439_g.field_71071_by.field_70461_c = this.lastSlot;
        }
        this.progress = 0.0f;
        this.targetBlock = null;
        this.targetFacing = null;
        this.targetHitVec = null;
    }

    @EventTarget(value=1)
    public void onUpdate(UpdateEvent event) {
        ItemStack currentHeld;
        boolean holdingBlock;
        if (!this.isEnabled()) {
            return;
        }
        if (event.getType() != EventType.PRE) {
            return;
        }
        if (Block.mc.field_71439_g == null || Block.mc.field_71441_e == null) {
            return;
        }
        if (Block.mc.field_71462_r != null) {
            return;
        }
        this.serverYaw = event.getYaw();
        this.serverPitch = event.getPitch();
        this.updateProgress();
        int blockSlot = this.findBestBlockSlot();
        if (blockSlot != -1 && Block.mc.field_71439_g.field_71071_by.field_70461_c != blockSlot) {
            Block.mc.field_71439_g.field_71071_by.field_70461_c = blockSlot;
        }
        boolean bl = holdingBlock = (currentHeld = Block.mc.field_71439_g.field_71071_by.func_70448_g()) != null && currentHeld.func_77973_b() instanceof ItemBlock;
        if (!holdingBlock) {
            this.targetBlock = null;
            this.targetFacing = null;
            this.targetHitVec = null;
            return;
        }
        this.findBestPlacement();
        if (this.targetBlock != null && this.targetFacing != null && this.targetHitVec != null) {
            Vec3 eyes = Block.mc.field_71439_g.func_174824_e(1.0f);
            double dx = this.targetHitVec.field_72450_a - eyes.field_72450_a;
            double dy = this.targetHitVec.field_72448_b - eyes.field_72448_b;
            double dz = this.targetHitVec.field_72449_c - eyes.field_72449_c;
            double dist = Math.sqrt(dx * dx + dz * dz);
            float targetYaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
            float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
            targetYaw = MathHelper.func_76142_g((float)targetYaw);
            float yawDiff = MathHelper.func_76142_g((float)(targetYaw - this.serverYaw));
            float pitchDiff = targetPitch - this.serverPitch;
            float maxTurn = ((Integer)this.speed.getValue()).floatValue();
            float yawStep = MathHelper.func_76131_a((float)yawDiff, (float)(-maxTurn), (float)maxTurn);
            float pitchStep = MathHelper.func_76131_a((float)pitchDiff, (float)(-maxTurn), (float)maxTurn);
            this.aimYaw = this.serverYaw + yawStep;
            this.aimPitch = MathHelper.func_76131_a((float)(this.serverPitch + pitchStep), (float)-90.0f, (float)90.0f);
            event.setRotation(this.aimYaw, this.aimPitch, 6);
            event.setPervRotation((Integer)this.moveFix.getValue() != 0 ? this.aimYaw : Block.mc.field_71439_g.field_70177_z, 6);
        }
    }

    @EventTarget
    public void onMove(MoveInputEvent event) {
        if (this.isEnabled() && (Integer)this.moveFix.getValue() == 1 && RotationState.isActived() && RotationState.getPriority() == 6.0f && MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }
    }

    @EventTarget(value=1)
    public void onTick(TickEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getType() != EventType.PRE) {
            return;
        }
        if (Block.mc.field_71439_g == null || Block.mc.field_71441_e == null) {
            return;
        }
        if (Block.mc.field_71462_r != null) {
            return;
        }
        if (this.targetBlock != null && this.targetFacing != null && this.targetHitVec != null) {
            if (!this.withinRotationTolerance(this.aimYaw, this.aimPitch)) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastPlaceTime >= (long)((Integer)this.placeDelay.getValue()).intValue()) {
                ItemStack heldStack;
                this.lastPlaceTime = currentTime;
                MovingObjectPosition mop = this.rayTraceBlock(this.aimYaw, this.aimPitch, ((Float)this.range.getValue()).floatValue());
                if (mop != null && mop.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK && mop.func_178782_a().equals((Object)this.targetBlock) && mop.field_178784_b == this.targetFacing && (heldStack = Block.mc.field_71439_g.field_71071_by.func_70448_g()) != null && heldStack.func_77973_b() instanceof ItemBlock) {
                    Block.mc.field_71442_b.func_178890_a(Block.mc.field_71439_g, Block.mc.field_71441_e, heldStack, this.targetBlock, this.targetFacing, mop.field_72307_f);
                    Block.mc.field_71439_g.func_71038_i();
                    this.targetBlock = null;
                    this.targetFacing = null;
                    this.targetHitVec = null;
                }
            }
        }
    }

    @EventTarget
    public void onSwap(SwapItemEvent event) {
        if (this.isEnabled()) {
            this.lastSlot = event.setSlot(this.lastSlot);
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled() || Block.mc.field_71462_r != null) {
            return;
        }
        if (!((Boolean)this.showProgress.getValue()).booleanValue()) {
            return;
        }
        if (Block.mc.field_71466_p == null) {
            return;
        }
        float scale = 1.0f;
        String text = String.format("Blocking: %.0f%%", Float.valueOf(this.progress * 100.0f));
        GL11.glPushMatrix();
        GL11.glScaled((double)scale, (double)scale, (double)0.0);
        GlStateManager.func_179097_i();
        GlStateManager.func_179147_l();
        GlStateManager.func_179112_b((int)770, (int)771);
        ScaledResolution sr = new ScaledResolution(mc);
        int width = Block.mc.field_71466_p.func_78256_a(text);
        Color color = this.getProgressColor();
        Block.mc.field_71466_p.func_175065_a(text, (float)sr.func_78326_a() / 2.0f / scale - (float)width / 2.0f, (float)sr.func_78328_b() / 5.0f * 2.0f / scale, color.getRGB() & 0xFFFFFF | 0xBF000000, true);
        GlStateManager.func_179084_k();
        GlStateManager.func_179126_j();
        GL11.glPopMatrix();
    }

    private int findBestBlockSlot() {
        int bestSlot = -1;
        int bestScore = Integer.MAX_VALUE;
        for (int slot = 0; slot <= 8; ++slot) {
            net.minecraft.block.Block block;
            String blockName;
            Integer score;
            ItemStack stack = Block.mc.field_71439_g.field_71071_by.func_70301_a(slot);
            if (stack == null || stack.field_77994_a == 0 || !(stack.func_77973_b() instanceof ItemBlock) || (score = this.BLOCK_SCORE.get(blockName = (block = ((ItemBlock)stack.func_77973_b()).func_179223_d()).func_149739_a().replace("tile.", ""))) == null || score >= bestScore) continue;
            bestScore = score;
            bestSlot = slot;
            if (score == 0) break;
        }
        return bestSlot;
    }

    private void findBestPlacement() {
        if (Block.mc.field_71439_g == null) {
            return;
        }
        Vec3 playerPos = Block.mc.field_71439_g.func_174791_d();
        BlockPos feetPos = new BlockPos(playerPos.field_72450_a, playerPos.field_72448_b, playerPos.field_72449_c);
        Vec3 eye = Block.mc.field_71439_g.func_174824_e(1.0f);
        double reach = ((Float)this.range.getValue()).doubleValue();
        for (int[] offset : PLACEMENT_ORDER) {
            BlockPos targetPos = feetPos.func_177982_a(offset[0], offset[1], offset[2]);
            if (!this.isAir(targetPos)) continue;
            ArrayList<BlockPos> supports = new ArrayList<BlockPos>();
            for (EnumFacing face : EnumFacing.values()) {
                BlockPos support = targetPos.func_177972_a(face);
                if (this.isAir(support)) continue;
                supports.add(support);
            }
            supports.sort(Comparator.comparingDouble(sup -> sup.func_177957_d(eye.field_72450_a, eye.field_72448_b, eye.field_72449_c)));
            for (BlockPos support : supports) {
                if (!this.tryPlaceOnBlock(support, eye, reach, targetPos)) continue;
                return;
            }
        }
        this.targetBlock = null;
        this.targetFacing = null;
        this.targetHitVec = null;
    }

    private boolean isAdjacent(BlockPos a, BlockPos b) {
        int dz;
        int dy;
        int dx = Math.abs(a.func_177958_n() - b.func_177958_n());
        return dx + (dy = Math.abs(a.func_177956_o() - b.func_177956_o())) + (dz = Math.abs(a.func_177952_p() - b.func_177952_p())) == 1;
    }

    private boolean tryPlaceOnBlock(BlockPos supportBlock, Vec3 eye, double reach, BlockPos targetPos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos placementPos = supportBlock.func_177972_a(facing);
            if (!placementPos.equals((Object)targetPos)) continue;
            int n = (int)Math.round(5.0);
            for (int r = 0; r <= n; ++r) {
                double v = (double)r * 0.2 + (Math.random() * 0.020000000000000004 * 2.0 - 0.020000000000000004);
                if (v < 0.0) {
                    v = 0.0;
                } else if (v > 1.0) {
                    v = 1.0;
                }
                for (int c = 0; c <= n; ++c) {
                    double u = (double)c * 0.2 + (Math.random() * 0.020000000000000004 * 2.0 - 0.020000000000000004);
                    if (u < 0.0) {
                        u = 0.0;
                    } else if (u > 1.0) {
                        u = 1.0;
                    }
                    Vec3 hitPos = this.getHitPosOnFace(supportBlock, facing, u, v);
                    float[] rot = this.getRotationsWrapped(eye, hitPos.field_72450_a, hitPos.field_72448_b, hitPos.field_72449_c);
                    MovingObjectPosition mop = this.rayTraceBlock(rot[0], rot[1], reach);
                    if (mop == null || mop.field_72313_a != MovingObjectPosition.MovingObjectType.BLOCK || !mop.func_178782_a().equals((Object)supportBlock) || mop.field_178784_b != facing) continue;
                    this.targetBlock = supportBlock;
                    this.targetFacing = facing;
                    this.targetHitVec = mop.field_72307_f;
                    this.aimYaw = rot[0];
                    this.aimPitch = rot[1];
                    return true;
                }
            }
        }
        return false;
    }

    private Vec3 getHitPosOnFace(BlockPos block, EnumFacing face, double u, double v) {
        double x = (double)block.func_177958_n() + 0.5;
        double y = (double)block.func_177956_o() + 0.5;
        double z = (double)block.func_177952_p() + 0.5;
        switch (face) {
            case DOWN: {
                y = (double)block.func_177956_o() + 0.05;
                x = (double)block.func_177958_n() + u;
                z = (double)block.func_177952_p() + v;
                break;
            }
            case UP: {
                y = (double)block.func_177956_o() + 1.0 - 0.05;
                x = (double)block.func_177958_n() + u;
                z = (double)block.func_177952_p() + v;
                break;
            }
            case NORTH: {
                z = (double)block.func_177952_p() + 0.05;
                x = (double)block.func_177958_n() + u;
                y = (double)block.func_177956_o() + v;
                break;
            }
            case SOUTH: {
                z = (double)block.func_177952_p() + 1.0 - 0.05;
                x = (double)block.func_177958_n() + u;
                y = (double)block.func_177956_o() + v;
                break;
            }
            case WEST: {
                x = (double)block.func_177958_n() + 0.05;
                z = (double)block.func_177952_p() + u;
                y = (double)block.func_177956_o() + v;
                break;
            }
            case EAST: {
                x = (double)block.func_177958_n() + 1.0 - 0.05;
                z = (double)block.func_177952_p() + u;
                y = (double)block.func_177956_o() + v;
            }
        }
        return new Vec3(x, y, z);
    }

    private boolean isAir(BlockPos pos) {
        net.minecraft.block.Block block = Block.mc.field_71441_e.func_180495_p(pos).func_177230_c();
        return block == Blocks.field_150350_a || block == Blocks.field_150355_j || block == Blocks.field_150358_i || block == Blocks.field_150353_l || block == Blocks.field_150356_k || block == Blocks.field_150480_ab;
    }

    private void updateProgress() {
        Vec3 playerPos = Block.mc.field_71439_g.func_174791_d();
        BlockPos feetPos = new BlockPos(playerPos.field_72450_a, playerPos.field_72448_b, playerPos.field_72449_c);
        int filled = 0;
        int total = 9;
        if (!this.isAir(feetPos.func_177981_b(2))) {
            ++filled;
        }
        for (int[] d : DIRS) {
            if (!this.isAir(feetPos.func_177982_a(d[0], 0, d[2]))) {
                ++filled;
            }
            if (this.isAir(feetPos.func_177982_a(d[0], 1, d[2]))) continue;
            ++filled;
        }
        this.progress = (float)filled / (float)total;
    }

    private Color getProgressColor() {
        if (this.progress <= 0.33f) {
            return new Color(255, 85, 85);
        }
        if (this.progress <= 0.66f) {
            return new Color(255, 255, 85);
        }
        return new Color(85, 255, 85);
    }

    private MovingObjectPosition rayTraceBlock(float yaw, float pitch, double range) {
        float yawRad = (float)Math.toRadians(yaw);
        float pitchRad = (float)Math.toRadians(pitch);
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        Vec3 start = Block.mc.field_71439_g.func_174824_e(1.0f);
        Vec3 end = start.func_72441_c(x * range, y * range, z * range);
        return Block.mc.field_71441_e.func_72933_a(start, end);
    }

    private boolean withinRotationTolerance(float targetYaw, float targetPitch) {
        float dy = Math.abs(MathHelper.func_76142_g((float)(targetYaw - this.serverYaw)));
        float dp = Math.abs(MathHelper.func_76142_g((float)(targetPitch - this.serverPitch)));
        return dy <= (float)((Integer)this.rotationTolerance.getValue()).intValue() && dp <= (float)((Integer)this.rotationTolerance.getValue()).intValue();
    }

    private double dist2PointAABB(Vec3 p, int x, int y, int z) {
        double minX = x;
        double maxX = x + 1;
        double minY = y;
        double maxY = y + 1;
        double minZ = z;
        double maxZ = z + 1;
        double cx = this.clamp(p.field_72450_a, minX, maxX);
        double cy = this.clamp(p.field_72448_b, minY, maxY);
        double cz = this.clamp(p.field_72449_c, minZ, maxZ);
        double dx = p.field_72450_a - cx;
        double dy = p.field_72448_b - cy;
        double dz = p.field_72449_c - cz;
        return dx * dx + dy * dy + dz * dz;
    }

    private double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private float[] getRotationsWrapped(Vec3 eye, double tx, double ty, double tz) {
        double dx = tx - eye.field_72450_a;
        double dy = ty - eye.field_72448_b;
        double dz = tz - eye.field_72449_c;
        double hd = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        if ((yaw = (yaw % 360.0f + 360.0f) % 360.0f) > 180.0f) {
            yaw -= 360.0f;
        }
        float pitch = (float)Math.toDegrees(-Math.atan2(dy, hd));
        return new float[]{yaw, pitch};
    }

    public int getSlot() {
        return this.lastSlot;
    }

    private static class BlockData {
        BlockPos pos;
        double distance;

        BlockData(BlockPos pos, double distance) {
            this.pos = pos;
            this.distance = distance;
        }
    }
}

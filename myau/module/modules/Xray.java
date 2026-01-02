package myau.module.modules;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.CopyOnWriteArraySet;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.LoadWorldEvent;
import myau.events.PacketEvent;
import myau.events.Render3DEvent;
import myau.mixin.IAccessorMinecraft;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraftforge.common.ForgeModContainer;

public class Xray
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final LinkedHashSet<Integer> xrayBlocks = new LinkedHashSet<Integer>(Arrays.asList(56, 14, 15, 16, 73, 74, 21, 129, 52, 83, 115));
    private static final LinkedHashSet<Vec3i> caveOffsetsSmall = new LinkedHashSet<Vec3i>(Arrays.asList(new Vec3i(0, -1, 0), new Vec3i(1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(0, 0, 1), new Vec3i(-1, 0, 0), new Vec3i(0, 1, 0)));
    private static final LinkedHashSet<Vec3i> caveOffsetsLarge = new LinkedHashSet<Vec3i>(Arrays.asList(new Vec3i(0, -2, 0), new Vec3i(1, -1, 0), new Vec3i(0, -1, -1), new Vec3i(0, -1, 0), new Vec3i(0, -1, 1), new Vec3i(-1, -1, 0), new Vec3i(2, 0, 0), new Vec3i(0, 0, 2), new Vec3i(0, 0, -2), new Vec3i(-2, 0, 0), new Vec3i(1, 0, -1), new Vec3i(1, 0, 0), new Vec3i(1, 0, 1), new Vec3i(0, 0, -1), new Vec3i(0, 0, 1), new Vec3i(-1, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(-1, 0, 1), new Vec3i(1, 1, 0), new Vec3i(0, 1, -1), new Vec3i(0, 1, 0), new Vec3i(0, 1, 1), new Vec3i(-1, 1, 0), new Vec3i(0, 2, 0)));
    public final CopyOnWriteArraySet<BlockPos> trackedBlocks = new CopyOnWriteArraySet();
    public final CopyOnWriteArraySet<BlockPos> pendingBlocks = new CopyOnWriteArraySet();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"SOFT", "FULL"});
    public final PercentProperty opacity = new PercentProperty("opacity", 50);
    public final IntProperty range = new IntProperty("range", 64, 16, 512);
    public final BooleanProperty cavesOnly = new BooleanProperty("caves-only", true);
    public final IntProperty caveRadius = new IntProperty("caves-radius", 2, 1, 2);
    public final BooleanProperty diamonds = new BooleanProperty("diamonds", true);
    public final BooleanProperty diamondTracers = new BooleanProperty("diamonds-tracers", true);
    public final BooleanProperty gold = new BooleanProperty("gold", true);
    public final BooleanProperty goldTracers = new BooleanProperty("gold-tracers", true);
    public final BooleanProperty iron = new BooleanProperty("iron", false);
    public final BooleanProperty ironTracers = new BooleanProperty("iron-tracers", false);
    public final BooleanProperty coal = new BooleanProperty("coal", false);
    public final BooleanProperty coalTracers = new BooleanProperty("coal-tracers", false);
    public final BooleanProperty redstone = new BooleanProperty("redstone", false);
    public final BooleanProperty redStoneTracers = new BooleanProperty("redstone-tracers", false);
    public final BooleanProperty lapis = new BooleanProperty("lapis", false);
    public final BooleanProperty lapisTracers = new BooleanProperty("lapis-tracers", false);
    public final BooleanProperty emeralds = new BooleanProperty("emeralds", false);
    public final BooleanProperty emeraldsTracers = new BooleanProperty("emeralds-tracers", false);
    public final BooleanProperty spawners = new BooleanProperty("spawners", false);
    public final BooleanProperty spawnerTracers = new BooleanProperty("spawners-tracers", false);
    public final BooleanProperty canes = new BooleanProperty("canes", false);
    public final BooleanProperty canesTracers = new BooleanProperty("canes-tracers", false);
    public final BooleanProperty warts = new BooleanProperty("warts", false);
    public final BooleanProperty wartsTracers = new BooleanProperty("warts-tracers", false);

    private void renderOreHighlight(BlockPos blockPos, int blockId, Vec3 viewVector) {
        if (Xray.mc.field_71439_g.func_70011_f((double)blockPos.func_177958_n(), (double)blockPos.func_177956_o(), (double)blockPos.func_177952_p()) <= ((Integer)this.range.getValue()).doubleValue()) {
            Color color = this.getOreColor(blockId);
            RenderUtil.drawBlockBoundingBox(blockPos, 1.0, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 1.5f);
            if (this.shouldDrawTracer(blockId)) {
                RenderUtil.drawLine3D(viewVector, (double)blockPos.func_177958_n() + 0.5, (double)blockPos.func_177956_o() + 0.5, (double)blockPos.func_177952_p() + 0.5, (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 1.0f, 1.5f);
            }
        }
    }

    private Color getOreColor(int blockId) {
        switch (blockId) {
            case 14: {
                return new Color(0xFFFF55);
            }
            case 15: {
                return new Color(0xFFFFFF);
            }
            case 16: {
                return new Color(0);
            }
            case 21: {
                return new Color(0x5555FF);
            }
            case 52: {
                return new Color(0xFF55FF);
            }
            case 56: {
                return new Color(0x55FFFF);
            }
            case 73: 
            case 74: {
                return new Color(0xFF5555);
            }
            case 83: {
                return new Color(0xAAFFAA);
            }
            case 115: {
                return new Color(0xAA0000);
            }
            case 129: {
                return new Color(0x55FF55);
            }
        }
        return new Color(-1);
    }

    private boolean shouldDrawTracer(int blockId) {
        switch (blockId) {
            case 14: {
                return (Boolean)this.goldTracers.getValue();
            }
            case 15: {
                return (Boolean)this.ironTracers.getValue();
            }
            case 16: {
                return (Boolean)this.coalTracers.getValue();
            }
            case 21: {
                return (Boolean)this.lapisTracers.getValue();
            }
            case 52: {
                return (Boolean)this.spawnerTracers.getValue();
            }
            case 56: {
                return (Boolean)this.diamondTracers.getValue();
            }
            case 73: 
            case 74: {
                return (Boolean)this.redStoneTracers.getValue();
            }
            case 83: {
                return (Boolean)this.canesTracers.getValue();
            }
            case 115: {
                return (Boolean)this.wartsTracers.getValue();
            }
            case 129: {
                return (Boolean)this.emeraldsTracers.getValue();
            }
        }
        return false;
    }

    private boolean isValidCaveBlock(BlockPos pos) {
        if (Xray.mc.field_71441_e.func_175668_a(pos, false)) {
            Block block = Xray.mc.field_71441_e.func_180495_p(pos).func_177230_c();
            return block instanceof BlockMobSpawner || !block.func_149730_j() || !block.func_149688_o().func_76218_k() || block.func_149744_f();
        }
        return false;
    }

    public Xray() {
        super("Xray", false);
    }

    public boolean shouldRenderSide(int blockId) {
        return xrayBlocks.contains(blockId);
    }

    public boolean isXrayBlock(int blockId) {
        switch (blockId) {
            case 14: {
                return (Boolean)this.gold.getValue();
            }
            case 15: {
                return (Boolean)this.iron.getValue();
            }
            case 16: {
                return (Boolean)this.coal.getValue();
            }
            case 21: {
                return (Boolean)this.lapis.getValue();
            }
            case 52: {
                return (Boolean)this.spawners.getValue();
            }
            case 56: {
                return (Boolean)this.diamonds.getValue();
            }
            case 73: 
            case 74: {
                return (Boolean)this.redstone.getValue();
            }
            case 83: {
                return (Boolean)this.canes.getValue();
            }
            case 115: {
                return (Boolean)this.warts.getValue();
            }
            case 129: {
                return (Boolean)this.emeralds.getValue();
            }
        }
        return false;
    }

    public boolean checkBlock(BlockPos blockPos) {
        if (!((Boolean)this.cavesOnly.getValue()).booleanValue()) {
            return true;
        }
        if ((Integer)this.caveRadius.getValue() >= 2) {
            for (Vec3i vec3i : caveOffsetsLarge) {
                if (!this.isValidCaveBlock(blockPos.func_177971_a(vec3i))) continue;
                return true;
            }
        } else {
            for (Vec3i vec3i : caveOffsetsSmall) {
                if (!this.isValidCaveBlock(blockPos.func_177971_a(vec3i))) continue;
                return true;
            }
        }
        return false;
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (this.isEnabled()) {
            int id;
            Vec3 vec3 = Xray.mc.field_71474_y.field_74320_O == 0 ? new Vec3(0.0, 0.0, 1.0).func_178789_a((float)(-Math.toRadians(RenderUtil.lerpFloat(Xray.mc.func_175606_aa().field_70125_A, Xray.mc.func_175606_aa().field_70127_C, ((IAccessorMinecraft)Xray.mc).getTimer().field_74281_c)))).func_178785_b((float)(-Math.toRadians(RenderUtil.lerpFloat(Xray.mc.func_175606_aa().field_70177_z, Xray.mc.func_175606_aa().field_70126_B, ((IAccessorMinecraft)Xray.mc).getTimer().field_74281_c)))) : new Vec3(0.0, 0.0, 0.0).func_178789_a((float)(-Math.toRadians(RenderUtil.lerpFloat(Xray.mc.field_71439_g.field_70726_aT, Xray.mc.field_71439_g.field_70727_aS, ((IAccessorMinecraft)Xray.mc).getTimer().field_74281_c)))).func_178785_b((float)(-Math.toRadians(RenderUtil.lerpFloat(Xray.mc.field_71439_g.field_71109_bG, Xray.mc.field_71439_g.field_71107_bF, ((IAccessorMinecraft)Xray.mc).getTimer().field_74281_c))));
            vec3 = new Vec3(vec3.field_72450_a, vec3.field_72448_b + (double)mc.func_175606_aa().func_70047_e(), vec3.field_72449_c);
            RenderUtil.enableRenderState();
            for (BlockPos blockPos : this.trackedBlocks) {
                if (this.pendingBlocks.contains(blockPos)) {
                    this.trackedBlocks.remove(blockPos);
                    continue;
                }
                id = Block.func_149682_b((Block)Xray.mc.field_71441_e.func_180495_p(blockPos).func_177230_c());
                if (this.isXrayBlock(id)) {
                    this.renderOreHighlight(blockPos, id, vec3);
                    continue;
                }
                this.trackedBlocks.remove(blockPos);
            }
            for (BlockPos blockPos : this.pendingBlocks) {
                id = Block.func_149682_b((Block)Xray.mc.field_71441_e.func_180495_p(blockPos).func_177230_c());
                if (this.isXrayBlock(id)) {
                    this.renderOreHighlight(blockPos, id, vec3);
                    continue;
                }
                this.pendingBlocks.remove(blockPos);
            }
            RenderUtil.disableRenderState();
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getType() == EventType.RECEIVE) {
            S23PacketBlockChange packet;
            if (event.getPacket() instanceof S22PacketMultiBlockChange) {
                for (S22PacketMultiBlockChange.BlockUpdateData blockUpdateData : ((S22PacketMultiBlockChange)event.getPacket()).func_179844_a()) {
                    if (!this.isXrayBlock(Block.func_149682_b((Block)blockUpdateData.func_180088_c().func_177230_c()))) continue;
                    this.pendingBlocks.add(new BlockPos((Vec3i)blockUpdateData.func_180090_a()));
                }
            } else if (event.getPacket() instanceof S23PacketBlockChange && this.isXrayBlock(Block.func_149682_b((Block)(packet = (S23PacketBlockChange)event.getPacket()).func_180728_a().func_177230_c()))) {
                this.pendingBlocks.add(new BlockPos((Vec3i)packet.func_179827_b()));
            }
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.trackedBlocks.clear();
        this.pendingBlocks.clear();
    }

    @Override
    public void onEnabled() {
        ForgeModContainer.forgeLightPipelineEnabled = false;
        if (Xray.mc.field_71438_f != null) {
            Xray.mc.field_71438_f.func_72712_a();
        }
    }

    @Override
    public void onDisabled() {
        ForgeModContainer.forgeLightPipelineEnabled = true;
        if (Xray.mc.field_71438_f != null) {
            Xray.mc.field_71438_f.func_72712_a();
        }
    }

    @Override
    public void verifyValue(String mode) {
        this.trackedBlocks.clear();
        this.pendingBlocks.clear();
        if (this.isEnabled() && Xray.mc.field_71438_f != null) {
            Xray.mc.field_71438_f.func_72712_a();
        }
    }
}

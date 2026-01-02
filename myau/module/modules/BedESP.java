package myau.module.modules;

import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;
import myau.Myau;
import myau.event.EventTarget;
import myau.events.Render3DEvent;
import myau.mixin.IAccessorRenderManager;
import myau.module.Module;
import myau.module.modules.HUD;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ColorProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.RenderUtil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BedESP
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final CopyOnWriteArraySet<BlockPos> beds = new CopyOnWriteArraySet();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"DEFAULT", "FULL"});
    public final ModeProperty color = new ModeProperty("color", 0, new String[]{"CUSTOM", "HUD"});
    public final ColorProperty customColor = new ColorProperty("custom-color", 0xFF5555, () -> (Integer)this.color.getValue() == 0);
    public final PercentProperty opacity = new PercentProperty("opacity", 25);
    public final BooleanProperty outline = new BooleanProperty("outline", false);
    public final BooleanProperty obsidian = new BooleanProperty("obsidian", true);

    private Color getColor() {
        switch ((Integer)this.color.getValue()) {
            case 0: {
                return new Color((Integer)this.customColor.getValue());
            }
            case 1: {
                return ((HUD)Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis());
            }
        }
        return new Color(-1);
    }

    private void drawObsidianBox(AxisAlignedBB axisAlignedBB) {
        if (((Boolean)this.outline.getValue()).booleanValue()) {
            RenderUtil.drawBoundingBox(axisAlignedBB, 170, 0, 170, 255, 1.5f);
        }
        RenderUtil.drawFilledBox(axisAlignedBB, 170, 0, 170);
    }

    private void drawObsidian(BlockPos blockPos) {
        if (((Boolean)this.outline.getValue()).booleanValue()) {
            RenderUtil.drawBlockBoundingBox(blockPos, 1.0, 170, 0, 170, 255, 1.5f);
        }
        RenderUtil.drawBlockBox(blockPos, 1.0, 170, 0, 170);
    }

    public BedESP() {
        super("BedESP", false);
    }

    public double getHeight() {
        return (Integer)this.mode.getValue() == 1 ? 1.0 : 0.5625;
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (this.isEnabled()) {
            RenderUtil.enableRenderState();
            for (BlockPos blockPos : this.beds) {
                IBlockState state = BedESP.mc.field_71441_e.func_180495_p(blockPos);
                if (state.func_177230_c() instanceof BlockBed && state.func_177229_b((IProperty)BlockBed.field_176472_a) == BlockBed.EnumPartType.HEAD) {
                    BlockPos opposite = blockPos.func_177972_a(((EnumFacing)state.func_177229_b((IProperty)BlockBed.field_176387_N)).func_176734_d());
                    IBlockState oppositeState = BedESP.mc.field_71441_e.func_180495_p(opposite);
                    if (!(oppositeState.func_177230_c() instanceof BlockBed) || oppositeState.func_177229_b((IProperty)BlockBed.field_176472_a) != BlockBed.EnumPartType.FOOT) continue;
                    if (((Boolean)this.obsidian.getValue()).booleanValue()) {
                        for (EnumFacing facing : Arrays.asList(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST)) {
                            BlockPos offsetX = blockPos.func_177972_a(facing);
                            BlockPos offsetZ = opposite.func_177972_a(facing);
                            boolean xObsidian = BedESP.mc.field_71441_e.func_180495_p(offsetX).func_177230_c() instanceof BlockObsidian;
                            boolean zObsidian = BedESP.mc.field_71441_e.func_180495_p(offsetZ).func_177230_c() instanceof BlockObsidian;
                            if (xObsidian && zObsidian) {
                                this.drawObsidianBox(new AxisAlignedBB((double)Math.min(offsetX.func_177958_n(), offsetZ.func_177958_n()), (double)offsetX.func_177956_o(), (double)Math.min(offsetX.func_177952_p(), offsetZ.func_177952_p()), Math.max((double)offsetX.func_177958_n() + 1.0, (double)offsetZ.func_177958_n() + 1.0), (double)offsetX.func_177956_o() + 1.0, Math.max((double)offsetX.func_177952_p() + 1.0, (double)offsetZ.func_177952_p() + 1.0)).func_72317_d(-((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ()));
                                continue;
                            }
                            if (xObsidian) {
                                this.drawObsidian(offsetX);
                                continue;
                            }
                            if (!zObsidian) continue;
                            this.drawObsidian(offsetZ);
                        }
                    }
                    AxisAlignedBB aabb = new AxisAlignedBB((double)Math.min(blockPos.func_177958_n(), opposite.func_177958_n()), (double)blockPos.func_177956_o(), (double)Math.min(blockPos.func_177952_p(), opposite.func_177952_p()), Math.max((double)blockPos.func_177958_n() + 1.0, (double)opposite.func_177958_n() + 1.0), (double)blockPos.func_177956_o() + this.getHeight(), Math.max((double)blockPos.func_177952_p() + 1.0, (double)opposite.func_177952_p() + 1.0)).func_72317_d(-((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ());
                    Color color = this.getColor();
                    if (((Boolean)this.outline.getValue()).booleanValue()) {
                        RenderUtil.drawBoundingBox(aabb, color.getRed(), color.getGreen(), color.getBlue(), 255, 1.5f);
                    }
                    RenderUtil.drawFilledBox(aabb, color.getRed(), color.getGreen(), color.getBlue());
                    continue;
                }
                this.beds.remove(blockPos);
            }
            RenderUtil.disableRenderState();
        }
    }

    @Override
    public void onEnabled() {
        if (BedESP.mc.field_71438_f != null) {
            BedESP.mc.field_71438_f.func_72712_a();
        }
    }
}

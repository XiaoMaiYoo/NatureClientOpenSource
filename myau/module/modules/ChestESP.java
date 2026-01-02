package myau.module.modules;

import java.awt.Color;
import java.util.stream.Collectors;
import myau.Myau;
import myau.event.EventTarget;
import myau.events.Render3DEvent;
import myau.mixin.IAccessorMinecraft;
import myau.mixin.IAccessorRenderManager;
import myau.module.Module;
import myau.module.modules.Tracers;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ColorProperty;
import myau.property.properties.PercentProperty;
import myau.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class ChestESP
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final ColorProperty color = new ColorProperty("color", new Color(255, 170, 0).getRGB());
    public final PercentProperty opacity = new PercentProperty("opacity", 100);
    public final BooleanProperty tracers = new BooleanProperty("tracers", false);

    public ChestESP() {
        super("ChestESP", false);
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (this.isEnabled()) {
            RenderUtil.enableRenderState();
            for (TileEntity chest : ChestESP.mc.field_71441_e.field_147482_g.stream().filter(tileEntity -> tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityEnderChest).collect(Collectors.toList())) {
                AxisAlignedBB aabb = new AxisAlignedBB((double)chest.func_174877_v().func_177958_n() + 0.0625, (double)chest.func_174877_v().func_177956_o() + 0.0, (double)chest.func_174877_v().func_177952_p() + 0.0625, (double)chest.func_174877_v().func_177958_n() + 0.9375, (double)chest.func_174877_v().func_177956_o() + 0.875, (double)chest.func_174877_v().func_177952_p() + 0.9375).func_72317_d(-((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ());
                Color color = new Color((Integer)this.color.getValue());
                RenderUtil.drawBoundingBox(aabb, color.getRed(), color.getGreen(), color.getBlue(), (int)((float)((Integer)this.opacity.getValue()).intValue() / 100.0f * 255.0f), 1.5f);
                if (!((Boolean)this.tracers.getValue()).booleanValue()) continue;
                Vec3 vec = ChestESP.mc.field_71474_y.field_74320_O == 0 ? new Vec3(0.0, 0.0, 1.0).func_178789_a((float)(-Math.toRadians(RenderUtil.lerpFloat(ChestESP.mc.func_175606_aa().field_70125_A, ChestESP.mc.func_175606_aa().field_70127_C, ((IAccessorMinecraft)ChestESP.mc).getTimer().field_74281_c)))).func_178785_b((float)(-Math.toRadians(RenderUtil.lerpFloat(ChestESP.mc.func_175606_aa().field_70177_z, ChestESP.mc.func_175606_aa().field_70126_B, ((IAccessorMinecraft)ChestESP.mc).getTimer().field_74281_c)))) : new Vec3(0.0, 0.0, 0.0).func_178789_a((float)(-Math.toRadians(RenderUtil.lerpFloat(ChestESP.mc.field_71439_g.field_70726_aT, ChestESP.mc.field_71439_g.field_70727_aS, ((IAccessorMinecraft)ChestESP.mc).getTimer().field_74281_c)))).func_178785_b((float)(-Math.toRadians(RenderUtil.lerpFloat(ChestESP.mc.field_71439_g.field_71109_bG, ChestESP.mc.field_71439_g.field_71107_bF, ((IAccessorMinecraft)ChestESP.mc).getTimer().field_74281_c))));
                vec = new Vec3(vec.field_72450_a, vec.field_72448_b + (double)mc.func_175606_aa().func_70047_e(), vec.field_72449_c);
                float opacity = (float)((Integer)((Tracers)Myau.moduleManager.modules.get(Tracers.class)).opacity.getValue()).intValue() / 100.0f;
                RenderUtil.drawLine3D(vec, (double)chest.func_174877_v().func_177958_n() + 0.5, (double)chest.func_174877_v().func_177956_o() + 0.5, (double)chest.func_174877_v().func_177952_p() + 0.5, (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, opacity, 1.5f);
            }
            RenderUtil.disableRenderState();
        }
    }
}

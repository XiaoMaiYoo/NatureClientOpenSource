package myau.module.modules;

import java.awt.Color;
import java.util.ArrayList;
import myau.event.EventTarget;
import myau.events.Render3DEvent;
import myau.mixin.IAccessorRenderManager;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.PercentProperty;
import myau.util.RenderUtil;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSnowball;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class Trajectories
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final PercentProperty opacity = new PercentProperty("opacity", 100);
    public final BooleanProperty bow = new BooleanProperty("bow", true);
    public final BooleanProperty projectiles = new BooleanProperty("projectiles", false);
    public final BooleanProperty pearls = new BooleanProperty("pearls", true);

    public Trajectories() {
        super("Trajectories", false, true);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (this.isEnabled() && Trajectories.mc.field_71439_g.func_70694_bm() != null && Trajectories.mc.field_71474_y.field_74320_O == 0) {
            float hitboxExpand;
            float gravity;
            Item item = Trajectories.mc.field_71439_g.func_70694_bm().func_77973_b();
            RenderManager renderManager = mc.func_175598_ae();
            boolean isBow = false;
            float velocityMultiplier = 1.5f;
            float drag = 0.99f;
            if (item instanceof ItemBow && ((Boolean)this.bow.getValue()).booleanValue()) {
                if (!Trajectories.mc.field_71439_g.func_71039_bw()) {
                    return;
                }
                isBow = true;
                gravity = 0.05f;
                hitboxExpand = 0.3f;
                float charge = (float)Trajectories.mc.field_71439_g.func_71057_bx() / 20.0f;
                if ((charge = (charge * charge + charge * 2.0f) / 3.0f) < 0.1f) {
                    return;
                }
                if (charge > 1.0f) {
                    charge = 1.0f;
                }
                velocityMultiplier = charge * 3.0f;
            } else if (item instanceof ItemFishingRod && ((Boolean)this.projectiles.getValue()).booleanValue()) {
                gravity = 0.04f;
                hitboxExpand = 0.25f;
                drag = 0.92f;
            } else if ((item instanceof ItemSnowball || item instanceof ItemEgg) && ((Boolean)this.projectiles.getValue()).booleanValue()) {
                gravity = 0.03f;
                hitboxExpand = 0.25f;
            } else {
                if (!(item instanceof ItemEnderPearl) || !((Boolean)this.pearls.getValue()).booleanValue()) {
                    return;
                }
                gravity = 0.03f;
                hitboxExpand = 0.25f;
            }
            float yaw = Trajectories.mc.field_71439_g.field_70177_z;
            float pitch = Trajectories.mc.field_71439_g.field_70125_A;
            double x = ((IAccessorRenderManager)renderManager).getRenderPosX() - (double)MathHelper.func_76134_b((float)(yaw / 180.0f * (float)Math.PI)) * 0.16;
            double y = ((IAccessorRenderManager)renderManager).getRenderPosY() + (double)Trajectories.mc.field_71439_g.func_70047_e() - (double)0.1f;
            double z = ((IAccessorRenderManager)renderManager).getRenderPosZ() - (double)MathHelper.func_76126_a((float)(yaw / 180.0f * (float)Math.PI)) * 0.16;
            double mx = (double)(MathHelper.func_76126_a((float)(yaw / 180.0f * (float)Math.PI)) * MathHelper.func_76134_b((float)(pitch / 180.0f * (float)Math.PI))) * (isBow ? 1.0 : 0.4) * -1.0;
            double my = (double)MathHelper.func_76126_a((float)(pitch / 180.0f * (float)Math.PI)) * (isBow ? 1.0 : 0.4) * -1.0;
            double mz = (double)(MathHelper.func_76134_b((float)(yaw / 180.0f * (float)Math.PI)) * MathHelper.func_76134_b((float)(pitch / 180.0f * (float)Math.PI))) * (isBow ? 1.0 : 0.4);
            float mag = MathHelper.func_76133_a((double)(mx * mx + my * my + mz * mz));
            mx /= (double)mag;
            my /= (double)mag;
            mz /= (double)mag;
            mx *= (double)velocityMultiplier;
            my *= (double)velocityMultiplier;
            mz *= (double)velocityMultiplier;
            MovingObjectPosition mop = null;
            boolean hasHitBlock = false;
            boolean hasHitEntity = false;
            WorldRenderer worldRenderer = Tessellator.func_178181_a().func_178180_c();
            ArrayList<Vec3> trajectoryPoints = new ArrayList<Vec3>();
            while (!hasHitBlock && y > 0.0) {
                Vec3 start = new Vec3(x, y, z);
                Vec3 end = new Vec3(x + mx, y + my, z + mz);
                mop = Trajectories.mc.field_71441_e.func_147447_a(start, end, false, true, false);
                start = new Vec3(x, y, z);
                end = new Vec3(x + mx, y + my, z + mz);
                if (mop != null) {
                    hasHitBlock = true;
                    end = new Vec3(mop.field_72307_f.field_72450_a, mop.field_72307_f.field_72448_b, mop.field_72307_f.field_72449_c);
                }
                AxisAlignedBB aabb = new AxisAlignedBB(x - (double)hitboxExpand, y - (double)hitboxExpand, z - (double)hitboxExpand, x + (double)hitboxExpand, y + (double)hitboxExpand, z + (double)hitboxExpand).func_72321_a(mx, my, mz).func_72314_b(1.0, 1.0, 1.0);
                int minChunkX = MathHelper.func_76128_c((double)((aabb.field_72340_a - 2.0) / 16.0));
                int maxChunkX = MathHelper.func_76128_c((double)((aabb.field_72336_d + 2.0) / 16.0));
                int minChunkZ = MathHelper.func_76128_c((double)((aabb.field_72339_c - 2.0) / 16.0));
                int maxChunkZ = MathHelper.func_76128_c((double)((aabb.field_72334_f + 2.0) / 16.0));
                ArrayList possibleEntities = new ArrayList();
                for (int x1 = minChunkX; x1 <= maxChunkX; ++x1) {
                    for (int z1 = minChunkZ; z1 <= maxChunkZ; ++z1) {
                        Trajectories.mc.field_71441_e.func_72964_e(x1, z1).func_177414_a((Entity)Trajectories.mc.field_71439_g, aabb, possibleEntities, null);
                    }
                }
                for (Entity entity : possibleEntities) {
                    AxisAlignedBB entityBox;
                    MovingObjectPosition intercept;
                    if (!entity.func_70067_L() || entity == Trajectories.mc.field_71439_g || (intercept = (entityBox = entity.func_174813_aQ().func_72314_b((double)hitboxExpand, (double)hitboxExpand, (double)hitboxExpand)).func_72327_a(start, end)) == null) continue;
                    hasHitEntity = true;
                    hasHitBlock = true;
                    mop = intercept;
                }
                if (Trajectories.mc.field_71441_e.func_180495_p(new BlockPos(x += mx, y += my, z += mz)).func_177230_c().func_149688_o() == Material.field_151586_h) {
                    mx *= 0.6;
                    my *= 0.6;
                    mz *= 0.6;
                } else {
                    mx *= (double)drag;
                    my *= (double)drag;
                    mz *= (double)drag;
                }
                my -= (double)gravity;
                trajectoryPoints.add(new Vec3(x - ((IAccessorRenderManager)renderManager).getRenderPosX(), y - ((IAccessorRenderManager)renderManager).getRenderPosY(), z - ((IAccessorRenderManager)renderManager).getRenderPosZ()));
            }
            if (trajectoryPoints.size() > 1) {
                RenderUtil.enableRenderState();
                RenderUtil.setColor(new Color(hasHitEntity ? 85 : 255, 255, hasHitEntity ? 85 : 255, (int)(((Integer)this.opacity.getValue()).floatValue() / 100.0f * 255.0f)).getRGB());
                GL11.glLineWidth((float)1.5f);
                GL11.glEnable((int)2848);
                GL11.glHint((int)3154, (int)4354);
                worldRenderer.func_181668_a(3, DefaultVertexFormats.field_181705_e);
                trajectoryPoints.forEach(vec3 -> worldRenderer.func_181662_b(vec3.field_72450_a, vec3.field_72448_b, vec3.field_72449_c).func_181675_d());
                Tessellator.func_178181_a().func_78381_a();
                GlStateManager.func_179094_E();
                GlStateManager.func_179137_b((double)(x - ((IAccessorRenderManager)renderManager).getRenderPosX()), (double)(y - ((IAccessorRenderManager)renderManager).getRenderPosY()), (double)(z - ((IAccessorRenderManager)renderManager).getRenderPosZ()));
                if (mop != null) {
                    switch (mop.field_178784_b.func_176740_k().ordinal()) {
                        case 0: {
                            GlStateManager.func_179114_b((float)90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                            break;
                        }
                        case 1: {
                            GlStateManager.func_179114_b((float)90.0f, (float)1.0f, (float)0.0f, (float)0.0f);
                        }
                    }
                    RenderUtil.drawLine(-0.25f, -0.25f, 0.25f, 0.25f, 1.5f, new Color(hasHitEntity ? 85 : 255, 255, hasHitEntity ? 85 : 255, (int)(((Integer)this.opacity.getValue()).floatValue() / 100.0f * 255.0f)).getRGB());
                    RenderUtil.drawLine(-0.25f, 0.25f, 0.25f, -0.25f, 1.5f, new Color(hasHitEntity ? 85 : 255, 255, hasHitEntity ? 85 : 255, (int)(((Integer)this.opacity.getValue()).floatValue() / 100.0f * 255.0f)).getRGB());
                }
                GlStateManager.func_179121_F();
                GL11.glDisable((int)2848);
                GL11.glLineWidth((float)2.0f);
                GlStateManager.func_179117_G();
                RenderUtil.disableRenderState();
            }
        }
    }
}

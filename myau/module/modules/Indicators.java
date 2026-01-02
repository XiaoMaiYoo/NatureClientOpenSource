package myau.module.modules;

import java.awt.Color;
import java.util.stream.Collectors;
import myau.enums.ChatColors;
import myau.event.EventTarget;
import myau.events.Render2DEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.util.RenderUtil;
import myau.util.RotationUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Indicators
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final FloatProperty scale = new FloatProperty("scale", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(1.5f));
    public final FloatProperty offset = new FloatProperty("offset", Float.valueOf(50.0f), Float.valueOf(0.0f), Float.valueOf(255.0f));
    public final BooleanProperty directionCheck = new BooleanProperty("direction-check", true);
    public final BooleanProperty fireballs = new BooleanProperty("fireballs", true);
    public final BooleanProperty pearls = new BooleanProperty("pearls", true);
    public final BooleanProperty arrows = new BooleanProperty("arrows", true);

    private boolean shouldRender(Entity entity) {
        double d = (entity.field_70165_t - entity.field_70142_S) * (Indicators.mc.field_71439_g.field_70165_t - entity.field_70165_t) + (entity.field_70163_u - entity.field_70137_T) * (Indicators.mc.field_71439_g.field_70163_u + (double)Indicators.mc.field_71439_g.func_70047_e() - entity.field_70163_u - (double)entity.field_70131_O / 2.0) + (entity.field_70161_v - entity.field_70136_U) * (Indicators.mc.field_71439_g.field_70161_v - entity.field_70161_v);
        if (d == 0.0) {
            return false;
        }
        if (d < 0.0 && ((Boolean)this.directionCheck.getValue()).booleanValue()) {
            return false;
        }
        if (((Boolean)this.fireballs.getValue()).booleanValue() && entity instanceof EntityFireball) {
            return true;
        }
        if (((Boolean)this.pearls.getValue()).booleanValue() && entity instanceof EntityEnderPearl) {
            return true;
        }
        if (!((Boolean)this.arrows.getValue()).booleanValue()) {
            return false;
        }
        return entity instanceof EntityArrow;
    }

    private Item getIndicatorItem(Entity entity) {
        if (entity instanceof EntityFireball) {
            return Items.field_151059_bz;
        }
        if (entity instanceof EntityEnderPearl) {
            return Items.field_151079_bi;
        }
        if (entity instanceof EntityArrow) {
            return Items.field_151032_g;
        }
        return new Item();
    }

    private Color getIndicatorColor(Entity entity) {
        if (entity instanceof EntityFireball) {
            return new Color(12676363);
        }
        if (entity instanceof EntityEnderPearl) {
            return new Color(2458740);
        }
        if (entity instanceof EntityArrow) {
            return new Color(0x969696);
        }
        return new Color(-1);
    }

    public Indicators() {
        super("Indicators", false, true);
    }

    @EventTarget
    public void onRender(Render2DEvent render2DEvent) {
        if (!this.isEnabled()) {
            return;
        }
        for (Entity entity : TeamUtil.getLoadedEntitiesSorted().stream().filter(this::shouldRender).collect(Collectors.toList())) {
            float offset = 10.0f + ((Float)this.offset.getValue()).floatValue();
            float yawBetween = RotationUtil.getYawBetween(RenderUtil.lerpDouble(Indicators.mc.field_71439_g.field_70165_t, Indicators.mc.field_71439_g.field_70169_q, render2DEvent.getPartialTicks()), RenderUtil.lerpDouble(Indicators.mc.field_71439_g.field_70161_v, Indicators.mc.field_71439_g.field_70166_s, render2DEvent.getPartialTicks()), RenderUtil.lerpDouble(entity.field_70165_t, entity.field_70169_q, render2DEvent.getPartialTicks()), RenderUtil.lerpDouble(entity.field_70161_v, entity.field_70166_s, render2DEvent.getPartialTicks()));
            if (Indicators.mc.field_71474_y.field_74320_O == 2) {
                yawBetween += 180.0f;
            }
            float x = (float)Math.sin(Math.toRadians(yawBetween));
            float z = (float)Math.cos(Math.toRadians(yawBetween)) * -1.0f;
            GlStateManager.func_179094_E();
            GlStateManager.func_179097_i();
            GlStateManager.func_179152_a((float)((Float)this.scale.getValue()).floatValue(), (float)((Float)this.scale.getValue()).floatValue(), (float)0.0f);
            GlStateManager.func_179109_b((float)((float)new ScaledResolution(mc).func_78326_a() / 2.0f / ((Float)this.scale.getValue()).floatValue()), (float)((float)new ScaledResolution(mc).func_78328_b() / 2.0f / ((Float)this.scale.getValue()).floatValue()), (float)0.0f);
            GlStateManager.func_179094_E();
            GlStateManager.func_179109_b((float)((offset + 0.0f) * x - 8.0f), (float)((offset + 0.0f) * z - 8.0f), (float)-300.0f);
            mc.func_175599_af().func_180450_b(new ItemStack(this.getIndicatorItem(entity)), 0, 0);
            GlStateManager.func_179121_F();
            String string = String.format("%dm", (int)Indicators.mc.field_71439_g.func_70032_d(entity));
            GlStateManager.func_179094_E();
            GlStateManager.func_179109_b((float)((offset + 0.0f) * x - (float)Indicators.mc.field_71466_p.func_78256_a(string) / 2.0f + 1.0f), (float)((offset + 0.0f) * z + 1.0f), (float)-100.0f);
            Indicators.mc.field_71466_p.func_175063_a(string, 0.0f, 0.0f, ChatColors.GRAY.toAwtColor() & 0xFFFFFF | 0xBF000000);
            GlStateManager.func_179121_F();
            GlStateManager.func_179094_E();
            GlStateManager.func_179109_b((float)((offset + 15.0f) * x + 1.0f), (float)((offset + 15.0f) * z + 1.0f), (float)-100.0f);
            RenderUtil.enableRenderState();
            RenderUtil.drawArrow(0.0f, 0.0f, (float)(Math.atan2(z, x) + Math.PI), 7.5f, 1.5f, this.getIndicatorColor(entity).getRGB());
            RenderUtil.disableRenderState();
            GlStateManager.func_179121_F();
            GlStateManager.func_179126_j();
            GlStateManager.func_179121_F();
        }
    }
}

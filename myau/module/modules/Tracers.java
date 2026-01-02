package myau.module.modules;

import java.awt.Color;
import java.util.stream.Collectors;
import myau.Myau;
import myau.enums.ChatColors;
import myau.event.EventTarget;
import myau.events.Render2DEvent;
import myau.events.Render3DEvent;
import myau.mixin.IAccessorMinecraft;
import myau.module.Module;
import myau.module.modules.HUD;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.RenderUtil;
import myau.util.RotationUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Tracers
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final ModeProperty colorMode = new ModeProperty("color", 0, new String[]{"DEFAULT", "TEAMS", "HUD"});
    public final BooleanProperty drawLines = new BooleanProperty("lines", true);
    public final BooleanProperty drawArrows = new BooleanProperty("arrows", false);
    public final PercentProperty opacity = new PercentProperty("opacity", 100);
    public final BooleanProperty showPlayers = new BooleanProperty("players", true);
    public final BooleanProperty showFriends = new BooleanProperty("friends", true);
    public final BooleanProperty showEnemies = new BooleanProperty("enemies", true);
    public final BooleanProperty showBots = new BooleanProperty("bots", false);

    private boolean shouldRender(EntityPlayer entityPlayer) {
        if (entityPlayer.field_70725_aQ > 0) {
            return false;
        }
        if (mc.func_175606_aa().func_70032_d((Entity)entityPlayer) > 512.0f) {
            return false;
        }
        if (entityPlayer != Tracers.mc.field_71439_g && entityPlayer != mc.func_175606_aa()) {
            if (TeamUtil.isBot(entityPlayer)) {
                return (Boolean)this.showBots.getValue();
            }
            if (TeamUtil.isFriend(entityPlayer)) {
                return (Boolean)this.showFriends.getValue();
            }
            return TeamUtil.isTarget(entityPlayer) ? ((Boolean)this.showEnemies.getValue()).booleanValue() : ((Boolean)this.showPlayers.getValue()).booleanValue();
        }
        return false;
    }

    private Color getEntityColor(EntityPlayer entityPlayer, float alpha) {
        if (TeamUtil.isFriend(entityPlayer)) {
            Color color = Myau.friendManager.getColor();
            return new Color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, alpha);
        }
        if (TeamUtil.isTarget(entityPlayer)) {
            Color color = Myau.targetManager.getColor();
            return new Color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, alpha);
        }
        switch ((Integer)this.colorMode.getValue()) {
            case 0: {
                return TeamUtil.getTeamColor(entityPlayer, alpha);
            }
            case 1: {
                int teamColor = TeamUtil.isSameTeam(entityPlayer) ? ChatColors.BLUE.toAwtColor() : ChatColors.RED.toAwtColor();
                return new Color(teamColor & Color.WHITE.getRGB() | (int)(alpha * 255.0f) << 24, true);
            }
            case 2: {
                int color = ((HUD)Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis()).getRGB();
                return new Color(color & Color.WHITE.getRGB() | (int)(alpha * 255.0f) << 24, true);
            }
        }
        return new Color(1.0f, 1.0f, 1.0f, alpha);
    }

    public Tracers() {
        super("Tracers", false);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (this.isEnabled() && ((Boolean)this.drawLines.getValue()).booleanValue()) {
            RenderUtil.enableRenderState();
            Vec3 position = Tracers.mc.field_71474_y.field_74320_O == 0 ? new Vec3(0.0, 0.0, 1.0).func_178789_a((float)(-Math.toRadians(RenderUtil.lerpFloat(Tracers.mc.func_175606_aa().field_70125_A, Tracers.mc.func_175606_aa().field_70127_C, ((IAccessorMinecraft)Tracers.mc).getTimer().field_74281_c)))).func_178785_b((float)(-Math.toRadians(RenderUtil.lerpFloat(Tracers.mc.func_175606_aa().field_70177_z, Tracers.mc.func_175606_aa().field_70126_B, ((IAccessorMinecraft)Tracers.mc).getTimer().field_74281_c)))) : new Vec3(0.0, 0.0, 0.0).func_178789_a((float)(-Math.toRadians(RenderUtil.lerpFloat(Tracers.mc.field_71439_g.field_70726_aT, Tracers.mc.field_71439_g.field_70727_aS, ((IAccessorMinecraft)Tracers.mc).getTimer().field_74281_c)))).func_178785_b((float)(-Math.toRadians(RenderUtil.lerpFloat(Tracers.mc.field_71439_g.field_71109_bG, Tracers.mc.field_71439_g.field_71107_bF, ((IAccessorMinecraft)Tracers.mc).getTimer().field_74281_c))));
            position = new Vec3(position.field_72450_a, position.field_72448_b + (double)mc.func_175606_aa().func_70047_e(), position.field_72449_c);
            for (EntityPlayer player : TeamUtil.getLoadedEntitiesSorted().stream().filter(entity -> entity instanceof EntityPlayer && this.shouldRender((EntityPlayer)entity)).map(EntityPlayer.class::cast).collect(Collectors.toList())) {
                Color color = this.getEntityColor(player, (float)((Integer)this.opacity.getValue()).intValue() / 100.0f);
                double x = RenderUtil.lerpDouble(player.field_70165_t, player.field_70142_S, event.getPartialTicks());
                double y = RenderUtil.lerpDouble(player.field_70163_u, player.field_70137_T, event.getPartialTicks()) - (player.func_70093_af() ? 0.125 : 0.0);
                double z = RenderUtil.lerpDouble(player.field_70161_v, player.field_70136_U, event.getPartialTicks());
                RenderUtil.drawLine3D(position, x, y + (double)player.func_70047_e(), z, (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f, 1.5f);
            }
            RenderUtil.disableRenderState();
        }
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (this.isEnabled() && ((Boolean)this.drawArrows.getValue()).booleanValue()) {
            for (EntityPlayer player : TeamUtil.getLoadedEntitiesSorted().stream().filter(entity -> entity instanceof EntityPlayer && this.shouldRender((EntityPlayer)entity)).map(EntityPlayer.class::cast).collect(Collectors.toList())) {
                float yawBetween = RotationUtil.getYawBetween(RenderUtil.lerpDouble(Tracers.mc.field_71439_g.field_70165_t, Tracers.mc.field_71439_g.field_70169_q, event.getPartialTicks()), RenderUtil.lerpDouble(Tracers.mc.field_71439_g.field_70161_v, Tracers.mc.field_71439_g.field_70166_s, event.getPartialTicks()), RenderUtil.lerpDouble(player.field_70165_t, player.field_70169_q, event.getPartialTicks()), RenderUtil.lerpDouble(player.field_70161_v, player.field_70166_s, event.getPartialTicks()));
                if (Tracers.mc.field_71474_y.field_74320_O == 2) {
                    yawBetween += 180.0f;
                }
                float arrowDirX = (float)Math.sin(Math.toRadians(yawBetween));
                float arrowDirY = (float)Math.cos(Math.toRadians(yawBetween)) * -1.0f;
                float opacity = ((Integer)this.opacity.getValue()).floatValue() / 100.0f;
                if ((yawBetween = Math.abs(MathHelper.func_76142_g((float)yawBetween))) < 30.0f) {
                    opacity = 0.0f;
                } else if (yawBetween < 60.0f) {
                    opacity *= (yawBetween - 30.0f) / 30.0f;
                }
                HUD hud = (HUD)Myau.moduleManager.modules.get(HUD.class);
                GlStateManager.func_179094_E();
                GlStateManager.func_179152_a((float)((Float)hud.scale.getValue()).floatValue(), (float)((Float)hud.scale.getValue()).floatValue(), (float)0.0f);
                GlStateManager.func_179109_b((float)((float)new ScaledResolution(mc).func_78326_a() / 2.0f / ((Float)hud.scale.getValue()).floatValue()), (float)((float)new ScaledResolution(mc).func_78328_b() / 2.0f / ((Float)hud.scale.getValue()).floatValue()), (float)0.0f);
                GlStateManager.func_179094_E();
                GlStateManager.func_179109_b((float)(55.0f * arrowDirX + 1.0f), (float)(55.0f * arrowDirY + 1.0f), (float)-100.0f);
                RenderUtil.enableRenderState();
                RenderUtil.drawTriangle(0.0f, 0.0f, (float)(Math.atan2(arrowDirY, arrowDirX) + Math.PI), 10.0f, this.getEntityColor(player, opacity).getRGB());
                RenderUtil.disableRenderState();
                GlStateManager.func_179121_F();
                GlStateManager.func_179121_F();
            }
        }
    }
}

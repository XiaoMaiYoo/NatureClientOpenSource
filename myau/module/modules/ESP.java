package myau.module.modules;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;
import javax.vecmath.Vector4d;
import myau.Myau;
import myau.enums.ChatColors;
import myau.event.EventTarget;
import myau.events.Render2DEvent;
import myau.events.Render3DEvent;
import myau.events.ResizeEvent;
import myau.mixin.IAccessorEntityRenderer;
import myau.mixin.IAccessorRenderManager;
import myau.module.Module;
import myau.module.modules.HUD;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import myau.util.ColorUtil;
import myau.util.RenderUtil;
import myau.util.TeamUtil;
import myau.util.shader.GlowShader;
import myau.util.shader.OutlineShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class ESP
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final OutlineShader outlineRenderer = new OutlineShader();
    private final GlowShader glowShader = new GlowShader();
    private Framebuffer framebuffer = null;
    private boolean outline = true;
    private boolean glow = true;
    public final ModeProperty mode = new ModeProperty("mode", 2, new String[]{"NONE", "2D", "3D", "OUTLINE", "FAKECORNER", "FAKE2D"});
    public final ModeProperty color = new ModeProperty("color", 0, new String[]{"DEFAULT", "TEAMS", "HUD"});
    public final ModeProperty healthBar = new ModeProperty("health-bar", 0, new String[]{"NONE", "2D", "RAVEN"});
    public final BooleanProperty players = new BooleanProperty("players", true);
    public final BooleanProperty friends = new BooleanProperty("friends", true);
    public final BooleanProperty enemies = new BooleanProperty("enemies", true);
    public final BooleanProperty self = new BooleanProperty("self", false);
    public final BooleanProperty bots = new BooleanProperty("bots", false);

    private boolean shouldRenderPlayer(EntityPlayer entityPlayer) {
        if (entityPlayer.field_70725_aQ > 0) {
            return false;
        }
        if (mc.func_175606_aa().func_70032_d((Entity)entityPlayer) > 512.0f) {
            return false;
        }
        if (!entityPlayer.field_70158_ak && !RenderUtil.isInViewFrustum(entityPlayer.func_174813_aQ(), 0.1f)) {
            return false;
        }
        if (entityPlayer != ESP.mc.field_71439_g && entityPlayer != mc.func_175606_aa()) {
            if (TeamUtil.isBot(entityPlayer)) {
                return (Boolean)this.bots.getValue();
            }
            if (TeamUtil.isFriend(entityPlayer)) {
                return (Boolean)this.friends.getValue();
            }
            return TeamUtil.isTarget(entityPlayer) ? ((Boolean)this.enemies.getValue()).booleanValue() : ((Boolean)this.players.getValue()).booleanValue();
        }
        return (Boolean)this.self.getValue() != false && ESP.mc.field_71474_y.field_74320_O != 0;
    }

    private Color getEntityColor(EntityPlayer entityPlayer) {
        if (TeamUtil.isFriend(entityPlayer)) {
            return Myau.friendManager.getColor();
        }
        if (TeamUtil.isTarget(entityPlayer)) {
            return Myau.targetManager.getColor();
        }
        switch ((Integer)this.color.getValue()) {
            case 0: {
                return TeamUtil.getTeamColor(entityPlayer, 1.0f);
            }
            case 1: {
                int teamColor = TeamUtil.isSameTeam(entityPlayer) ? ChatColors.BLUE.toAwtColor() : ChatColors.RED.toAwtColor();
                return new Color(teamColor);
            }
            case 2: {
                int hudColor = ((HUD)Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis()).getRGB();
                return new Color(hudColor);
            }
        }
        return new Color(-1);
    }

    public ESP() {
        super("ESP", false);
    }

    public boolean isOutlineEnabled() {
        return this.outline;
    }

    public boolean isGlowEnabled() {
        return this.glow;
    }

    @EventTarget
    public void onResize(ResizeEvent event) {
        if (this.framebuffer != null) {
            this.framebuffer.func_147608_a();
        }
        this.framebuffer = new Framebuffer(ESP.mc.field_71443_c, ESP.mc.field_71440_d, false);
    }

    @EventTarget(value=1)
    public void onRender(Render2DEvent event) {
        block10: {
            List renderedEntities;
            block11: {
                if (!this.isEnabled()) break block10;
                if ((Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 3) break block11;
                if ((Integer)this.healthBar.getValue() != 1) break block10;
            }
            if (!(renderedEntities = TeamUtil.getLoadedEntitiesSorted().stream().filter(entity -> entity instanceof EntityPlayer && this.shouldRenderPlayer((EntityPlayer)entity)).map(EntityPlayer.class::cast).collect(Collectors.toList())).isEmpty()) {
                if ((Integer)this.mode.getValue() == 3) {
                    GlStateManager.func_179094_E();
                    GlStateManager.func_179123_a();
                    if (this.framebuffer == null) {
                        this.framebuffer = new Framebuffer(ESP.mc.field_71443_c, ESP.mc.field_71440_d, false);
                    }
                    this.framebuffer.func_147610_a(false);
                    ((IAccessorEntityRenderer)ESP.mc.field_71460_t).callSetupCameraTransform(event.getPartialTicks(), 0);
                    boolean shadow = ESP.mc.field_71474_y.field_181151_V;
                    ESP.mc.field_71474_y.field_181151_V = false;
                    this.outline = false;
                    this.glow = false;
                    this.glowShader.use();
                    for (EntityPlayer player : renderedEntities) {
                        Color entityColor = this.getEntityColor(player);
                        this.glowShader.W(entityColor);
                        boolean invisible = player.func_82150_aj();
                        player.func_82142_c(false);
                        mc.func_175598_ae().func_147936_a((Entity)player, event.getPartialTicks(), true);
                        player.func_82142_c(invisible);
                    }
                    this.glowShader.stop();
                    this.glow = true;
                    this.outline = true;
                    ESP.mc.field_71474_y.field_181151_V = shadow;
                    ESP.mc.field_71460_t.func_175072_h();
                    ESP.mc.field_71460_t.func_78478_c();
                    mc.func_147110_a().func_147610_a(false);
                    this.outlineRenderer.use();
                    RenderUtil.drawFramebuffer(this.framebuffer);
                    this.outlineRenderer.stop();
                    this.framebuffer.func_147614_f();
                    mc.func_147110_a().func_147610_a(false);
                    GlStateManager.func_179099_b();
                    GlStateManager.func_179121_F();
                }
                if ((Integer)this.mode.getValue() == 1 || (Integer)this.healthBar.getValue() == 1) {
                    RenderUtil.enableRenderState();
                    double scaleFactor = new ScaledResolution(mc).func_78325_e();
                    double scale = scaleFactor / Math.pow(scaleFactor, 2.0);
                    GlStateManager.func_179094_E();
                    GlStateManager.func_179139_a((double)scale, (double)scale, (double)scale);
                    for (EntityPlayer player : renderedEntities) {
                        ((IAccessorEntityRenderer)ESP.mc.field_71460_t).callSetupCameraTransform(event.getPartialTicks(), 0);
                        Vector4d screenPosition = RenderUtil.projectToScreen((Entity)player, scaleFactor);
                        ESP.mc.field_71460_t.func_78478_c();
                        if (screenPosition == null) continue;
                        float x = (float)screenPosition.x;
                        float y = (float)screenPosition.y;
                        float z = (float)screenPosition.z;
                        float w = (float)screenPosition.w;
                        if ((Integer)this.mode.getValue() == 1) {
                            int color = this.getEntityColor(player).getRGB();
                            RenderUtil.drawOutlineRect(x, y, z, w, 3.0f, 0, (color & 0xFCFCFC) >> 2 | color & 0xFF000000);
                            RenderUtil.drawOutlineRect(x, y, z, w, 1.5f, 0, color);
                        }
                        if ((Integer)this.healthBar.getValue() != 1) continue;
                        float heal = player.func_110143_aJ() + player.func_110139_bj();
                        float percent = Math.min(Math.max(heal / player.func_110138_aP(), 0.0f), 1.0f);
                        float box = (z - x) * 0.08f;
                        Color healthColor = ColorUtil.getHealthBlend(percent);
                        RenderUtil.drawLine(x - box, y, x - box, w, 3.0f, ColorUtil.darker(healthColor, 0.2f).getRGB());
                        RenderUtil.drawLine(x - box, w, x - box, w + (y - w) * percent, 1.5f, healthColor.getRGB());
                    }
                    GlStateManager.func_179121_F();
                    RenderUtil.disableRenderState();
                }
            }
        }
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (this.isEnabled() && ((Integer)this.mode.getValue() == 2 || (Integer)this.mode.getValue() == 4 || (Integer)this.mode.getValue() == 5 || (Integer)this.healthBar.getValue() == 2)) {
            RenderUtil.enableRenderState();
            for (EntityPlayer player : TeamUtil.getLoadedEntitiesSorted().stream().filter(entity -> entity instanceof EntityPlayer && this.shouldRenderPlayer((EntityPlayer)entity)).map(EntityPlayer.class::cast).collect(Collectors.toList())) {
                Color color;
                if (!player.field_70158_ak && !RenderUtil.isInViewFrustum(player.func_174813_aQ(), 0.1f)) continue;
                if ((Integer)this.mode.getValue() == 2) {
                    color = this.getEntityColor(player);
                    RenderUtil.drawEntityBoundingBox((Entity)player, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 1.5f, 0.1f);
                    GlStateManager.func_179117_G();
                }
                if ((Integer)this.mode.getValue() == 4) {
                    color = this.getEntityColor(player);
                    RenderUtil.drawCornerESP(player, (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
                }
                if ((Integer)this.mode.getValue() == 5) {
                    color = this.getEntityColor(player);
                    RenderUtil.drawFake2DESP(player, (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
                }
                if ((Integer)this.healthBar.getValue() != 2) continue;
                double x = RenderUtil.lerpDouble(player.field_70165_t, player.field_70142_S, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX();
                double y = RenderUtil.lerpDouble(player.field_70163_u, player.field_70137_T, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY() - (double)0.1f;
                double z = RenderUtil.lerpDouble(player.field_70161_v, player.field_70136_U, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ();
                GlStateManager.func_179094_E();
                GlStateManager.func_179137_b((double)x, (double)y, (double)z);
                GlStateManager.func_179114_b((float)(ESP.mc.func_175598_ae().field_78735_i * -1.0f), (float)0.0f, (float)1.0f, (float)0.0f);
                float heal = player.func_110143_aJ() + player.func_110139_bj();
                float percent = Math.min(Math.max(heal / player.func_110138_aP(), 0.0f), 1.0f);
                Color healthColor = ColorUtil.getHealthBlend(percent);
                float height = player.field_70131_O + 0.2f;
                RenderUtil.drawRect3D(0.57250005f, -0.027500002f, 0.7275f, height + 0.027500002f, Color.black.getRGB());
                RenderUtil.drawRect3D(0.6f, 0.0f, 0.70000005f, height, Color.darkGray.getRGB());
                RenderUtil.drawRect3D(0.6f, 0.0f, 0.70000005f, height * percent, healthColor.getRGB());
                GlStateManager.func_179121_F();
            }
            RenderUtil.disableRenderState();
        }
    }
}

package myau.module.modules;

import java.awt.Color;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import myau.Myau;
import myau.enums.ChatColors;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.events.Render2DEvent;
import myau.module.Module;
import myau.module.modules.HUD;
import myau.module.modules.KillAura;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.ColorUtil;
import myau.util.RenderUtil;
import myau.util.TeamUtil;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TargetHUD
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final DecimalFormat healthFormat = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
    private static final DecimalFormat diffFormat = new DecimalFormat("+0.0;-0.0", new DecimalFormatSymbols(Locale.US));
    private final TimerUtil lastAttackTimer = new TimerUtil();
    private final TimerUtil animTimer = new TimerUtil();
    private final TimerUtil scaleAnimTimer = new TimerUtil();
    private final TimerUtil targetLostTimer = new TimerUtil();
    private EntityLivingBase lastTarget = null;
    private EntityLivingBase target = null;
    private ResourceLocation headTexture = null;
    private float oldHealth = 0.0f;
    private float newHealth = 0.0f;
    private float maxHealth = 0.0f;
    private float scaleAnimation = 0.0f;
    private boolean isAnimatingOut = false;
    private boolean targetLost = false;
    private EntityLivingBase lastRenderTarget = null;
    public final ModeProperty mode = new ModeProperty("Mode", 1, new String[]{"Myau", "Exhibition"});
    public final ModeProperty color = new ModeProperty("Color", 0, new String[]{"Default", "Hud"});
    public final ModeProperty posX = new ModeProperty("PositionX", 1, new String[]{"Left", "Middle", "Right"});
    public final ModeProperty posY = new ModeProperty("PositionY", 1, new String[]{"Top", "Middle", "Bottom"});
    public final FloatProperty scale = new FloatProperty("Scale", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(1.5f));
    public final IntProperty offX = new IntProperty("OffsetX", 0, -255, 255);
    public final IntProperty offY = new IntProperty("OffsetY", 40, -255, 255);
    public final PercentProperty background = new PercentProperty("Background", 25);
    public final BooleanProperty head = new BooleanProperty("Head", true);
    public final BooleanProperty indicator = new BooleanProperty("Indicator", true);
    public final BooleanProperty outline = new BooleanProperty("Outline", false);
    public final BooleanProperty animations = new BooleanProperty("Animations", true);
    public final BooleanProperty shadow = new BooleanProperty("Shadow", true);
    public final BooleanProperty kaOnly = new BooleanProperty("KaOnly", true);
    public final BooleanProperty chatPreview = new BooleanProperty("ChatPreview", false);

    public TargetHUD() {
        super("TargetHUD", false, true);
    }

    private EntityLivingBase resolveTarget() {
        KillAura killAura = (KillAura)Myau.moduleManager.getModule("KillAura");
        if (killAura != null && killAura.isEnabled() && killAura.isAttackAllowed() && TeamUtil.isEntityLoaded((Entity)killAura.getTarget())) {
            return killAura.getTarget();
        }
        if (!((Boolean)this.kaOnly.getValue()).booleanValue() && !this.lastAttackTimer.hasTimeElapsed(1500L) && TeamUtil.isEntityLoaded((Entity)this.lastTarget)) {
            return this.lastTarget;
        }
        return (Boolean)this.chatPreview.getValue() != false && TargetHUD.mc.field_71462_r instanceof GuiChat ? TargetHUD.mc.field_71439_g : null;
    }

    private ResourceLocation getSkin(EntityLivingBase entityLivingBase) {
        NetworkPlayerInfo playerInfo;
        if (entityLivingBase instanceof EntityPlayer && (playerInfo = mc.func_147114_u().func_175104_a(entityLivingBase.func_70005_c_())) != null) {
            return playerInfo.func_178837_g();
        }
        return null;
    }

    private static int getEntityId(C02PacketUseEntity packet) {
        try {
            Field field = C02PacketUseEntity.class.getDeclaredField("entityId");
            field.setAccessible(true);
            return field.getInt(packet);
        }
        catch (Exception e) {
            return -1;
        }
    }

    private Color getTargetColor(EntityLivingBase entityLivingBase) {
        if (entityLivingBase instanceof EntityPlayer) {
            if (TeamUtil.isFriend((EntityPlayer)entityLivingBase)) {
                return Myau.friendManager.getColor();
            }
            if (TeamUtil.isTarget((EntityPlayer)entityLivingBase)) {
                return Myau.targetManager.getColor();
            }
        }
        switch ((Integer)this.color.getValue()) {
            case 0: {
                if (!(entityLivingBase instanceof EntityPlayer)) {
                    return new Color(-1);
                }
                return TeamUtil.getTeamColor((EntityPlayer)entityLivingBase, 1.0f);
            }
            case 1: {
                HUD hud = (HUD)Myau.moduleManager.getModule("HUD");
                if (hud != null) {
                    return hud.getColor(System.currentTimeMillis());
                }
                return Color.CYAN;
            }
        }
        return new Color(-1);
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (this.isEnabled() && TargetHUD.mc.field_71439_g != null) {
            EntityLivingBase currentTarget = this.resolveTarget();
            if (currentTarget != null) {
                this.targetLost = false;
                if (!this.isAnimatingOut) {
                    if (this.target != currentTarget) {
                        float heal;
                        if (this.target == null) {
                            this.scaleAnimTimer.reset();
                        }
                        this.target = currentTarget;
                        this.lastRenderTarget = currentTarget;
                        this.headTexture = null;
                        this.animTimer.setTime();
                        this.oldHealth = heal = this.target.func_110143_aJ() / 2.0f + this.target.func_110139_bj() / 2.0f;
                        this.newHealth = heal;
                    }
                    this.updateScaleAnimation();
                    switch ((Integer)this.mode.getValue()) {
                        case 0: {
                            this.renderMyauMode();
                            break;
                        }
                        case 1: {
                            this.renderExhibitionMode();
                        }
                    }
                }
            } else if (this.target != null) {
                if (!this.targetLost) {
                    this.targetLost = true;
                    this.targetLostTimer.reset();
                }
                if (this.targetLostTimer.hasTimeElapsed(50L)) {
                    this.isAnimatingOut = true;
                    this.scaleAnimTimer.reset();
                    this.target = null;
                } else {
                    switch ((Integer)this.mode.getValue()) {
                        case 0: {
                            this.renderMyauMode();
                            break;
                        }
                        case 1: {
                            this.renderExhibitionMode();
                        }
                    }
                }
            }
            if (this.isAnimatingOut) {
                this.updateScaleAnimation();
                if (this.scaleAnimation > 0.0f && this.lastRenderTarget != null) {
                    this.target = this.lastRenderTarget;
                    switch ((Integer)this.mode.getValue()) {
                        case 0: {
                            this.renderMyauMode();
                            break;
                        }
                        case 1: {
                            this.renderExhibitionMode();
                        }
                    }
                    this.target = null;
                } else if (this.scaleAnimation <= 0.0f) {
                    this.isAnimatingOut = false;
                    this.targetLost = false;
                    this.lastRenderTarget = null;
                }
            }
        }
    }

    private void updateScaleAnimation() {
        long elapsedTime = this.scaleAnimTimer.getElapsedTime();
        float animationDuration = 200.0f;
        this.scaleAnimation = !this.isAnimatingOut ? Math.min((float)elapsedTime / animationDuration, 1.0f) : Math.max(1.0f - (float)elapsedTime / animationDuration, 0.0f);
        this.scaleAnimation = this.easeOutQuart(this.scaleAnimation);
    }

    private float easeOutQuart(float t) {
        return 1.0f - (float)Math.pow(1.0f - t, 4.0);
    }

    private void renderMyauMode() {
        ResourceLocation resourceLocation;
        float health = (TargetHUD.mc.field_71439_g.func_110143_aJ() + TargetHUD.mc.field_71439_g.func_110139_bj()) / 2.0f;
        float abs = this.target.func_110139_bj() / 2.0f;
        float heal = this.target.func_110143_aJ() / 2.0f + abs;
        if (!((Boolean)this.animations.getValue()).booleanValue() || this.animTimer.hasTimeElapsed(150L)) {
            this.oldHealth = this.newHealth;
            this.newHealth = heal;
            this.maxHealth = this.target.func_110138_aP() / 2.0f;
            if (this.oldHealth != this.newHealth) {
                this.animTimer.reset();
            }
        }
        if ((resourceLocation = this.getSkin(this.target)) != null) {
            this.headTexture = resourceLocation;
        }
        float elapsedTime = Math.min(Math.max(this.animTimer.getElapsedTime(), 0L), 150L);
        float healthRatio = Math.min(Math.max(RenderUtil.lerpFloat(this.newHealth, this.oldHealth, elapsedTime / 150.0f) / this.maxHealth, 0.0f), 1.0f);
        Color targetColor = this.getTargetColor(this.target);
        Color healthBarColor = (Integer)this.color.getValue() == 0 ? ColorUtil.getHealthBlend(healthRatio) : targetColor;
        float healthDeltaRatio = Math.min(Math.max((health - heal + 1.0f) / 2.0f, 0.0f), 1.0f);
        Color healthDeltaColor = ColorUtil.getHealthBlend(healthDeltaRatio);
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        String targetNameText = ChatColors.formatColor(String.format("&r%s&r", TeamUtil.stripName((Entity)this.target)));
        int targetNameWidth = TargetHUD.mc.field_71466_p.func_78256_a(targetNameText);
        String healthText = ChatColors.formatColor(String.format("&r&f%s%s\u2764&r", healthFormat.format(heal), abs > 0.0f ? "&6" : "&c"));
        int healthTextWidth = TargetHUD.mc.field_71466_p.func_78256_a(healthText);
        String statusText = ChatColors.formatColor(String.format("&r&l%s&r", heal == health ? "D" : (heal < health ? "W" : "L")));
        int statusTextWidth = TargetHUD.mc.field_71466_p.func_78256_a(statusText);
        String healthDiffText = ChatColors.formatColor(String.format("&r%s&r", heal == health ? "0.0" : diffFormat.format(health - heal)));
        int healthDiffWidth = TargetHUD.mc.field_71466_p.func_78256_a(healthDiffText);
        float barContentWidth = Math.max((float)targetNameWidth + ((Boolean)this.indicator.getValue() != false ? 2.0f + (float)statusTextWidth + 2.0f : 0.0f), (float)healthTextWidth + ((Boolean)this.indicator.getValue() != false ? 2.0f + (float)healthDiffWidth + 2.0f : 0.0f));
        float headIconOffset = (Boolean)this.head.getValue() != false && this.headTexture != null ? 25.0f : 0.0f;
        float barTotalWidth = Math.max(headIconOffset + 70.0f, headIconOffset + 2.0f + barContentWidth + 2.0f);
        float posX = ((Integer)this.offX.getValue()).floatValue() / ((Float)this.scale.getValue()).floatValue();
        switch ((Integer)this.posX.getValue()) {
            case 1: {
                posX += (float)scaledResolution.func_78326_a() / ((Float)this.scale.getValue()).floatValue() / 2.0f - barTotalWidth / 2.0f;
                break;
            }
            case 2: {
                posX *= -1.0f;
                posX += (float)scaledResolution.func_78326_a() / ((Float)this.scale.getValue()).floatValue() - barTotalWidth;
            }
        }
        float posY = ((Integer)this.offY.getValue()).floatValue() / ((Float)this.scale.getValue()).floatValue();
        switch ((Integer)this.posY.getValue()) {
            case 1: {
                posY += (float)scaledResolution.func_78328_b() / ((Float)this.scale.getValue()).floatValue() / 2.0f - 13.5f;
                break;
            }
            case 2: {
                posY *= -1.0f;
                posY += (float)scaledResolution.func_78328_b() / ((Float)this.scale.getValue()).floatValue() - 27.0f;
            }
        }
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)(posX + barTotalWidth / 2.0f), (float)(posY + 13.5f), (float)-450.0f);
        float finalScale = ((Float)this.scale.getValue()).floatValue() * this.scaleAnimation;
        GlStateManager.func_179152_a((float)finalScale, (float)finalScale, (float)0.0f);
        GlStateManager.func_179109_b((float)(-barTotalWidth / 2.0f), (float)-13.5f, (float)0.0f);
        RenderUtil.enableRenderState();
        int backgroundColor = new Color(0.0f, 0.0f, 0.0f, (float)((Integer)this.background.getValue()).intValue() / 100.0f).getRGB();
        int outlineColor = (Boolean)this.outline.getValue() != false ? targetColor.getRGB() : new Color(0, 0, 0, 0).getRGB();
        RenderUtil.drawOutlineRect(0.0f, 0.0f, barTotalWidth, 27.0f, 1.5f, backgroundColor, outlineColor);
        RenderUtil.drawRect(headIconOffset + 2.0f, 22.0f, barTotalWidth - 2.0f, 25.0f, ColorUtil.darker(healthBarColor, 0.2f).getRGB());
        RenderUtil.drawRect(headIconOffset + 2.0f, 22.0f, headIconOffset + 2.0f + healthRatio * (barTotalWidth - 2.0f - headIconOffset - 2.0f), 25.0f, healthBarColor.getRGB());
        RenderUtil.disableRenderState();
        GlStateManager.func_179097_i();
        GlStateManager.func_179147_l();
        GlStateManager.func_179112_b((int)770, (int)771);
        TargetHUD.mc.field_71466_p.func_175065_a(targetNameText, headIconOffset + 2.0f, 2.0f, -1, ((Boolean)this.shadow.getValue()).booleanValue());
        TargetHUD.mc.field_71466_p.func_175065_a(healthText, headIconOffset + 2.0f, 12.0f, -1, ((Boolean)this.shadow.getValue()).booleanValue());
        if (((Boolean)this.indicator.getValue()).booleanValue()) {
            TargetHUD.mc.field_71466_p.func_175065_a(statusText, barTotalWidth - 2.0f - (float)statusTextWidth, 2.0f, healthDeltaColor.getRGB(), ((Boolean)this.shadow.getValue()).booleanValue());
            TargetHUD.mc.field_71466_p.func_175065_a(healthDiffText, barTotalWidth - 2.0f - (float)healthDiffWidth, 12.0f, ColorUtil.darker(healthDeltaColor, 0.8f).getRGB(), ((Boolean)this.shadow.getValue()).booleanValue());
        }
        if (((Boolean)this.head.getValue()).booleanValue() && this.headTexture != null) {
            GlStateManager.func_179124_c((float)1.0f, (float)1.0f, (float)1.0f);
            mc.func_110434_K().func_110577_a(this.headTexture);
            Gui.func_152125_a((int)2, (int)2, (float)8.0f, (float)8.0f, (int)8, (int)8, (int)23, (int)23, (float)64.0f, (float)64.0f);
            Gui.func_152125_a((int)2, (int)2, (float)40.0f, (float)8.0f, (int)8, (int)8, (int)23, (int)23, (float)64.0f, (float)64.0f);
            GlStateManager.func_179124_c((float)1.0f, (float)1.0f, (float)1.0f);
        }
        GlStateManager.func_179084_k();
        GlStateManager.func_179126_j();
        GlStateManager.func_179121_F();
    }

    private void renderExhibitionMode() {
        ScaledResolution resolution = new ScaledResolution(mc);
        double boxWidth = 40 + TargetHUD.mc.field_71466_p.func_78256_a(this.target.func_70005_c_());
        double renderWidth = Math.max(boxWidth, 120.0);
        float posX = ((Integer)this.offX.getValue()).floatValue() / ((Float)this.scale.getValue()).floatValue();
        switch ((Integer)this.posX.getValue()) {
            case 1: {
                posX += (float)resolution.func_78326_a() / ((Float)this.scale.getValue()).floatValue() / 2.0f - (float)renderWidth / 2.0f;
                break;
            }
            case 2: {
                posX *= -1.0f;
                posX += (float)resolution.func_78326_a() / ((Float)this.scale.getValue()).floatValue() - (float)renderWidth;
            }
        }
        float posY = ((Integer)this.offY.getValue()).floatValue() / ((Float)this.scale.getValue()).floatValue();
        switch ((Integer)this.posY.getValue()) {
            case 1: {
                posY += (float)resolution.func_78328_b() / ((Float)this.scale.getValue()).floatValue() / 2.0f - 20.0f;
                break;
            }
            case 2: {
                posY *= -1.0f;
                posY += (float)resolution.func_78328_b() / ((Float)this.scale.getValue()).floatValue() - 40.0f;
            }
        }
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)(posX + (float)renderWidth / 2.0f), (float)(posY + 20.0f), (float)0.0f);
        float finalScale = ((Float)this.scale.getValue()).floatValue() * this.scaleAnimation;
        GlStateManager.func_179152_a((float)finalScale, (float)finalScale, (float)0.0f);
        GlStateManager.func_179109_b((float)(-((float)renderWidth) / 2.0f), (float)-20.0f, (float)0.0f);
        this.drawExhibitionBorderedRect(-2.5f, -2.5f, (float)renderWidth + 2.5f, 42.5f, 0.5f, this.getExhibitionColor(60), this.getExhibitionColor(10));
        this.drawExhibitionBorderedRect(-1.5f, -1.5f, (float)renderWidth + 1.5f, 41.5f, 1.5f, this.getExhibitionColor(60), this.getExhibitionColor(40));
        this.drawExhibitionBorderedRect(0.0f, 0.0f, (float)renderWidth, 40.0f, 0.5f, this.getExhibitionColor(22), this.getExhibitionColor(60));
        this.drawExhibitionBorderedRect(2.0f, 2.0f, 38.0f, 38.0f, 0.5f, this.getExhibitionColor(0, 0), this.getExhibitionColor(10));
        this.drawExhibitionBorderedRect(2.5f, 2.5f, 37.5f, 37.5f, 0.5f, this.getExhibitionColor(17), this.getExhibitionColor(48));
        GlStateManager.func_179094_E();
        int factor = resolution.func_78325_e();
        GL11.glScissor((int)((int)((posX + 3.0f) * (float)factor)), (int)((int)(((float)resolution.func_78328_b() - (posY + 37.0f)) * (float)factor)), (int)(34 * factor), (int)(34 * factor));
        GL11.glEnable((int)3089);
        this.drawEntityOnScreen(this.target);
        GL11.glDisable((int)3089);
        GlStateManager.func_179121_F();
        GlStateManager.func_179109_b((float)2.0f, (float)0.0f, (float)0.0f);
        GlStateManager.func_179094_E();
        GlStateManager.func_179152_a((float)0.8f, (float)0.8f, (float)0.8f);
        TargetHUD.mc.field_71466_p.func_175065_a(this.target.func_70005_c_(), 46.0f, 4.0f, -1, ((Boolean)this.shadow.getValue()).booleanValue());
        GlStateManager.func_179121_F();
        float health = this.target.func_110143_aJ();
        float absorption = this.target.func_110139_bj();
        float progress = health / (this.target.func_110138_aP() + absorption);
        float realHealthProgress = health / this.target.func_110138_aP();
        Color customColor = health >= 0.0f ? this.blendColors(new float[]{0.0f, 0.5f, 1.0f}, new Color[]{Color.RED, Color.YELLOW, Color.GREEN}, realHealthProgress).brighter() : Color.RED;
        double width = Math.min(TargetHUD.mc.field_71466_p.func_78256_a(this.target.func_70005_c_()), 60);
        width = this.getIncremental(width, 10.0);
        if (width < 60.0) {
            width = 60.0;
        }
        double healthLocation = width * (double)progress;
        this.drawExhibitionBorderedRect(37.0f, 12.0f, 39.0f + (float)width, 16.0f, 0.5f, this.getExhibitionColor(0, 0), this.getExhibitionColor(0));
        this.drawExhibitionRect(38.0f + (float)healthLocation + 0.5f, 12.5f, 38.0f + (float)width + 0.5f, 15.5f, this.getExhibitionColorOpacity(customColor.getRGB(), 35));
        this.drawExhibitionRect(37.5f, 12.5f, 38.0f + (float)healthLocation + 0.5f, 15.5f, customColor.getRGB());
        if (absorption > 0.0f) {
            double absorptionDifferent = width * (double)(absorption / (this.target.func_110138_aP() + absorption));
            this.drawExhibitionRect(38.0f + (float)healthLocation + 0.5f, 12.5f, 38.0f + (float)healthLocation + 0.5f + (float)absorptionDifferent, 15.5f, -2130728448);
        }
        for (int i = 1; i < 10; ++i) {
            double dThing = width / 10.0 * (double)i;
            this.drawExhibitionRect(38.0f + (float)dThing, 12.0f, 38.0f + (float)dThing + 0.5f, 16.0f, this.getExhibitionColor(0));
        }
        String str = "HP: " + (int)health + " | Dist: " + (int)TargetHUD.mc.field_71439_g.func_70032_d((Entity)this.target);
        GlStateManager.func_179094_E();
        GlStateManager.func_179152_a((float)0.7f, (float)0.7f, (float)0.7f);
        TargetHUD.mc.field_71466_p.func_175065_a(str, 53.0f, 26.0f, -1, ((Boolean)this.shadow.getValue()).booleanValue());
        GlStateManager.func_179121_F();
        if (this.target instanceof EntityPlayer) {
            EntityPlayer targetPlayer = (EntityPlayer)this.target;
            GL11.glPushMatrix();
            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            int split = 20;
            for (int index = 3; index >= 0; --index) {
                ItemStack armor = targetPlayer.field_71071_by.field_70460_b[index];
                if (armor == null) continue;
                items.add(armor);
            }
            int yOffset = 23;
            if (targetPlayer.func_71045_bC() != null) {
                items.add(targetPlayer.func_71045_bC());
            }
            RenderHelper.func_74520_c();
            for (ItemStack itemStack : items) {
                int pLevel;
                if (TargetHUD.mc.field_71441_e != null) {
                    split += 16;
                }
                GlStateManager.func_179094_E();
                GlStateManager.func_179118_c();
                GlStateManager.func_179086_m((int)256);
                TargetHUD.mc.func_175599_af().field_77023_b = -150.0f;
                mc.func_175599_af().func_180450_b(itemStack, split, yOffset);
                mc.func_175599_af().func_175030_a(TargetHUD.mc.field_71466_p, itemStack, split, yOffset);
                TargetHUD.mc.func_175599_af().field_77023_b = 0.0f;
                int renderY = yOffset;
                if (itemStack.func_77973_b() instanceof ItemSword) {
                    int sLevel = EnchantmentHelper.func_77506_a((int)Enchantment.field_180314_l.field_77352_x, (ItemStack)itemStack);
                    int fLevel = EnchantmentHelper.func_77506_a((int)Enchantment.field_77334_n.field_77352_x, (ItemStack)itemStack);
                    if (sLevel > 0) {
                        this.drawEnchantTag("S" + this.getSharpnessColor(sLevel) + sLevel, split, renderY);
                        renderY = (int)((float)renderY + 4.5f);
                    }
                    if (fLevel > 0) {
                        this.drawEnchantTag("F" + this.getFireAspectColor(fLevel) + fLevel, split, renderY);
                        renderY = (int)((float)renderY + 4.5f);
                    }
                } else if (itemStack.func_77973_b() instanceof ItemArmor && (pLevel = EnchantmentHelper.func_77506_a((int)Enchantment.field_180310_c.field_77352_x, (ItemStack)itemStack)) > 0) {
                    this.drawEnchantTag("P" + this.getProtectionColor(pLevel) + pLevel, split, renderY);
                    renderY = (int)((float)renderY + 4.5f);
                }
                GlStateManager.func_179084_k();
                GlStateManager.func_179140_f();
                GlStateManager.func_179141_d();
                GlStateManager.func_179121_F();
            }
            RenderHelper.func_74518_a();
            GL11.glPopMatrix();
        }
        GlStateManager.func_179121_F();
    }

    private double getIncremental(double value, double increment) {
        return Math.ceil(value / increment) * increment;
    }

    private void drawEntityOnScreen(EntityLivingBase ent) {
        GlStateManager.func_179142_g();
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)20.0f, (float)36.0f, (float)50.0f);
        float largestSize = Math.max(ent.field_70131_O, ent.field_70130_N);
        float relativeScale = Math.max(largestSize / 1.8f, 1.0f);
        GlStateManager.func_179152_a((float)(-16.0f / relativeScale), (float)(16.0f / relativeScale), (float)(16.0f / relativeScale));
        GlStateManager.func_179114_b((float)180.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        GlStateManager.func_179114_b((float)135.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        RenderHelper.func_74519_b();
        GlStateManager.func_179114_b((float)-135.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GlStateManager.func_179114_b((float)(-((float)Math.atan(0.425f)) * 20.0f), (float)1.0f, (float)0.0f, (float)0.0f);
        GlStateManager.func_179109_b((float)0.0f, (float)0.0f, (float)0.0f);
        RenderManager renderManager = mc.func_175598_ae();
        renderManager.func_178631_a(180.0f);
        renderManager.func_178633_a(false);
        renderManager.func_147940_a((Entity)ent, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        renderManager.func_178633_a(true);
        GlStateManager.func_179121_F();
        RenderHelper.func_74518_a();
        GlStateManager.func_179101_C();
        GlStateManager.func_179138_g((int)OpenGlHelper.field_77476_b);
        GlStateManager.func_179090_x();
        GlStateManager.func_179138_g((int)OpenGlHelper.field_77478_a);
    }

    private Color blendColors(float[] fractions, Color[] colors, float progress) {
        if (fractions.length == colors.length) {
            int[] indicies = this.getFractionIndicies(fractions, progress);
            float[] range = new float[]{fractions[indicies[0]], fractions[indicies[1]]};
            Color[] colorRange = new Color[]{colors[indicies[0]], colors[indicies[1]]};
            float max = range[1] - range[0];
            float value = progress - range[0];
            float weight = value / max;
            return this.blend(colorRange[0], colorRange[1], 1.0f - weight);
        }
        return colors[0];
    }

    private int[] getFractionIndicies(float[] fractions, float progress) {
        int startPoint;
        int[] range = new int[2];
        for (startPoint = 0; startPoint < fractions.length && fractions[startPoint] <= progress; ++startPoint) {
        }
        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }
        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }

    private Color blend(Color color1, Color color2, double ratio) {
        float r = (float)ratio;
        float ir = 1.0f - r;
        float[] rgb1 = color1.getColorComponents(new float[3]);
        float[] rgb2 = color2.getColorComponents(new float[3]);
        return new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);
    }

    private void drawEnchantTag(String text, int x, float y) {
        GlStateManager.func_179094_E();
        GlStateManager.func_179097_i();
        GlStateManager.func_179152_a((float)0.5f, (float)0.5f, (float)0.5f);
        TargetHUD.mc.field_71466_p.func_175065_a(text, (float)(x * 2), y * 2.0f, -1, true);
        GlStateManager.func_179126_j();
        GlStateManager.func_179121_F();
    }

    private String getProtectionColor(int level) {
        switch (level) {
            case 1: {
                return "\u00a7a";
            }
            case 2: {
                return "\u00a79";
            }
            case 3: {
                return "\u00a7e";
            }
            case 4: {
                return "\u00a7c";
            }
        }
        return "\u00a7f";
    }

    private String getSharpnessColor(int level) {
        switch (level) {
            case 1: {
                return "\u00a7a";
            }
            case 2: {
                return "\u00a79";
            }
            case 3: {
                return "\u00a7e";
            }
            case 4: {
                return "\u00a76";
            }
            case 5: {
                return "\u00a7c";
            }
        }
        return "\u00a7f";
    }

    private String getFireAspectColor(int level) {
        switch (level) {
            case 1: {
                return "\u00a76";
            }
            case 2: {
                return "\u00a7c";
            }
        }
        return "\u00a7f";
    }

    private int getExhibitionColor(int brightness) {
        return this.getExhibitionColor(brightness, brightness, brightness, 255);
    }

    private int getExhibitionColor(int brightness, int alpha) {
        return this.getExhibitionColor(brightness, brightness, brightness, alpha);
    }

    private int getExhibitionColor(int red, int green, int blue) {
        return this.getExhibitionColor(red, green, blue, 255);
    }

    private int getExhibitionColor(int red, int green, int blue, int alpha) {
        int color = 0;
        color |= Math.max(0, Math.min(255, alpha)) << 24;
        color |= Math.max(0, Math.min(255, red)) << 16;
        color |= Math.max(0, Math.min(255, green)) << 8;
        return color |= Math.max(0, Math.min(255, blue));
    }

    private int getExhibitionColorOpacity(int color, int alpha) {
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        return this.getExhibitionColor(red, green, blue, Math.max(0, Math.min(255, alpha)));
    }

    private void drawExhibitionRect(float x1, float y1, float x2, float y2, int color) {
        RenderUtil.enableRenderState();
        RenderUtil.drawRect(x1, y1, x2, y2, color);
        RenderUtil.disableRenderState();
    }

    private void drawExhibitionBorderedRect(float x1, float y1, float x2, float y2, float borderWidth, int fillColor, int borderColor) {
        RenderUtil.enableRenderState();
        RenderUtil.drawRect(x1, y1, x2, y2, borderColor);
        RenderUtil.drawRect(x1 + borderWidth, y1 + borderWidth, x2 - borderWidth, y2 - borderWidth, fillColor);
        RenderUtil.disableRenderState();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getType() == EventType.SEND && event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet = (C02PacketUseEntity)event.getPacket();
            if (packet.func_149565_c() != C02PacketUseEntity.Action.ATTACK) {
                return;
            }
            int entityId = TargetHUD.getEntityId(packet);
            if (entityId == -1) {
                return;
            }
            Entity entity = TargetHUD.mc.field_71441_e.func_73045_a(entityId);
            if (entity instanceof EntityLivingBase) {
                if (entity instanceof EntityArmorStand) {
                    return;
                }
                this.lastAttackTimer.reset();
                this.lastTarget = (EntityLivingBase)entity;
            }
        }
    }
}

package myau.module.modules;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import myau.Myau;
import myau.enums.ChatColors;
import myau.event.EventTarget;
import myau.events.Render3DEvent;
import myau.mixin.IAccessorRenderManager;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.ColorUtil;
import myau.util.RenderUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

public class NameTags
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final DecimalFormat healthFormatter = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
    public final FloatProperty scale = new FloatProperty("scale", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(2.0f));
    public final BooleanProperty autoScale = new BooleanProperty("auto-scale", true);
    public final PercentProperty backgroundOpacity = new PercentProperty("background", 25);
    public final BooleanProperty shadow = new BooleanProperty("shadow", true);
    public final ModeProperty distanceMode = new ModeProperty("distance", 0, new String[]{"NONE", "DEFAULT", "VAPE"});
    public final ModeProperty healthMode = new ModeProperty("health", 2, new String[]{"NONE", "HP", "HEARTS", "TAB"});
    public final BooleanProperty armor = new BooleanProperty("armor", true);
    public final BooleanProperty effects = new BooleanProperty("effects", true);
    public final BooleanProperty players = new BooleanProperty("players", true);
    public final BooleanProperty friends = new BooleanProperty("friends", true);
    public final BooleanProperty enemies = new BooleanProperty("enemies", true);
    public final BooleanProperty bossees = new BooleanProperty("bosses", false);
    public final BooleanProperty mobs = new BooleanProperty("mobs", false);
    public final BooleanProperty creepers = new BooleanProperty("creepers", false);
    public final BooleanProperty endermans = new BooleanProperty("endermen", false);
    public final BooleanProperty blazes = new BooleanProperty("blazes", false);
    public final BooleanProperty animals = new BooleanProperty("animals", false);
    public final BooleanProperty self = new BooleanProperty("self", false);
    public final BooleanProperty bots = new BooleanProperty("bots", false);

    public NameTags() {
        super("NameTags", false);
    }

    public boolean shouldRenderTags(EntityLivingBase entityLivingBase) {
        if (entityLivingBase.field_70725_aQ > 0) {
            return false;
        }
        if (mc.func_175606_aa().func_70032_d((Entity)entityLivingBase) > 512.0f) {
            return false;
        }
        if (entityLivingBase instanceof EntityPlayer) {
            if (entityLivingBase != NameTags.mc.field_71439_g && entityLivingBase != mc.func_175606_aa()) {
                if (TeamUtil.isBot((EntityPlayer)entityLivingBase)) {
                    return (Boolean)this.bots.getValue();
                }
                if (TeamUtil.isFriend((EntityPlayer)entityLivingBase)) {
                    return (Boolean)this.friends.getValue();
                }
                return TeamUtil.isTarget((EntityPlayer)entityLivingBase) ? ((Boolean)this.enemies.getValue()).booleanValue() : ((Boolean)this.players.getValue()).booleanValue();
            }
            return (Boolean)this.self.getValue() != false && NameTags.mc.field_71474_y.field_74320_O != 0;
        }
        if (entityLivingBase instanceof EntityDragon || entityLivingBase instanceof EntityWither) {
            return !entityLivingBase.func_82150_aj() && (Boolean)this.bossees.getValue() != false;
        }
        if (!(entityLivingBase instanceof EntityMob) && !(entityLivingBase instanceof EntitySlime)) {
            return (entityLivingBase instanceof EntityAnimal || entityLivingBase instanceof EntityBat || entityLivingBase instanceof EntitySquid || entityLivingBase instanceof EntityVillager) && (Boolean)this.animals.getValue() != false;
        }
        if (entityLivingBase instanceof EntityCreeper) {
            return (Boolean)this.creepers.getValue();
        }
        if (entityLivingBase instanceof EntityEnderman) {
            return (Boolean)this.endermans.getValue();
        }
        return entityLivingBase instanceof EntityBlaze ? ((Boolean)this.blazes.getValue()).booleanValue() : ((Boolean)this.mobs.getValue()).booleanValue();
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        for (Entity entity : TeamUtil.getLoadedEntitiesSorted()) {
            String teamName;
            if (!(entity instanceof EntityLivingBase) || !this.shouldRenderTags((EntityLivingBase)entity) || !entity.field_70158_ak && !RenderUtil.isInViewFrustum(entity.func_174813_aQ(), 10.0) || StringUtils.isBlank(EnumChatFormatting.func_110646_a((String)(teamName = TeamUtil.stripName(entity))))) continue;
            double x = RenderUtil.lerpDouble(entity.field_70165_t, entity.field_70142_S, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX();
            double y = RenderUtil.lerpDouble(entity.field_70163_u, entity.field_70137_T, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY() + (double)entity.func_70047_e();
            double z = RenderUtil.lerpDouble(entity.field_70161_v, entity.field_70136_U, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ();
            double distance = mc.func_175606_aa().func_70032_d(entity);
            GlStateManager.func_179094_E();
            GlStateManager.func_179137_b((double)x, (double)(y + (entity.func_70093_af() ? 0.225 : 0.4)), (double)z);
            GlStateManager.func_179114_b((float)(NameTags.mc.func_175598_ae().field_78735_i * -1.0f), (float)0.0f, (float)1.0f, (float)0.0f);
            float viewDir = NameTags.mc.field_71474_y.field_74320_O == 2 ? -1.0f : 1.0f;
            GlStateManager.func_179114_b((float)NameTags.mc.func_175598_ae().field_78732_j, (float)viewDir, (float)0.0f, (float)0.0f);
            double scale = Math.pow(Math.min(Math.max((Boolean)this.autoScale.getValue() != false ? distance : 0.0, 6.0), 128.0), 0.75) * 0.0075;
            GlStateManager.func_179139_a((double)(-scale * (double)((Float)this.scale.getValue()).floatValue()), (double)(-scale * (double)((Float)this.scale.getValue()).floatValue()), (double)1.0);
            if (entity instanceof EntityPlayer) {
                boolean isFriend = TeamUtil.isFriend((EntityPlayer)entity);
                boolean isEnemy = TeamUtil.isTarget((EntityPlayer)entity);
                if (isFriend || isEnemy) {
                    GlStateManager.func_179094_E();
                    GlStateManager.func_179152_a((float)1.5f, (float)1.5f, (float)1.0f);
                    String tagText = isFriend ? "&aTEAM" : "&cLOSER";
                    int tagColor = isFriend ? Color.GREEN.getRGB() : Color.RED.getRGB();
                    int tagWidth = NameTags.mc.field_71466_p.func_78256_a(tagText);
                    GlStateManager.func_179097_i();
                    NameTags.mc.field_71466_p.func_175065_a(tagText, (float)(-tagWidth) / 2.0f, (float)(-NameTags.mc.field_71466_p.field_78288_b * 3), tagColor, ((Boolean)this.shadow.getValue()).booleanValue());
                    GlStateManager.func_179126_j();
                    GlStateManager.func_179121_F();
                }
            }
            String distanceText = "";
            switch ((Integer)this.distanceMode.getValue()) {
                case 1: {
                    distanceText = String.format("&7%dm&r ", (int)distance);
                    break;
                }
                case 2: {
                    distanceText = String.format("&a[&f%d&a]&r ", (int)distance);
                }
            }
            float health = ((EntityLivingBase)entity).func_110143_aJ();
            float absorption = ((EntityLivingBase)entity).func_110139_bj();
            float max = ((EntityLivingBase)entity).func_110138_aP();
            float percent = Math.min(Math.max((health + absorption) / max, 0.0f), 1.0f);
            String healText = "";
            switch ((Integer)this.healthMode.getValue()) {
                case 1: {
                    healText = String.format(" %d%s", (int)health, absorption > 0.0f ? String.format(" &6%d&r", (int)absorption) : "&r");
                    break;
                }
                case 2: {
                    healText = String.format(" %s%s", healthFormatter.format((double)health / 2.0), absorption > 0.0f ? String.format(" &6%s&r", healthFormatter.format((double)absorption / 2.0)) : "&r");
                    break;
                }
                case 3: {
                    Score score;
                    ScoreObjective objective;
                    Scoreboard scoreboard;
                    if (!(entity instanceof EntityPlayer) || (scoreboard = NameTags.mc.field_71441_e.func_96441_U()) == null || (objective = scoreboard.func_96539_a(2)) == null || (score = scoreboard.func_96529_a(entity.func_70005_c_(), objective)) == null) break;
                    healText = String.format(" &e%d&r", score.func_96652_c());
                }
            }
            String color = ChatColors.formatColor(String.format("%s&f%s&r%s", distanceText, teamName, healText));
            int width = NameTags.mc.field_71466_p.func_78256_a(color);
            if ((Integer)this.backgroundOpacity.getValue() > 0) {
                Color textColor = !entity.func_70093_af() && !entity.func_82150_aj() ? new Color(0.0f, 0.0f, 0.0f, (float)((Integer)this.backgroundOpacity.getValue()).intValue() / 100.0f) : new Color(0.33f, 0.0f, 0.33f, (float)((Integer)this.backgroundOpacity.getValue()).intValue() / 100.0f);
                RenderUtil.enableRenderState();
                RenderUtil.drawRect((float)(-width) / 2.0f - 1.0f, (float)(-NameTags.mc.field_71466_p.field_78288_b) - 1.0f, (float)width / 2.0f + ((Boolean)this.shadow.getValue() != false ? 1.0f : 0.0f), (Boolean)this.shadow.getValue() != false ? 0.0f : -1.0f, textColor.getRGB());
                RenderUtil.disableRenderState();
            }
            GlStateManager.func_179097_i();
            NameTags.mc.field_71466_p.func_175065_a(color, (float)(-width) / 2.0f, (float)(-NameTags.mc.field_71466_p.field_78288_b), ColorUtil.getHealthBlend(percent).getRGB(), ((Boolean)this.shadow.getValue()).booleanValue());
            GlStateManager.func_179126_j();
            if (entity instanceof EntityPlayer) {
                float y2;
                List effects;
                int offset;
                int height = NameTags.mc.field_71466_p.field_78288_b + 2;
                if (((Boolean)this.armor.getValue()).booleanValue()) {
                    ArrayList<ItemStack> renderingItems = new ArrayList<ItemStack>();
                    for (int i = 4; i >= 0; --i) {
                        ItemStack itemStack = i == 0 ? ((EntityPlayer)entity).func_70694_bm() : ((EntityPlayer)entity).field_71071_by.field_70460_b[i - 1];
                        if (itemStack == null) continue;
                        renderingItems.add(itemStack);
                    }
                    if (!renderingItems.isEmpty()) {
                        offset = renderingItems.size() * -8;
                        for (int i = 0; i < renderingItems.size(); ++i) {
                            RenderUtil.renderItemInGUI((ItemStack)renderingItems.get(i), offset + i * 16, -height - 16);
                        }
                        height += 16;
                    }
                }
                if (((Boolean)this.effects.getValue()).booleanValue() && !(effects = ((EntityPlayer)entity).func_70651_bq().stream().filter(potionEffect -> Potion.field_76425_a[potionEffect.func_76456_a()].func_76400_d()).collect(Collectors.toList())).isEmpty()) {
                    GlStateManager.func_179094_E();
                    GlStateManager.func_179152_a((float)0.5f, (float)0.5f, (float)1.0f);
                    offset = effects.size() * -9;
                    for (int i = 0; i < effects.size(); ++i) {
                        RenderUtil.renderPotionEffect((PotionEffect)effects.get(i), offset + i * 18, -(height * 2) - 18);
                    }
                    GlStateManager.func_179121_F();
                }
                if (TeamUtil.isFriend((EntityPlayer)entity)) {
                    RenderUtil.enableRenderState();
                    float x1 = (float)(-width) / 2.0f - 1.0f;
                    float y1 = (float)(-NameTags.mc.field_71466_p.field_78288_b) - 1.0f;
                    float x2 = (float)width / 2.0f + 1.0f;
                    y2 = (Boolean)this.shadow.getValue() != false ? 0.0f : -1.0f;
                    int friendColor = Myau.friendManager.getColor().getRGB();
                    RenderUtil.drawOutlineRect(x1, y1, x2, y2, 1.5f, 0, friendColor);
                    RenderUtil.disableRenderState();
                } else if (TeamUtil.isTarget((EntityPlayer)entity)) {
                    RenderUtil.enableRenderState();
                    float x1 = (float)(-width) / 2.0f - 1.0f;
                    float y1 = (float)(-NameTags.mc.field_71466_p.field_78288_b) - 1.0f;
                    float x2 = (float)width / 2.0f + 1.0f;
                    y2 = (Boolean)this.shadow.getValue() != false ? 0.0f : -1.0f;
                    int targetColor = Myau.targetManager.getColor().getRGB();
                    RenderUtil.drawOutlineRect(x1, y1, x2, y2, 1.5f, 0, targetColor);
                    RenderUtil.disableRenderState();
                }
            }
            GlStateManager.func_179121_F();
        }
    }
}

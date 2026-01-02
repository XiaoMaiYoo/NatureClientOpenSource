package myau.module.modules;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.LeftClickMouseEvent;
import myau.events.Render3DEvent;
import myau.events.TickEvent;
import myau.mixin.IAccessorRenderManager;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ColorProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.ModeProperty;
import myau.util.RenderUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class HitBox
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private MovingObjectPosition targetEntity = null;
    public final FloatProperty multiplier = new FloatProperty("multiplier", Float.valueOf(1.2f), Float.valueOf(1.0f), Float.valueOf(5.0f));
    public final ModeProperty showHitbox = new ModeProperty("show-hitbox", 0, new String[]{"NONE", "PLAYERS", "MOBS", "ANIMALS", "ALL"});
    public final ColorProperty color = new ColorProperty("color", new Color(255, 255, 255).getRGB(), () -> (Integer)this.showHitbox.getValue() != 0);
    public final BooleanProperty teams = new BooleanProperty("teams", true, () -> (Integer)this.showHitbox.getValue() == 1 || (Integer)this.showHitbox.getValue() == 4);
    public final BooleanProperty botCheck = new BooleanProperty("bot-check", true, () -> (Integer)this.showHitbox.getValue() == 1 || (Integer)this.showHitbox.getValue() == 4);

    public HitBox() {
        super("HitBox", false);
    }

    public static float getExpansion(Entity entity) {
        HitBox hitBox = (HitBox)Myau.moduleManager.modules.get(HitBox.class);
        if (hitBox != null && hitBox.isEnabled() && entity instanceof EntityLivingBase) {
            return ((Float)hitBox.multiplier.getValue()).floatValue();
        }
        return 1.0f;
    }

    private void calculateMouseOver(float partialTicks) {
        if (mc.func_175606_aa() != null && HitBox.mc.field_71441_e != null) {
            HitBox.mc.field_147125_j = null;
            Entity pointedEntity = null;
            double reach = 3.0;
            this.targetEntity = mc.func_175606_aa().func_174822_a(reach, partialTicks);
            double distance = reach;
            Vec3 eyePos = mc.func_175606_aa().func_174824_e(partialTicks);
            if (this.targetEntity != null) {
                distance = this.targetEntity.field_72307_f.func_72438_d(eyePos);
            }
            Vec3 lookVec = mc.func_175606_aa().func_70676_i(partialTicks);
            Vec3 reachVec = eyePos.func_72441_c(lookVec.field_72450_a * reach, lookVec.field_72448_b * reach, lookVec.field_72449_c * reach);
            Vec3 hitVec = null;
            float expansion = 1.0f;
            List entities = HitBox.mc.field_71441_e.func_72839_b(mc.func_175606_aa(), mc.func_175606_aa().func_174813_aQ().func_72321_a(lookVec.field_72450_a * reach, lookVec.field_72448_b * reach, lookVec.field_72449_c * reach).func_72314_b((double)expansion, (double)expansion, (double)expansion));
            double closestDistance = distance;
            for (Entity entity : entities) {
                double interceptDistance;
                if (!entity.func_70067_L()) continue;
                float collisionSize = (float)((double)entity.func_70111_Y() * (double)HitBox.getExpansion(entity));
                AxisAlignedBB expandedBox = entity.func_174813_aQ().func_72314_b((double)collisionSize, (double)collisionSize, (double)collisionSize);
                MovingObjectPosition intercept = expandedBox.func_72327_a(eyePos, reachVec);
                if (expandedBox.func_72318_a(eyePos)) {
                    if (!(0.0 < closestDistance) && closestDistance != 0.0) continue;
                    pointedEntity = entity;
                    hitVec = intercept == null ? eyePos : intercept.field_72307_f;
                    closestDistance = 0.0;
                    continue;
                }
                if (intercept == null || !((interceptDistance = eyePos.func_72438_d(intercept.field_72307_f)) < closestDistance) && closestDistance != 0.0) continue;
                if (entity == HitBox.mc.func_175606_aa().field_70154_o && !entity.canRiderInteract()) {
                    if (closestDistance != 0.0) continue;
                    pointedEntity = entity;
                    hitVec = intercept.field_72307_f;
                    continue;
                }
                pointedEntity = entity;
                hitVec = intercept.field_72307_f;
                closestDistance = interceptDistance;
            }
            if (pointedEntity != null && (closestDistance < distance || this.targetEntity == null)) {
                this.targetEntity = new MovingObjectPosition(pointedEntity, hitVec);
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    HitBox.mc.field_147125_j = pointedEntity;
                }
            }
        }
    }

    private boolean shouldShowEntity(EntityLivingBase entity) {
        if (entity == HitBox.mc.field_71439_g) {
            return false;
        }
        if (entity.field_70725_aQ > 0 || entity instanceof EntityArmorStand || entity.func_82150_aj()) {
            return false;
        }
        if (mc.func_175606_aa().func_70032_d((Entity)entity) > 128.0f) {
            return false;
        }
        if (!entity.field_70158_ak && !RenderUtil.isInViewFrustum(entity.func_174813_aQ(), 0.1f)) {
            return false;
        }
        switch ((Integer)this.showHitbox.getValue()) {
            case 0: {
                return false;
            }
            case 1: {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer)entity;
                    if (TeamUtil.isFriend(player)) {
                        return false;
                    }
                    if (((Boolean)this.teams.getValue()).booleanValue() && TeamUtil.isSameTeam(player)) {
                        return false;
                    }
                    return (Boolean)this.botCheck.getValue() == false || !TeamUtil.isBot(player);
                }
                return false;
            }
            case 2: {
                if (entity instanceof EntityDragon || entity instanceof EntityWither) {
                    return true;
                }
                if (entity instanceof EntityMob || entity instanceof EntitySlime) {
                    return !(entity instanceof EntitySilverfish);
                }
                return false;
            }
            case 3: {
                return entity instanceof EntityAnimal || entity instanceof EntityBat || entity instanceof EntitySquid || entity instanceof EntityVillager || entity instanceof EntityIronGolem;
            }
            case 4: {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer)entity;
                    if (TeamUtil.isFriend(player)) {
                        return false;
                    }
                    if (((Boolean)this.teams.getValue()).booleanValue() && TeamUtil.isSameTeam(player)) {
                        return false;
                    }
                    if (((Boolean)this.botCheck.getValue()).booleanValue() && TeamUtil.isBot(player)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            this.calculateMouseOver(1.0f);
        }
    }

    @EventTarget(value=1)
    public void onLeftClick(LeftClickMouseEvent event) {
        if (this.isEnabled() && !event.isCancelled() && this.targetEntity != null) {
            HitBox.mc.field_71476_x = this.targetEntity;
        }
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        List entities;
        if (this.isEnabled() && (Integer)this.showHitbox.getValue() != 0 && !(entities = HitBox.mc.field_71441_e.field_72996_f.stream().filter(entity -> entity instanceof EntityLivingBase).map(entity -> (EntityLivingBase)entity).filter(this::shouldShowEntity).collect(Collectors.toList())).isEmpty()) {
            RenderUtil.enableRenderState();
            Color renderColor = new Color((Integer)this.color.getValue());
            for (EntityLivingBase entity2 : entities) {
                float collisionSize = (float)((double)entity2.func_70111_Y() * (double)((Float)this.multiplier.getValue()).floatValue());
                AxisAlignedBB expandedBox = entity2.func_174813_aQ().func_72314_b((double)collisionSize, (double)collisionSize, (double)collisionSize);
                AxisAlignedBB offsetBox = new AxisAlignedBB(expandedBox.field_72340_a - entity2.field_70165_t + (RenderUtil.lerpDouble(entity2.field_70165_t, entity2.field_70142_S, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX()), expandedBox.field_72338_b - entity2.field_70163_u + (RenderUtil.lerpDouble(entity2.field_70163_u, entity2.field_70137_T, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY()), expandedBox.field_72339_c - entity2.field_70161_v + (RenderUtil.lerpDouble(entity2.field_70161_v, entity2.field_70136_U, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ()), expandedBox.field_72336_d - entity2.field_70165_t + (RenderUtil.lerpDouble(entity2.field_70165_t, entity2.field_70142_S, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX()), expandedBox.field_72337_e - entity2.field_70163_u + (RenderUtil.lerpDouble(entity2.field_70163_u, entity2.field_70137_T, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY()), expandedBox.field_72334_f - entity2.field_70161_v + (RenderUtil.lerpDouble(entity2.field_70161_v, entity2.field_70136_U, event.getPartialTicks()) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ()));
                RenderUtil.drawBoundingBox(offsetBox, renderColor.getRed(), renderColor.getGreen(), renderColor.getBlue(), 150, 1.5f);
            }
            RenderUtil.disableRenderState();
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{String.format("%.1fx", this.multiplier.getValue())};
    }
}

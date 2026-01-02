package myau.module.modules;

import myau.event.EventTarget;
import myau.events.RenderLivingEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
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
import org.lwjgl.opengl.GL11;

public class Chams
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final BooleanProperty players = new BooleanProperty("players", true);
    public final BooleanProperty friends = new BooleanProperty("friends", true);
    public final BooleanProperty enemiess = new BooleanProperty("enemies", true);
    public final BooleanProperty bosses = new BooleanProperty("bosses", false);
    public final BooleanProperty mobs = new BooleanProperty("mobs", false);
    public final BooleanProperty creepers = new BooleanProperty("creepers", false);
    public final BooleanProperty enderman = new BooleanProperty("endermen", false);
    public final BooleanProperty blaze = new BooleanProperty("blazes", false);
    public final BooleanProperty animals = new BooleanProperty("animals", false);
    public final BooleanProperty self = new BooleanProperty("self", false);
    public final BooleanProperty bots = new BooleanProperty("bots", false);

    private boolean shouldRenderChams(EntityLivingBase entityLivingBase) {
        if (entityLivingBase.field_70725_aQ > 0) {
            return false;
        }
        if (mc.func_175606_aa().func_70032_d((Entity)entityLivingBase) > 512.0f) {
            return false;
        }
        if (entityLivingBase instanceof EntityPlayer) {
            if (entityLivingBase != Chams.mc.field_71439_g && entityLivingBase != mc.func_175606_aa()) {
                if (TeamUtil.isBot((EntityPlayer)entityLivingBase)) {
                    return (Boolean)this.bots.getValue();
                }
                if (TeamUtil.isFriend((EntityPlayer)entityLivingBase)) {
                    return (Boolean)this.friends.getValue();
                }
                return TeamUtil.isTarget((EntityPlayer)entityLivingBase) ? ((Boolean)this.enemiess.getValue()).booleanValue() : ((Boolean)this.players.getValue()).booleanValue();
            }
            return (Boolean)this.self.getValue() != false && Chams.mc.field_71474_y.field_74320_O != 0;
        }
        if (entityLivingBase instanceof EntityDragon || entityLivingBase instanceof EntityWither) {
            return !entityLivingBase.func_82150_aj() && (Boolean)this.bosses.getValue() != false;
        }
        if (!(entityLivingBase instanceof EntityMob) && !(entityLivingBase instanceof EntitySlime)) {
            return (entityLivingBase instanceof EntityAnimal || entityLivingBase instanceof EntityBat || entityLivingBase instanceof EntitySquid || entityLivingBase instanceof EntityVillager) && (Boolean)this.animals.getValue() != false;
        }
        if (entityLivingBase instanceof EntityCreeper) {
            return (Boolean)this.creepers.getValue();
        }
        if (entityLivingBase instanceof EntityEnderman) {
            return (Boolean)this.enderman.getValue();
        }
        return entityLivingBase instanceof EntityBlaze ? ((Boolean)this.blaze.getValue()).booleanValue() : ((Boolean)this.mobs.getValue()).booleanValue();
    }

    public Chams() {
        super("Chams", false);
    }

    @EventTarget
    public void onRenderLiving(RenderLivingEvent event) {
        if (this.isEnabled() && this.shouldRenderChams(event.getEntity())) {
            switch (event.getType()) {
                case PRE: {
                    GL11.glEnable((int)32823);
                    GL11.glPolygonOffset((float)1.0f, (float)-2500000.0f);
                    break;
                }
                case POST: {
                    GL11.glPolygonOffset((float)1.0f, (float)2500000.0f);
                    GL11.glDisable((int)32823);
                }
            }
        }
    }
}

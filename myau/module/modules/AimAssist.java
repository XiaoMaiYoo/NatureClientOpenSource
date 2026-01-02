package myau.module.modules;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.KeyEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.module.modules.AutoClicker;
import myau.module.modules.Reach;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.PercentProperty;
import myau.util.ItemUtil;
import myau.util.PlayerUtil;
import myau.util.RotationUtil;
import myau.util.TeamUtil;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;

public class AimAssist
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final TimerUtil timer = new TimerUtil();
    public final FloatProperty hSpeed = new FloatProperty("horizontal-speed", Float.valueOf(3.0f), Float.valueOf(0.0f), Float.valueOf(10.0f));
    public final FloatProperty vSpeed = new FloatProperty("vertical-speed", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(10.0f));
    public final PercentProperty smoothing = new PercentProperty("smoothing", 50);
    public final FloatProperty range = new FloatProperty("range", Float.valueOf(4.5f), Float.valueOf(3.0f), Float.valueOf(8.0f));
    public final IntProperty fov = new IntProperty("fov", 90, 30, 360);
    public final BooleanProperty weaponOnly = new BooleanProperty("weapons-only", true);
    public final BooleanProperty allowTools = new BooleanProperty("allow-tools", false, this.weaponOnly::getValue);
    public final BooleanProperty botChecks = new BooleanProperty("bot-check", true);
    public final BooleanProperty team = new BooleanProperty("teams", true);

    private boolean isValidTarget(EntityPlayer entityPlayer) {
        if (entityPlayer != AimAssist.mc.field_71439_g && entityPlayer != AimAssist.mc.field_71439_g.field_70154_o) {
            if (entityPlayer == mc.func_175606_aa() || entityPlayer == AimAssist.mc.func_175606_aa().field_70154_o) {
                return false;
            }
            if (entityPlayer.field_70725_aQ > 0) {
                return false;
            }
            if (RotationUtil.distanceToEntity((Entity)entityPlayer) > (double)((Float)this.range.getValue()).floatValue()) {
                return false;
            }
            if (RotationUtil.angleToEntity((Entity)entityPlayer) > (float)((Integer)this.fov.getValue()).intValue()) {
                return false;
            }
            if (RotationUtil.rayTrace((Entity)entityPlayer) != null) {
                return false;
            }
            if (TeamUtil.isFriend(entityPlayer)) {
                return false;
            }
            return !((Boolean)this.team.getValue() != false && TeamUtil.isSameTeam(entityPlayer) || (Boolean)this.botChecks.getValue() != false && TeamUtil.isBot(entityPlayer));
        }
        return false;
    }

    private boolean isInReach(EntityPlayer entityPlayer) {
        Reach reach = (Reach)Myau.moduleManager.modules.get(Reach.class);
        double distance = reach.isEnabled() ? (double)((Float)reach.range.getValue()).floatValue() : 3.0;
        return RotationUtil.distanceToEntity((Entity)entityPlayer) <= distance;
    }

    private boolean isLookingAtBlock() {
        return AimAssist.mc.field_71476_x != null && AimAssist.mc.field_71476_x.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    public AimAssist() {
        super("AimAssist", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        List inRange;
        boolean attacking;
        if (!(!this.isEnabled() || event.getType() != EventType.POST || AimAssist.mc.field_71462_r != null || ((Boolean)this.weaponOnly.getValue()).booleanValue() && !ItemUtil.hasRawUnbreakingEnchant() && (!((Boolean)this.allowTools.getValue()).booleanValue() || !ItemUtil.isHoldingTool()) || (attacking = PlayerUtil.isAttacking()) && this.isLookingAtBlock() || !attacking && this.timer.hasTimeElapsed(350L) || (inRange = AimAssist.mc.field_71441_e.field_72996_f.stream().filter(entity -> entity instanceof EntityPlayer).map(entity -> (EntityPlayer)entity).filter(this::isValidTarget).sorted(Comparator.comparingDouble(RotationUtil::distanceToEntity)).collect(Collectors.toList())).isEmpty())) {
            EntityPlayer player;
            if (inRange.stream().anyMatch(this::isInReach)) {
                inRange.removeIf(entityPlayer -> !this.isInReach((EntityPlayer)entityPlayer));
            }
            if (!(RotationUtil.distanceToEntity((Entity)(player = (EntityPlayer)inRange.get(0))) <= 0.0)) {
                AxisAlignedBB axisAlignedBB = player.func_174813_aQ();
                double collisionBorderSize = player.func_70111_Y();
                float[] rotation = RotationUtil.getRotationsToBox(axisAlignedBB.func_72314_b(collisionBorderSize, collisionBorderSize, collisionBorderSize), AimAssist.mc.field_71439_g.field_70177_z, AimAssist.mc.field_71439_g.field_70125_A, 180.0f, (float)((Integer)this.smoothing.getValue()).intValue() / 100.0f);
                float yaw = Math.min(Math.abs(((Float)this.hSpeed.getValue()).floatValue()), 10.0f);
                float pitch = Math.min(Math.abs(((Float)this.vSpeed.getValue()).floatValue()), 10.0f);
                Myau.rotationManager.setRotation(AimAssist.mc.field_71439_g.field_70177_z + (rotation[0] - AimAssist.mc.field_71439_g.field_70177_z) * 0.1f * yaw, AimAssist.mc.field_71439_g.field_70125_A + (rotation[1] - AimAssist.mc.field_71439_g.field_70125_A) * 0.1f * pitch, 0, false);
            }
        }
    }

    @EventTarget
    public void onPress(KeyEvent event) {
        if (event.getKey() == AimAssist.mc.field_71474_y.field_74312_F.func_151463_i() && !Myau.moduleManager.modules.get(AutoClicker.class).isEnabled()) {
            this.timer.reset();
        }
    }
}

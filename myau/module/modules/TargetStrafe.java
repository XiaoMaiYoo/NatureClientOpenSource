package myau.module.modules;

import java.awt.Color;
import java.util.ArrayList;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.Render3DEvent;
import myau.events.StrafeEvent;
import myau.events.UpdateEvent;
import myau.module.Module;
import myau.module.modules.Fly;
import myau.module.modules.HUD;
import myau.module.modules.KillAura;
import myau.module.modules.LongJump;
import myau.module.modules.Speed;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.ColorUtil;
import myau.util.MoveUtil;
import myau.util.PlayerUtil;
import myau.util.RenderUtil;
import myau.util.RotationUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

public class TargetStrafe
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private EntityLivingBase target = null;
    private float targetYaw = Float.NaN;
    private int direction = 1;
    public final FloatProperty radius = new FloatProperty("radius", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(6.0f));
    public final IntProperty points = new IntProperty("points", 6, 3, 24);
    public final BooleanProperty requirePress = new BooleanProperty("require-press", true);
    public final BooleanProperty speedOnly = new BooleanProperty("speed-only", true);
    public final ModeProperty showTarget = new ModeProperty("show-target", 1, new String[]{"NONE", "DEFAULT", "HUD"});

    private boolean canStrafe() {
        if (((Boolean)this.speedOnly.getValue()).booleanValue()) {
            Speed speed = (Speed)Myau.moduleManager.modules.get(Speed.class);
            Fly fly = (Fly)Myau.moduleManager.modules.get(Fly.class);
            LongJump longJump = (LongJump)Myau.moduleManager.modules.get(LongJump.class);
            if (!(speed.isEnabled() || fly.isEnabled() || longJump.isEnabled() && longJump.isJumping())) {
                return false;
            }
        }
        return (Boolean)this.requirePress.getValue() == false || PlayerUtil.isJumping();
    }

    private EntityLivingBase getKillAuraTarget() {
        KillAura killAura = (KillAura)Myau.moduleManager.modules.get(KillAura.class);
        if (killAura.isEnabled() && killAura.isAttackAllowed()) {
            EntityLivingBase entityLivingBase = killAura.getTarget();
            return !TeamUtil.isEntityLoaded((Entity)entityLivingBase) ? null : entityLivingBase;
        }
        return null;
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
        switch ((Integer)this.showTarget.getValue()) {
            case 1: {
                if (!(entityLivingBase instanceof EntityPlayer)) {
                    return Color.WHITE;
                }
                return TeamUtil.getTeamColor((EntityPlayer)entityLivingBase, 1.0f);
            }
            case 2: {
                int color = ((HUD)Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis()).getRGB();
                return new Color(color);
            }
        }
        return new Color(-1);
    }

    private boolean isInWater(double x, double z) {
        return PlayerUtil.checkInWater(new AxisAlignedBB(x - 0.015, TargetStrafe.mc.field_71439_g.field_70163_u, z - 0.015, x + 0.015, TargetStrafe.mc.field_71439_g.field_70163_u + (double)TargetStrafe.mc.field_71439_g.field_70131_O, z + 0.015));
    }

    private int wrapIndex(int index, int size) {
        if (index < 0) {
            return size - 1;
        }
        return index >= size ? 0 : index;
    }

    public TargetStrafe() {
        super("TargetStrafe", false);
    }

    public float getTargetYaw() {
        return this.targetYaw;
    }

    @EventTarget(value=0)
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            boolean right;
            boolean left = PlayerUtil.isMovingLeft();
            if (left ^ (right = PlayerUtil.isMovingRight())) {
                int n = this.direction = left ? 1 : -1;
            }
            if (!this.canStrafe()) {
                this.target = null;
                this.targetYaw = Float.NaN;
            } else {
                this.target = this.getKillAuraTarget();
                if (this.target == null) {
                    this.targetYaw = Float.NaN;
                } else {
                    ArrayList<Vec2d> vpositions = new ArrayList<Vec2d>();
                    for (int i = 0; i < (Integer)this.points.getValue(); ++i) {
                        vpositions.add(new Vec2d((double)((Float)this.radius.getValue()).floatValue() * Math.cos((double)i * (Math.PI * 2 / (double)((Integer)this.points.getValue()).intValue())), (double)((Float)this.radius.getValue()).floatValue() * Math.sin((double)i * (Math.PI * 2 / (double)((Integer)this.points.getValue()).intValue()))));
                    }
                    if (vpositions.isEmpty()) {
                        this.target = null;
                        this.targetYaw = Float.NaN;
                    } else {
                        double nextZ;
                        double closestDistance = 0.0;
                        int closestIndex = -1;
                        for (int i = 0; i < vpositions.size(); ++i) {
                            double distance = TargetStrafe.mc.field_71439_g.func_70011_f(this.target.field_70165_t + ((Vec2d)vpositions.get(i)).getX(), TargetStrafe.mc.field_71439_g.field_70163_u, this.target.field_70161_v + ((Vec2d)vpositions.get(i)).getY());
                            if (closestIndex != -1 && !(distance < closestDistance)) continue;
                            closestDistance = distance;
                            closestIndex = i;
                        }
                        if (TargetStrafe.mc.field_71439_g.field_70123_F) {
                            this.direction *= -1;
                        }
                        int nextIndex = closestIndex + this.direction;
                        double nextX = this.target.field_70165_t + ((Vec2d)vpositions.get(nextIndex = this.wrapIndex(nextIndex, vpositions.size()))).getX();
                        if (this.isInWater(nextX, nextZ = this.target.field_70161_v + ((Vec2d)vpositions.get(nextIndex)).getY())) {
                            this.direction *= -1;
                            nextIndex = closestIndex + this.direction;
                            nextIndex = this.wrapIndex(nextIndex, vpositions.size());
                            nextX = this.target.field_70165_t + ((Vec2d)vpositions.get(nextIndex)).getX();
                            nextZ = this.target.field_70161_v + ((Vec2d)vpositions.get(nextIndex)).getY();
                        }
                        double deltaX = nextX - TargetStrafe.mc.field_71439_g.field_70165_t;
                        double deltaZ = nextZ - TargetStrafe.mc.field_71439_g.field_70161_v;
                        float currentPitch = event.getPitch();
                        float currentYaw = event.getYaw();
                        double deltaY = 0.0;
                        this.targetYaw = RotationUtil.getRotationsTo(deltaX, deltaY, deltaZ, currentYaw, currentPitch)[0];
                        event.setPervRotation(this.targetYaw, 10);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (this.isEnabled() && !Float.isNaN(this.targetYaw) && MoveUtil.isForwardPressed()) {
            event.setStrafe(0.0f);
            event.setForward(1.0f);
        }
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (this.isEnabled() && TeamUtil.isEntityLoaded((Entity)this.target) && (Integer)this.showTarget.getValue() != 0) {
            Color color = this.getTargetColor(this.target);
            RenderUtil.enableRenderState();
            RenderUtil.drawEntityCircle((Entity)this.target, ((Float)this.radius.getValue()).floatValue(), (Integer)this.points.getValue(), ColorUtil.darker(color, 0.2f).getRGB());
            RenderUtil.drawEntityCircle((Entity)this.target, ((Float)this.radius.getValue()).floatValue(), (Integer)this.points.getValue(), color.getRGB());
            RenderUtil.disableRenderState();
        }
    }

    @Override
    public void onDisabled() {
        this.target = null;
        this.targetYaw = Float.NaN;
    }

    public static class Vec2d {
        private final double x;
        private final double y;

        public Vec2d(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }
    }
}

package myau.module.modules;

import myau.Myau;
import myau.event.EventTarget;
import myau.events.LivingUpdateEvent;
import myau.events.StrafeEvent;
import myau.mixin.IAccessorEntity;
import myau.module.Module;
import myau.property.properties.FloatProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.MoveUtil;
import net.minecraft.client.Minecraft;

public class Speed
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"Custom", "Hypixel3"});
    public final FloatProperty multiplier = new FloatProperty("multiplier", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(10.0f), () -> (Integer)this.mode.getValue() == 0);
    public final FloatProperty friction = new FloatProperty("friction", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(10.0f), () -> (Integer)this.mode.getValue() == 0);
    public final PercentProperty strafe = new PercentProperty("strafe", 0, () -> (Integer)this.mode.getValue() == 0);
    private boolean hypixel3WasOnGround = false;

    private boolean canBoost() {
        try {
            Class<?> scaffoldClass = Class.forName("myau.module.modules.Scaffold");
            Module scaffold = Myau.moduleManager.getModule(scaffoldClass);
            return !scaffold.isEnabled() && (Speed.mc.field_71439_g.field_71158_b.field_78900_b != 0.0f || Speed.mc.field_71439_g.field_71158_b.field_78902_a != 0.0f) && Speed.mc.field_71439_g.func_71024_bL().func_75116_a() > 6 && !Speed.mc.field_71439_g.func_70093_af() && !Speed.mc.field_71439_g.func_70090_H() && !Speed.mc.field_71439_g.func_180799_ab() && !((IAccessorEntity)Speed.mc.field_71439_g).getIsInWeb();
        }
        catch (Exception e) {
            return (Speed.mc.field_71439_g.field_71158_b.field_78900_b != 0.0f || Speed.mc.field_71439_g.field_71158_b.field_78902_a != 0.0f) && Speed.mc.field_71439_g.func_71024_bL().func_75116_a() > 6 && !Speed.mc.field_71439_g.func_70093_af() && !Speed.mc.field_71439_g.func_70090_H() && !Speed.mc.field_71439_g.func_180799_ab();
        }
    }

    public Speed() {
        super("Speed", false);
    }

    private void handleHypixel3() {
        boolean isMoving;
        if (Speed.mc.field_71439_g == null) {
            return;
        }
        boolean bl = isMoving = (double)Math.abs(Speed.mc.field_71439_g.field_71158_b.field_78900_b) > 0.01 || (double)Math.abs(Speed.mc.field_71439_g.field_71158_b.field_78902_a) > 0.01;
        if (isMoving && !Speed.mc.field_71439_g.func_70093_af()) {
            Speed.mc.field_71439_g.func_70031_b(true);
        }
        if (Speed.mc.field_71439_g.field_70122_E) {
            if (!this.hypixel3WasOnGround) {
                Speed.mc.field_71439_g.field_70181_x = 0.42;
                MoveUtil.setSpeed(1.6);
                this.hypixel3WasOnGround = true;
            }
        } else {
            double currentSpeed;
            this.hypixel3WasOnGround = false;
            if (Speed.mc.field_71439_g.field_70143_R < 1.0f && (currentSpeed = MoveUtil.getSpeed()) < 1.0) {
                MoveUtil.setSpeed(1.0);
            }
        }
    }

    @EventTarget(value=3)
    public void onStrafe(StrafeEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if ((Integer)this.mode.getValue() == 0 && this.canBoost()) {
            if (Speed.mc.field_71439_g.field_70122_E) {
                Speed.mc.field_71439_g.field_70181_x = 0.42f;
                MoveUtil.setSpeed(MoveUtil.getJumpMotion() * (double)((Float)this.multiplier.getValue()).floatValue(), MoveUtil.getMoveYaw());
            } else {
                if (((Float)this.friction.getValue()).floatValue() != 1.0f) {
                    event.setFriction(event.getFriction() * ((Float)this.friction.getValue()).floatValue());
                }
                if ((Integer)this.strafe.getValue() > 0) {
                    double speed = MoveUtil.getSpeed();
                    MoveUtil.setSpeed(speed * (double)(100 - (Integer)this.strafe.getValue()) / 100.0, MoveUtil.getDirectionYaw());
                    MoveUtil.addSpeed(speed * (double)((Integer)this.strafe.getValue()).intValue() / 100.0, MoveUtil.getMoveYaw());
                    MoveUtil.setSpeed(speed);
                }
            }
        }
    }

    @EventTarget(value=3)
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if ((Integer)this.mode.getValue() == 1) {
            if (this.canBoost()) {
                this.handleHypixel3();
            }
        } else if (this.canBoost()) {
            Speed.mc.field_71439_g.field_71158_b.field_78901_c = false;
        }
    }

    @Override
    public void onDisabled() {
        this.hypixel3WasOnGround = false;
        super.onDisabled();
    }

    @Override
    public String[] getSuffix() {
        return new String[]{this.mode.getModeString()};
    }
}

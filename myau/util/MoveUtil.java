package myau.util;

import myau.Myau;
import myau.management.RotationState;
import myau.module.modules.TargetStrafe;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class MoveUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public static boolean isForwardPressed() {
        if (MoveUtil.mc.field_71474_y.field_74351_w.func_151470_d() != MoveUtil.mc.field_71474_y.field_74368_y.func_151470_d()) {
            return true;
        }
        return MoveUtil.mc.field_71474_y.field_74370_x.func_151470_d() != MoveUtil.mc.field_71474_y.field_74366_z.func_151470_d();
    }

    public static int getForwardValue() {
        int forwardValue = 0;
        if (MoveUtil.mc.field_71474_y.field_74351_w.func_151470_d()) {
            ++forwardValue;
        }
        if (MoveUtil.mc.field_71474_y.field_74368_y.func_151470_d()) {
            --forwardValue;
        }
        return forwardValue;
    }

    public static int getLeftValue() {
        int leftValue = 0;
        if (MoveUtil.mc.field_71474_y.field_74370_x.func_151470_d()) {
            ++leftValue;
        }
        if (MoveUtil.mc.field_71474_y.field_74366_z.func_151470_d()) {
            --leftValue;
        }
        return leftValue;
    }

    public static float getMoveYaw() {
        return MoveUtil.adjustYaw(RotationState.isActived() ? RotationState.getSmoothedYaw() : MoveUtil.mc.field_71439_g.field_70177_z, MoveUtil.mc.field_71439_g.field_71158_b.field_78900_b, MoveUtil.mc.field_71439_g.field_71158_b.field_78902_a);
    }

    public static float adjustYaw(float yaw, float forward, float strafe) {
        TargetStrafe targetStrafe = (TargetStrafe)Myau.moduleManager.modules.get(TargetStrafe.class);
        if (targetStrafe.isEnabled() && !Float.isNaN(targetStrafe.getTargetYaw())) {
            return targetStrafe.getTargetYaw();
        }
        if (forward < 0.0f) {
            yaw += 180.0f;
        }
        if (strafe != 0.0f) {
            float multiplier = forward == 0.0f ? 1.0f : 0.5f * Math.signum(forward);
            yaw += -90.0f * multiplier * Math.signum(strafe);
        }
        return MathHelper.func_76142_g((float)yaw);
    }

    public static float getDirectionYaw() {
        if (MoveUtil.getSpeed() == 0.0) {
            return MathHelper.func_76142_g((float)MoveUtil.mc.field_71439_g.field_70177_z);
        }
        return MathHelper.func_76142_g((float)((float)Math.toDegrees(Math.atan2(MoveUtil.mc.field_71439_g.field_70179_y, MoveUtil.mc.field_71439_g.field_70159_w)) - 90.0f));
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.28015;
        if (MoveUtil.getSpeedTime() > 0) {
            baseSpeed = 0.28015 * (1.0 + 0.15 * (double)MoveUtil.getSpeedLevel());
        }
        return baseSpeed;
    }

    public static double getBaseJumpHigh(int speedLevel) {
        double jumpHeight = 0.452;
        if (speedLevel == 1) {
            jumpHeight = 0.49720000000000003;
        } else if (speedLevel >= 2) {
            jumpHeight *= 1.2;
        }
        return jumpHeight;
    }

    public static double getJumpMotion() {
        int speedLevel = 0;
        if (MoveUtil.getSpeedTime() > 0) {
            speedLevel = MoveUtil.getSpeedLevel();
        }
        return MoveUtil.getBaseJumpHigh(speedLevel);
    }

    public static double getSpeed() {
        return MoveUtil.getSpeed(MoveUtil.mc.field_71439_g.field_70159_w, MoveUtil.mc.field_71439_g.field_70179_y);
    }

    public static double getSpeed(double motionX, double motionZ) {
        return Math.hypot(motionX, motionZ);
    }

    public static void setSpeed(double speed) {
        MoveUtil.setSpeed(speed, MoveUtil.getDirectionYaw());
    }

    public static void setSpeed(double speed, float yaw) {
        MoveUtil.mc.field_71439_g.field_70159_w = -Math.sin(Math.toRadians(yaw)) * speed;
        MoveUtil.mc.field_71439_g.field_70179_y = Math.cos(Math.toRadians(yaw)) * speed;
    }

    public static void addSpeed(double speed, float yaw) {
        MoveUtil.mc.field_71439_g.field_70159_w += -Math.sin(Math.toRadians(yaw)) * speed;
        MoveUtil.mc.field_71439_g.field_70179_y += Math.cos(Math.toRadians(yaw)) * speed;
    }

    public static int getSpeedLevel() {
        int speedLevel = 0;
        if (MoveUtil.mc.field_71439_g.func_70644_a(Potion.field_76424_c)) {
            speedLevel = MoveUtil.mc.field_71439_g.func_70660_b(Potion.field_76424_c).func_76458_c() + 1;
        }
        return speedLevel;
    }

    public static int getSpeedTime() {
        if (MoveUtil.mc.field_71439_g.func_70644_a(Potion.field_76424_c)) {
            return MoveUtil.mc.field_71439_g.func_70660_b(Potion.field_76424_c).func_76459_b();
        }
        return 0;
    }

    public static float getAllowedHorizontalDistance() {
        float slipperiness = MoveUtil.mc.field_71439_g.field_70170_p.func_180495_p((BlockPos)new BlockPos((int)MathHelper.func_76128_c((double)MoveUtil.mc.field_71439_g.field_70165_t), (int)(MathHelper.func_76128_c((double)MoveUtil.mc.field_71439_g.func_174813_aQ().field_72338_b) - 1), (int)MathHelper.func_76128_c((double)MoveUtil.mc.field_71439_g.field_70161_v))).func_177230_c().field_149765_K * 0.91f;
        return MoveUtil.mc.field_71439_g.func_70689_ay() * (0.16277136f / (slipperiness * slipperiness * slipperiness));
    }

    public static double[] predictMovement() {
        float forwardInput;
        float strafeInput = (float)MoveUtil.getLeftValue() * 0.98f;
        float inputMagnitude = strafeInput * strafeInput + (forwardInput = (float)MoveUtil.getForwardValue() * 0.98f) * forwardInput;
        if (inputMagnitude >= 1.0E-4f) {
            if ((inputMagnitude = MathHelper.func_76129_c((float)inputMagnitude)) < 1.0f) {
                inputMagnitude = 1.0f;
            }
            inputMagnitude = MoveUtil.getAllowedHorizontalDistance() / inputMagnitude;
            float sinYaw = MathHelper.func_76126_a((float)(MoveUtil.mc.field_71439_g.field_70177_z * (float)Math.PI / 180.0f));
            float cosYaw = MathHelper.func_76134_b((float)(MoveUtil.mc.field_71439_g.field_70177_z * (float)Math.PI / 180.0f));
            return new double[]{(strafeInput *= inputMagnitude) * cosYaw - (forwardInput *= inputMagnitude) * sinYaw, forwardInput * cosYaw + strafeInput * sinYaw};
        }
        return new double[]{0.0, 0.0};
    }

    public static void fixStrafe(float targetYaw) {
        float angle = MathHelper.func_76142_g((float)(MoveUtil.adjustYaw(MoveUtil.mc.field_71439_g.field_70177_z, MoveUtil.getForwardValue(), MoveUtil.getLeftValue()) - targetYaw + 22.5f));
        switch ((int)(angle + 180.0f) / 45 % 8) {
            case 0: {
                MoveUtil.mc.field_71439_g.field_71158_b.field_78900_b = -1.0f;
                MoveUtil.mc.field_71439_g.field_71158_b.field_78902_a = 0.0f;
                break;
            }
            case 1: {
                MoveUtil.mc.field_71439_g.field_71158_b.field_78900_b = -1.0f;
                MoveUtil.mc.field_71439_g.field_71158_b.field_78902_a = 1.0f;
                break;
            }
            case 2: {
                MoveUtil.mc.field_71439_g.field_71158_b.field_78900_b = 0.0f;
                MoveUtil.mc.field_71439_g.field_71158_b.field_78902_a = 1.0f;
                break;
            }
            case 3: {
                MoveUtil.mc.field_71439_g.field_71158_b.field_78900_b = 1.0f;
                MoveUtil.mc.field_71439_g.field_71158_b.field_78902_a = 1.0f;
                break;
            }
            case 4: {
                MoveUtil.mc.field_71439_g.field_71158_b.field_78900_b = 1.0f;
                MoveUtil.mc.field_71439_g.field_71158_b.field_78902_a = 0.0f;
                break;
            }
            case 5: {
                MoveUtil.mc.field_71439_g.field_71158_b.field_78900_b = 1.0f;
                MoveUtil.mc.field_71439_g.field_71158_b.field_78902_a = -1.0f;
                break;
            }
            case 6: {
                MoveUtil.mc.field_71439_g.field_71158_b.field_78900_b = 0.0f;
                MoveUtil.mc.field_71439_g.field_71158_b.field_78902_a = -1.0f;
                break;
            }
            case 7: {
                MoveUtil.mc.field_71439_g.field_71158_b.field_78900_b = -1.0f;
                MoveUtil.mc.field_71439_g.field_71158_b.field_78902_a = -1.0f;
            }
        }
        if (MoveUtil.mc.field_71439_g.field_71158_b.field_78899_d) {
            MoveUtil.mc.field_71439_g.field_71158_b.field_78900_b *= 0.3f;
            MoveUtil.mc.field_71439_g.field_71158_b.field_78902_a *= 0.3f;
        }
    }
}

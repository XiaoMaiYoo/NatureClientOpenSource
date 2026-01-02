package myau.util;

import myau.mixin.IAccessorEntity;
import myau.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class RotationUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public static float wrapAngleDiff(float angle, float target) {
        return target + MathHelper.func_76142_g((float)(angle - target));
    }

    public static float clampAngle(float angle, float maxAngle) {
        if (angle > (maxAngle = Math.max(0.0f, Math.min(180.0f, maxAngle)))) {
            angle = maxAngle;
        } else if (angle < -maxAngle) {
            angle = -maxAngle;
        }
        return angle;
    }

    public static float smoothAngle(float angle, float smoothFactor) {
        return angle * (0.5f + 0.5f * (1.0f - Math.max(0.0f, Math.min(1.0f, smoothFactor + RandomUtil.nextFloat(-0.1f, 0.1f)))));
    }

    public static float quantizeAngle(float angle) {
        return (float)((double)angle - (double)angle % (double)0.0096f);
    }

    public static float[] getRotationsToBox(AxisAlignedBB boundingBox, float yaw, float pitch, float maxAngle, float smoothFactor) {
        Vec3 eyePos = RotationUtil.mc.field_71439_g.func_174824_e(1.0f);
        double minTargetY = boundingBox.field_72338_b + 0.05 * (boundingBox.field_72337_e - boundingBox.field_72338_b);
        double maxTargetY = boundingBox.field_72338_b + 0.75 * (boundingBox.field_72337_e - boundingBox.field_72338_b);
        double deltaX = (boundingBox.field_72340_a + boundingBox.field_72336_d) / 2.0 - eyePos.field_72450_a;
        double deltaY = eyePos.field_72448_b >= maxTargetY ? maxTargetY - eyePos.field_72448_b : (eyePos.field_72448_b <= minTargetY ? minTargetY - eyePos.field_72448_b : 0.0);
        double deltaZ = (boundingBox.field_72339_c + boundingBox.field_72334_f) / 2.0 - eyePos.field_72449_c;
        return RotationUtil.getRotations(deltaX, deltaY, deltaZ, yaw, pitch, maxAngle, smoothFactor);
    }

    public static float[] getRotationsTo(double targetX, double targetY, double targetZ, float currentYaw, float currentPitch) {
        return RotationUtil.getRotations(targetX, targetY, targetZ, currentYaw, currentPitch, 180.0f, 0.0f);
    }

    public static float[] getRotations(double targetX, double targetY, double targetZ, float currentYaw, float currentPitch, float maxAngle, float smoothFactor) {
        double horizontalDistance = Math.sqrt(targetX * targetX + targetZ * targetZ);
        float yawDelta = MathHelper.func_76142_g((float)((float)(Math.atan2(targetZ, targetX) * 180.0 / Math.PI) - 90.0f - currentYaw));
        float pitchDelta = MathHelper.func_76142_g((float)((float)(-Math.atan2(targetY, horizontalDistance) * 180.0 / Math.PI) - currentPitch));
        yawDelta = Math.abs(yawDelta) <= 1.0f ? 0.0f : RotationUtil.smoothAngle(RotationUtil.clampAngle(yawDelta, maxAngle), smoothFactor);
        pitchDelta = Math.abs(pitchDelta) <= 1.0f ? 0.0f : RotationUtil.smoothAngle(RotationUtil.clampAngle(pitchDelta, maxAngle), smoothFactor);
        return new float[]{RotationUtil.quantizeAngle(currentYaw + yawDelta), RotationUtil.quantizeAngle(currentPitch + pitchDelta)};
    }

    public static Vec3 clampVecToBox(Vec3 vector, AxisAlignedBB boundingBox) {
        double[] coords = new double[]{vector.field_72450_a, vector.field_72448_b, vector.field_72449_c};
        double[] minCoords = new double[]{boundingBox.field_72340_a, boundingBox.field_72338_b, boundingBox.field_72339_c};
        double[] maxCoords = new double[]{boundingBox.field_72336_d, boundingBox.field_72337_e, boundingBox.field_72334_f};
        for (int i = 0; i < 3; ++i) {
            if (coords[i] > maxCoords[i]) {
                coords[i] = maxCoords[i];
                continue;
            }
            if (!(coords[i] < minCoords[i])) continue;
            coords[i] = minCoords[i];
        }
        return new Vec3(coords[0], coords[1], coords[2]);
    }

    public static float smoothRotation(float current, float target, float maxDelta) {
        float delta = MathHelper.func_76142_g((float)(target - current));
        if (delta > maxDelta) {
            delta = maxDelta;
        } else if (delta < -maxDelta) {
            delta = -maxDelta;
        }
        return current + delta;
    }

    public static double distanceToBox(AxisAlignedBB boundingBox) {
        return RotationUtil.clampVecToBox(boundingBox, RotationUtil.mc.field_71439_g.func_174824_e(1.0f));
    }

    public static double clampVecToBox(AxisAlignedBB boundingBox, Vec3 point) {
        if (boundingBox.func_72318_a(point)) {
            return 0.0;
        }
        Vec3 clampedPoint = RotationUtil.clampVecToBox(point, boundingBox);
        double deltaX = clampedPoint.field_72450_a - point.field_72450_a;
        double deltaY = clampedPoint.field_72448_b - point.field_72448_b;
        double deltaZ = clampedPoint.field_72449_c - point.field_72449_c;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public static float getYawBetween(double x1, double z1, double x2, double z2) {
        return MathHelper.func_76142_g((float)((float)(Math.atan2(z2 - z1, x2 - x1) * 180.0 / Math.PI) - 90.0f - RotationUtil.mc.field_71439_g.field_70177_z));
    }

    public static MovingObjectPosition rayTrace(float yaw, float pitch, double distance, float partialTicks) {
        Vec3 eyePos = RotationUtil.mc.field_71439_g.func_174824_e(partialTicks);
        Vec3 lookVec = ((IAccessorEntity)RotationUtil.mc.field_71439_g).callGetVectorForRotation(pitch, yaw);
        Vec3 targetPos = eyePos.func_72441_c(lookVec.field_72450_a * distance, lookVec.field_72448_b * distance, lookVec.field_72449_c * distance);
        return RotationUtil.mc.field_71441_e.func_72933_a(eyePos, targetPos);
    }

    public static MovingObjectPosition rayTrace(AxisAlignedBB boundingBox, float yaw, float pitch, double distance) {
        Vec3 eyePos = RotationUtil.mc.field_71439_g.func_174824_e(1.0f);
        Vec3 lookVec = ((IAccessorEntity)RotationUtil.mc.field_71439_g).callGetVectorForRotation(pitch, yaw);
        Vec3 targetPos = eyePos.func_72441_c(lookVec.field_72450_a * distance, lookVec.field_72448_b * distance, lookVec.field_72449_c * distance);
        return boundingBox.func_72327_a(eyePos, targetPos);
    }

    public static double distanceToEntity(Entity entity) {
        float borderSize = entity.func_70111_Y();
        AxisAlignedBB boundingBox = entity.func_174813_aQ().func_72314_b((double)borderSize, (double)borderSize, (double)borderSize);
        return RotationUtil.distanceToBox(boundingBox);
    }

    public static double distanceToBox(Entity entity, Vec3 point) {
        float borderSize = entity.func_70111_Y();
        return RotationUtil.clampVecToBox(entity.func_174813_aQ().func_72314_b((double)borderSize, (double)borderSize, (double)borderSize), point);
    }

    public static float angleToEntity(Entity entity) {
        Vec3 eyePos = RotationUtil.mc.field_71439_g.func_174824_e(1.0f);
        float borderSize = entity.func_70111_Y();
        AxisAlignedBB boundingBox = entity.func_174813_aQ().func_72314_b((double)borderSize, (double)borderSize, (double)borderSize);
        if (boundingBox.func_72318_a(eyePos)) {
            return 0.0f;
        }
        double deltaX = entity.field_70165_t - eyePos.field_72450_a;
        double deltaZ = entity.field_70161_v - eyePos.field_72449_c;
        return Math.abs(MathHelper.func_76142_g((float)((float)(Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f - RotationUtil.mc.field_71439_g.field_70177_z))) * 2.0f;
    }

    public static MovingObjectPosition rayTrace(Entity entity) {
        Vec3 eyePos = RotationUtil.mc.field_71439_g.func_174824_e(1.0f);
        float borderSize = entity.func_70111_Y();
        Vec3 targetPos = RotationUtil.clampVecToBox(eyePos, entity.func_174813_aQ().func_72314_b((double)borderSize, (double)borderSize, (double)borderSize));
        return RotationUtil.mc.field_71441_e.func_72933_a(eyePos, targetPos);
    }

    public static Vec3 getBlockHitVec(BlockPos blockPos) {
        return new Vec3((double)blockPos.func_177958_n() + 0.5, (double)blockPos.func_177956_o() + 0.5, (double)blockPos.func_177952_p() + 0.5);
    }

    public static float[] getBackRotations(BlockPos blockPos, EnumFacing facing, float moveYaw, float partialTicks) {
        float backYaw = moveYaw - 180.0f;
        Vec3 center = RotationUtil.getBlockHitVec(blockPos);
        float[] normalRot = RotationUtil.getRotationsTo(center.field_72450_a, center.field_72448_b, center.field_72449_c, backYaw, RotationUtil.mc.field_71439_g.field_70125_A);
        float pitch = normalRot[1];
        MovingObjectPosition mop = RotationUtil.rayTrace(backYaw, pitch, (double)RotationUtil.mc.field_71442_b.func_78757_d(), partialTicks);
        if (mop != null && mop.func_178782_a().equals((Object)blockPos) && mop.field_178784_b == facing) {
            return new float[]{RotationUtil.quantizeAngle(backYaw), RotationUtil.quantizeAngle(pitch)};
        }
        return null;
    }

    public static float[] getRotationFromPosition(Vec3 targetPos) {
        if (RotationUtil.mc.field_71439_g == null || targetPos == null) {
            return new float[]{RotationUtil.mc.field_71439_g.field_70177_z, RotationUtil.mc.field_71439_g.field_70125_A};
        }
        Vec3 eyePos = RotationUtil.mc.field_71439_g.func_174824_e(1.0f);
        double x = targetPos.field_72450_a - eyePos.field_72450_a;
        double y = targetPos.field_72448_b - eyePos.field_72448_b;
        double z = targetPos.field_72449_c - eyePos.field_72449_c;
        double horizontalDistance = MathHelper.func_76133_a((double)(x * x + z * z));
        float yaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-Math.atan2(y, horizontalDistance) * 180.0 / Math.PI);
        return new float[]{yaw, pitch};
    }
}

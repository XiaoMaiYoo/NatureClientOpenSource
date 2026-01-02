package myau.util;

import com.google.common.base.Predicates;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class RayCastUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public static Entity raycastEntity(double range, float yaw, float pitch, IEntityFilter entityFilter) {
        Entity renderViewEntity = mc.func_175606_aa();
        if (renderViewEntity != null && RayCastUtil.mc.field_71441_e != null) {
            double blockReachDistance = range;
            Vec3 eyePosition = renderViewEntity.func_174824_e(1.0f);
            float yawCos = MathHelper.func_76134_b((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
            float yawSin = MathHelper.func_76126_a((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
            float pitchCos = -MathHelper.func_76134_b((float)(-pitch * ((float)Math.PI / 180)));
            float pitchSin = MathHelper.func_76126_a((float)(-pitch * ((float)Math.PI / 180)));
            Vec3 entityLook = new Vec3((double)(yawSin * pitchCos), (double)pitchSin, (double)(yawCos * pitchCos));
            Vec3 vector = eyePosition.func_72441_c(entityLook.field_72450_a * blockReachDistance, entityLook.field_72448_b * blockReachDistance, entityLook.field_72449_c * blockReachDistance);
            List entityList = RayCastUtil.mc.field_71441_e.func_175674_a(renderViewEntity, renderViewEntity.func_174813_aQ().func_72321_a(entityLook.field_72450_a * blockReachDistance, entityLook.field_72448_b * blockReachDistance, entityLook.field_72449_c * blockReachDistance).func_72314_b(1.0, 1.0, 1.0), Predicates.and(EntitySelectors.field_180132_d, Entity::func_70067_L));
            Entity pointedEntity = null;
            for (Entity entity : entityList) {
                double eyeDistance;
                if (!entityFilter.canRaycast(entity)) continue;
                float collisionBorderSize = entity.func_70111_Y();
                AxisAlignedBB axisAlignedBB = entity.func_174813_aQ().func_72314_b((double)collisionBorderSize, (double)collisionBorderSize, (double)collisionBorderSize);
                MovingObjectPosition movingObjectPosition = axisAlignedBB.func_72327_a(eyePosition, vector);
                if (axisAlignedBB.func_72318_a(eyePosition)) {
                    if (!(blockReachDistance >= 0.0)) continue;
                    pointedEntity = entity;
                    blockReachDistance = 0.0;
                    continue;
                }
                if (movingObjectPosition == null || !((eyeDistance = eyePosition.func_72438_d(movingObjectPosition.field_72307_f)) < blockReachDistance) && blockReachDistance != 0.0) continue;
                pointedEntity = entity;
                blockReachDistance = eyeDistance;
            }
            return pointedEntity;
        }
        return null;
    }

    public static interface IEntityFilter {
        public boolean canRaycast(Entity var1);
    }
}

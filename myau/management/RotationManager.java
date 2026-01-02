package myau.management;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.Render3DEvent;
import myau.events.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class RotationManager {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private float lastUpdate = Float.NaN;
    private float yawDelta = Float.NaN;
    private float pitchDelta = Float.NaN;
    private int priority = Integer.MIN_VALUE;
    private boolean rotated = false;

    private void applyRotation(float partialTicks) {
        if (!(RotationManager.mc.field_71439_g == null || Float.isNaN(this.yawDelta) || Float.isNaN(this.pitchDelta) || Float.isNaN(this.lastUpdate))) {
            float pitch;
            float yaw = this.yawDelta * (partialTicks - this.lastUpdate);
            if (yaw != 0.0f) {
                RotationManager.mc.field_71439_g.field_70126_B = RotationManager.mc.field_71439_g.field_70177_z;
                RotationManager.mc.field_71439_g.field_70177_z += yaw;
            }
            if ((pitch = this.pitchDelta * (partialTicks - this.lastUpdate)) != 0.0f) {
                RotationManager.mc.field_71439_g.field_70127_C = RotationManager.mc.field_71439_g.field_70125_A;
                RotationManager.mc.field_71439_g.field_70125_A += pitch;
                RotationManager.mc.field_71439_g.field_70125_A = MathHelper.func_76131_a((float)RotationManager.mc.field_71439_g.field_70125_A, (float)-90.0f, (float)90.0f);
            }
            this.lastUpdate = partialTicks;
        }
    }

    private void resetRotationState() {
        this.lastUpdate = Float.NaN;
        this.yawDelta = Float.NaN;
        this.pitchDelta = Float.NaN;
        this.priority = Integer.MIN_VALUE;
        this.rotated = false;
    }

    public void setRotation(float yaw, float pitch, int priority, boolean force) {
        if (this.priority <= priority) {
            this.priority = priority;
            this.yawDelta = MathHelper.func_76142_g((float)(yaw - RotationManager.mc.field_71439_g.field_70177_z));
            this.pitchDelta = MathHelper.func_76131_a((float)(pitch - RotationManager.mc.field_71439_g.field_70125_A), (float)-90.0f, (float)90.0f);
            this.lastUpdate = 0.0f;
            this.rotated = force;
            this.applyRotation(0.0f);
        }
    }

    public boolean isRotated() {
        return this.rotated;
    }

    @EventTarget(value=0)
    public void onTick(TickEvent event) {
        if (event.getType() != EventType.PRE) {
            return;
        }
        this.applyRotation(1.0f);
        this.resetRotationState();
    }

    @EventTarget(value=0)
    public void onRender3D(Render3DEvent event) {
        this.applyRotation(event.getPartialTicks());
    }
}

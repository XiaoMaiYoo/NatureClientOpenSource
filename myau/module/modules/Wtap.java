package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.MoveInputEvent;
import myau.events.PacketEvent;
import myau.module.Module;
import myau.property.properties.FloatProperty;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.potion.Potion;

public class Wtap
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final TimerUtil timer = new TimerUtil();
    private boolean active = false;
    private boolean stopForward = false;
    private long delayTicks = 0L;
    private long durationTicks = 0L;
    public final FloatProperty delay = new FloatProperty("delay", Float.valueOf(5.5f), Float.valueOf(0.0f), Float.valueOf(10.0f));
    public final FloatProperty duration = new FloatProperty("duration", Float.valueOf(1.5f), Float.valueOf(1.0f), Float.valueOf(5.0f));

    private boolean canTrigger() {
        return !(Wtap.mc.field_71439_g.field_71158_b.field_78900_b < 0.8f || Wtap.mc.field_71439_g.field_70123_F || (float)Wtap.mc.field_71439_g.func_71024_bL().func_75116_a() <= 6.0f && !Wtap.mc.field_71439_g.field_71075_bZ.field_75101_c || !Wtap.mc.field_71439_g.func_70051_ag() && (Wtap.mc.field_71439_g.func_71039_bw() || Wtap.mc.field_71439_g.func_70644_a(Potion.field_76440_q) || !Wtap.mc.field_71474_y.field_151444_V.func_151470_d()));
    }

    public Wtap() {
        super("WTap", false);
    }

    @EventTarget(value=4)
    public void onMoveInput(MoveInputEvent event) {
        if (this.active) {
            if (!this.stopForward && !this.canTrigger()) {
                this.active = false;
                while (this.delayTicks > 0L) {
                    this.delayTicks -= 50L;
                }
                while (this.durationTicks > 0L) {
                    this.durationTicks -= 50L;
                }
            } else if (this.delayTicks > 0L) {
                this.delayTicks -= 50L;
            } else {
                if (this.durationTicks > 0L) {
                    this.durationTicks -= 50L;
                    this.stopForward = true;
                    Wtap.mc.field_71439_g.field_71158_b.field_78900_b = 0.0f;
                }
                if (this.durationTicks <= 0L) {
                    this.active = false;
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (this.isEnabled() && !event.isCancelled() && event.getType() == EventType.SEND && event.getPacket() instanceof C02PacketUseEntity && ((C02PacketUseEntity)event.getPacket()).func_149565_c() == C02PacketUseEntity.Action.ATTACK && !this.active && this.timer.hasTimeElapsed(500L) && Wtap.mc.field_71439_g.func_70051_ag()) {
            this.timer.reset();
            this.active = true;
            this.stopForward = false;
            this.delayTicks += (long)(50.0f * ((Float)this.delay.getValue()).floatValue());
            this.durationTicks += (long)(50.0f * ((Float)this.duration.getValue()).floatValue());
        }
    }
}

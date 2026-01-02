package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.LoadWorldEvent;
import myau.events.PacketEvent;
import myau.module.Module;
import myau.util.PacketUtil;
import myau.util.RandomUtil;
import myau.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class NoRotate
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private boolean reset = false;

    public NoRotate() {
        super("NoRotate", false);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (this.isEnabled() && event.getType() == EventType.RECEIVE && !event.isCancelled() && (NoRotate.mc.field_71439_g.field_70177_z != -180.0f || NoRotate.mc.field_71439_g.field_70125_A != 0.0f)) {
            String msg;
            if (event.getPacket() instanceof S02PacketChat && ((msg = ((S02PacketChat)event.getPacket()).func_148915_c().func_150254_d()).contains("\u00a7e\u00a7lProtect your bed and destroy the enemy beds.") || msg.contains("\u00a7eYou will respawn in \u00a7r\u00a7c1 \u00a7r\u00a7esecond!"))) {
                this.reset = true;
            }
            if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                if (this.reset) {
                    this.reset = false;
                    return;
                }
                S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook)event.getPacket();
                event.setCancelled(true);
                double x = packet.func_148932_c();
                double y = packet.func_148928_d();
                double z = packet.func_148933_e();
                float yaw = packet.func_148931_f();
                float pitch = packet.func_148930_g();
                if (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X)) {
                    x += NoRotate.mc.field_71439_g.field_70165_t;
                } else {
                    NoRotate.mc.field_71439_g.field_70159_w = 0.0;
                }
                if (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y)) {
                    y += NoRotate.mc.field_71439_g.field_70163_u;
                } else {
                    NoRotate.mc.field_71439_g.field_70181_x = 0.0;
                }
                if (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Z)) {
                    z += NoRotate.mc.field_71439_g.field_70161_v;
                } else {
                    NoRotate.mc.field_71439_g.field_70179_y = 0.0;
                }
                if (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X_ROT)) {
                    pitch += NoRotate.mc.field_71439_g.field_70125_A;
                }
                if (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y_ROT)) {
                    yaw += NoRotate.mc.field_71439_g.field_70177_z;
                }
                NoRotate.mc.field_71439_g.func_70080_a(x, y, z, RotationUtil.quantizeAngle(NoRotate.mc.field_71439_g.field_70177_z + RandomUtil.nextFloat(-0.01f, 0.01f)), RotationUtil.quantizeAngle(NoRotate.mc.field_71439_g.field_70125_A + RandomUtil.nextFloat(-0.01f, 0.01f)));
                PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(NoRotate.mc.field_71439_g.field_70165_t, NoRotate.mc.field_71439_g.func_174813_aQ().field_72338_b, NoRotate.mc.field_71439_g.field_70161_v, yaw % 360.0f, pitch % 360.0f, false));
            }
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.reset = false;
    }

    @Override
    public void onDisabled() {
        this.reset = false;
    }
}

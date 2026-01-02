package myau.util;

import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

public class PacketUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public static void sendPacket(Packet<?> packet) {
        mc.func_147114_u().func_147298_b().func_179290_a(packet);
    }

    public static void sendPacketNoEvent(Packet<?> packet) {
        mc.func_147114_u().func_147298_b().func_179288_a(packet, null, new GenericFutureListener[0]);
    }
}

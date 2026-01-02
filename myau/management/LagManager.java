package myau.management;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.events.TickEvent;
import myau.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.Vec3;

public class LagManager {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final Deque<LagPacket> packetQueue = new ConcurrentLinkedDeque<LagPacket>();
    private int tickDelay = 0;
    private boolean flushing = false;
    private Vec3 lastPosition = new Vec3(0.0, 0.0, 0.0);

    private void flushQueue() {
        if (mc.func_147114_u() == null) {
            this.packetQueue.clear();
        } else {
            this.flushing = true;
            while (!this.packetQueue.isEmpty()) {
                C03PacketPlayer c03;
                LagPacket lagPacket = this.packetQueue.peek();
                if (this.tickDelay > 0 && lagPacket.delay <= this.tickDelay) break;
                PacketUtil.sendPacketNoEvent(lagPacket.packet);
                if (lagPacket.packet instanceof C03PacketPlayer && (c03 = (C03PacketPlayer)lagPacket.packet).func_149466_j()) {
                    this.lastPosition = new Vec3(c03.func_149464_c(), c03.func_149467_d(), c03.func_149472_e());
                }
                this.packetQueue.poll();
            }
            this.flushing = false;
        }
    }

    private void incrementDelays() {
        this.packetQueue.forEach(z -> ++z.delay);
    }

    public boolean handlePacket(Packet<?> packet) {
        C03PacketPlayer c03;
        this.flushQueue();
        if (packet instanceof C00PacketKeepAlive || packet instanceof C01PacketChatMessage) {
            return false;
        }
        if ((long)this.tickDelay > 0L) {
            this.packetQueue.offer(new LagPacket(packet));
            return true;
        }
        if (packet instanceof C03PacketPlayer && (c03 = (C03PacketPlayer)packet).func_149466_j()) {
            this.lastPosition = new Vec3(c03.func_149464_c(), c03.func_149467_d(), c03.func_149472_e());
        }
        return false;
    }

    public void setDelay(int delay) {
        this.tickDelay = delay;
    }

    public Vec3 getLastPosition() {
        return this.lastPosition;
    }

    public boolean isFlushing() {
        return this.flushing;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() == EventType.POST) {
            if (LagManager.mc.field_71439_g.field_70128_L) {
                this.setDelay(0);
            }
            this.incrementDelays();
            this.flushQueue();
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof C00Handshake || event.getPacket() instanceof C00PacketLoginStart || event.getPacket() instanceof C00PacketServerQuery || event.getPacket() instanceof C01PacketPing || event.getPacket() instanceof C01PacketEncryptionResponse) {
            this.setDelay(0);
        }
    }

    public static class LagPacket {
        public final Packet<?> packet;
        public int delay;

        public LagPacket(Packet<?> packet) {
            this.packet = packet;
            this.delay = 0;
        }
    }
}

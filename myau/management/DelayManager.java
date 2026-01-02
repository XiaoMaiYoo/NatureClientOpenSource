package myau.management;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import myau.enums.DelayModules;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.events.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.world.World;

public class DelayManager {
    public static Minecraft mc = Minecraft.func_71410_x();
    public DelayModules delayModule = DelayModules.NONE;
    public long delay = 0L;
    public Deque<Packet<INetHandlerPlayClient>> delayedPacket = new ConcurrentLinkedDeque<Packet<INetHandlerPlayClient>>();

    public boolean shouldDelay(Packet<INetHandlerPlayClient> packet) {
        if (this.delayModule == DelayModules.NONE) {
            return false;
        }
        if (packet instanceof S00PacketKeepAlive) {
            return false;
        }
        if (!(packet instanceof S01PacketJoinGame) && !(packet instanceof S07PacketRespawn)) {
            S19PacketEntityStatus s19;
            Entity entity;
            if (packet instanceof S19PacketEntityStatus && (entity = (s19 = (S19PacketEntityStatus)packet).func_149161_a((World)DelayManager.mc.field_71441_e)) != null && (!entity.equals((Object)DelayManager.mc.field_71439_g) || s19.func_149160_c() != 2)) {
                return false;
            }
            this.delayedPacket.offer(packet);
            return true;
        }
        this.setDelayState(false, this.delayModule);
        return false;
    }

    public boolean setDelayState(boolean state, DelayModules delayModule) {
        if (state) {
            this.delay = 0L;
            this.delayModule = delayModule;
        } else {
            this.delayModule = DelayModules.NONE;
            if (Minecraft.func_71410_x().func_147114_u() != null && this.delayedPacket.isEmpty()) {
                return true;
            }
            while (true) {
                Packet<INetHandlerPlayClient> packet;
                if ((packet = this.delayedPacket.poll()) == null) {
                    this.delayedPacket.clear();
                    break;
                }
                packet.func_148833_a((INetHandler)Minecraft.func_71410_x().func_147114_u());
            }
        }
        return this.delayModule != DelayModules.NONE;
    }

    public DelayModules getDelayModule() {
        return this.delayModule;
    }

    public void delay(DelayModules modules) {
        this.delayModule = modules;
    }

    public long getDelay() {
        return this.delay;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof C00Handshake || event.getPacket() instanceof C00PacketLoginStart || event.getPacket() instanceof C00PacketServerQuery || event.getPacket() instanceof C01PacketPing || event.getPacket() instanceof C01PacketEncryptionResponse) {
            this.setDelayState(false, this.delayModule);
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() == EventType.POST) {
            if (DelayManager.mc.field_71439_g.field_70128_L) {
                this.setDelayState(false, this.delayModule);
            }
            if (this.delayModule != DelayModules.NONE) {
                ++this.delay;
            }
        }
    }
}

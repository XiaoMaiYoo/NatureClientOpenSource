package myau.module.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.LoadWorldEvent;
import myau.events.PacketEvent;
import myau.events.Render2DEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.world.World;

public class Buffer
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final List<Packet<INetHandlerPlayClient>> packets = new ArrayList<Packet<INetHandlerPlayClient>>();
    private boolean delaying = false;
    private int timeout = 0;
    private boolean s08 = false;
    private final int color = new Color(209, 1, 1, 255).getRGB();
    public final BooleanProperty renderTimer = new BooleanProperty("render-timer", false);
    public final IntProperty maxTimeout = new IntProperty("max-timeout", 300, 50, 600);

    public Buffer() {
        super("Buffer", false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventTarget(value=0)
    public void onPacket(PacketEvent event) {
        S19PacketEntityStatus s19;
        S12PacketEntityVelocity s12;
        if (!this.isEnabled() || event.getType() != EventType.RECEIVE || event.isCancelled()) {
            return;
        }
        if (Buffer.mc.field_71439_g == null || Buffer.mc.field_71441_e == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (!this.delaying && packet instanceof S08PacketPlayerPosLook) {
            this.s08 = true;
        }
        if (packet instanceof S12PacketEntityVelocity && (s12 = (S12PacketEntityVelocity)packet).func_149412_c() == Buffer.mc.field_71439_g.func_145782_y()) {
            if (this.s08) {
                this.s08 = false;
                return;
            }
            this.delaying = true;
        }
        if (packet instanceof S19PacketEntityStatus && (s19 = (S19PacketEntityStatus)packet).func_149161_a((World)Buffer.mc.field_71441_e) != null && s19.func_149161_a((World)Buffer.mc.field_71441_e).equals((Object)Buffer.mc.field_71439_g) && s19.func_149160_c() == 2) {
            this.delaying = true;
        }
        if (this.delaying && (packet instanceof S12PacketEntityVelocity || packet instanceof S32PacketConfirmTransaction || packet instanceof S08PacketPlayerPosLook)) {
            List<Packet<INetHandlerPlayClient>> list = this.packets;
            synchronized (list) {
                Packet<?> playPacket = packet;
                this.packets.add(playPacket);
            }
            event.setCancelled(true);
        }
    }

    @EventTarget(value=2)
    public void onTick(TickEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getType() == EventType.POST) {
            if (this.delaying && ++this.timeout >= (Integer)this.maxTimeout.getValue()) {
                this.flush();
                ChatUtil.sendFormatted(Myau.clientName + "&cFreeze timed out.");
            }
            this.s08 = false;
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled() || !((Boolean)this.renderTimer.getValue()).booleanValue()) {
            return;
        }
        if (Buffer.mc.field_71439_g == null || this.timeout == 0) {
            return;
        }
        if (Buffer.mc.field_71462_r != null) {
            return;
        }
        this.renderTimer(this.timeout);
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.flush();
    }

    private void renderTimer(int ticks) {
        int widthOffset = ticks < 10 ? 4 : (ticks >= 10 && ticks < 100 ? 7 : (ticks >= 100 && ticks < 1000 ? 10 : 13));
        String text = String.valueOf(ticks);
        int width = Buffer.mc.field_71466_p.func_78256_a(text);
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.func_78326_a();
        int screenHeight = sr.func_78328_b();
        float yadd = 8.0f;
        Buffer.mc.field_71466_p.func_175063_a(text, (float)(screenWidth / 2 - width + widthOffset), (float)(screenHeight / 2 + (int)yadd), this.color);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void flush() {
        if (this.packets.isEmpty()) {
            this.delaying = false;
            this.timeout = 0;
            return;
        }
        List<Packet<INetHandlerPlayClient>> list = this.packets;
        synchronized (list) {
            while (true) {
                if (this.packets.isEmpty()) {
                    // MONITOREXIT @DISABLED, blocks:[2, 4, 5] lbl9 : MonitorExitStatement: MONITOREXIT : var1_1
                    this.delaying = false;
                    this.timeout = 0;
                    return;
                }
                Packet<INetHandlerPlayClient> packet = this.packets.remove(0);
                try {
                    packet.func_148833_a((INetHandler)mc.func_147114_u());
                }
                catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                break;
            }
        }
    }

    @Override
    public void onDisabled() {
        this.flush();
    }

    @Override
    public String[] getSuffix() {
        if (this.delaying && this.timeout > 0) {
            return new String[]{String.valueOf(this.timeout)};
        }
        return new String[]{"Ready"};
    }
}

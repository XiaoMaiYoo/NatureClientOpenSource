package myau.module.modules;

import com.google.common.base.CaseFormat;
import myau.Myau;
import myau.enums.BlinkModules;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.events.TickEvent;
import myau.mixin.IAccessorC03PacketPlayer;
import myau.mixin.IAccessorMinecraft;
import myau.module.Module;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.ChatUtil;
import myau.util.PacketUtil;
import myau.util.PlayerUtil;
import myau.util.ServerUtil;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;

public class NoFall
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final TimerUtil packetDelayTimer = new TimerUtil();
    private final TimerUtil scoreboardResetTimer = new TimerUtil();
    private boolean slowFalling = false;
    private boolean lastOnGround = false;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"PACKET", "BLINK", "NO_GROUND", "SPOOF"});
    public final FloatProperty distance = new FloatProperty("distance", Float.valueOf(3.0f), Float.valueOf(0.0f), Float.valueOf(20.0f));
    public final IntProperty delay = new IntProperty("delay", 0, 0, 10000);

    private boolean canTrigger() {
        return this.scoreboardResetTimer.hasTimeElapsed(3000L) && this.packetDelayTimer.hasTimeElapsed(((Integer)this.delay.getValue()).longValue());
    }

    public NoFall() {
        super("NoFall", false);
    }

    @EventTarget(value=1)
    public void onPacket(PacketEvent event) {
        if (event.getType() == EventType.RECEIVE && event.getPacket() instanceof S08PacketPlayerPosLook) {
            this.onDisabled();
        } else if (this.isEnabled() && event.getType() == EventType.SEND && !event.isCancelled() && event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer packet = (C03PacketPlayer)event.getPacket();
            switch ((Integer)this.mode.getValue()) {
                case 0: {
                    if (this.slowFalling) {
                        this.slowFalling = false;
                        ((IAccessorMinecraft)NoFall.mc).getTimer().field_74278_d = 1.0f;
                        break;
                    }
                    if (packet.func_149465_i()) break;
                    AxisAlignedBB aabb = NoFall.mc.field_71439_g.func_174813_aQ().func_72314_b(2.0, 0.0, 2.0);
                    if (!PlayerUtil.canFly(((Float)this.distance.getValue()).floatValue()) || PlayerUtil.checkInWater(aabb) || !this.canTrigger()) break;
                    this.packetDelayTimer.reset();
                    this.slowFalling = true;
                    ((IAccessorMinecraft)NoFall.mc).getTimer().field_74278_d = 0.5f;
                    break;
                }
                case 1: {
                    boolean allowed;
                    boolean bl = allowed = !NoFall.mc.field_71439_g.func_70617_f_() && !NoFall.mc.field_71439_g.field_71075_bZ.field_75101_c && NoFall.mc.field_71439_g.field_70737_aN == 0;
                    if (Myau.blinkManager.getBlinkingModule() != BlinkModules.NO_FALL) {
                        if (this.lastOnGround && !packet.func_149465_i() && allowed && PlayerUtil.canFly(((Float)this.distance.getValue()).intValue()) && NoFall.mc.field_71439_g.field_70181_x < 0.0) {
                            Myau.blinkManager.setBlinkState(false, Myau.blinkManager.getBlinkingModule());
                            Myau.blinkManager.setBlinkState(true, BlinkModules.NO_FALL);
                        }
                    } else if (!allowed) {
                        Myau.blinkManager.setBlinkState(false, BlinkModules.NO_FALL);
                        ChatUtil.sendFormatted(String.format("%s%s: &cFailed player check!&r", Myau.clientName, this.getName()));
                    } else if (PlayerUtil.checkInWater(NoFall.mc.field_71439_g.func_174813_aQ().func_72314_b(2.0, 0.0, 2.0))) {
                        Myau.blinkManager.setBlinkState(false, BlinkModules.NO_FALL);
                        ChatUtil.sendFormatted(String.format("%s%s: &cFailed void check!&r", Myau.clientName, this.getName()));
                    } else if (packet.func_149465_i()) {
                        for (Packet<?> blinkedPacket : Myau.blinkManager.blinkedPackets) {
                            if (!(blinkedPacket instanceof C03PacketPlayer)) continue;
                            ((IAccessorC03PacketPlayer)blinkedPacket).setOnGround(true);
                        }
                        Myau.blinkManager.setBlinkState(false, BlinkModules.NO_FALL);
                        this.packetDelayTimer.reset();
                    }
                    this.lastOnGround = packet.func_149465_i() && allowed && this.canTrigger();
                    break;
                }
                case 2: {
                    ((IAccessorC03PacketPlayer)packet).setOnGround(false);
                    break;
                }
                case 3: {
                    if (packet.func_149465_i()) break;
                    AxisAlignedBB aabb = NoFall.mc.field_71439_g.func_174813_aQ().func_72314_b(2.0, 0.0, 2.0);
                    if (!PlayerUtil.canFly(((Float)this.distance.getValue()).floatValue()) || PlayerUtil.checkInWater(aabb) || !this.canTrigger()) break;
                    this.packetDelayTimer.reset();
                    ((IAccessorC03PacketPlayer)packet).setOnGround(true);
                    NoFall.mc.field_71439_g.field_70143_R = 0.0f;
                }
            }
        }
    }

    @EventTarget(value=0)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (ServerUtil.hasPlayerCountInfo()) {
                this.scoreboardResetTimer.reset();
            }
            if ((Integer)this.mode.getValue() == 0 && this.slowFalling) {
                PacketUtil.sendPacketNoEvent(new C03PacketPlayer(true));
                NoFall.mc.field_71439_g.field_70143_R = 0.0f;
            }
        }
    }

    @Override
    public void onDisabled() {
        this.lastOnGround = false;
        Myau.blinkManager.setBlinkState(false, BlinkModules.NO_FALL);
        if (this.slowFalling) {
            this.slowFalling = false;
            ((IAccessorMinecraft)NoFall.mc).getTimer().field_74278_d = 1.0f;
        }
    }

    @Override
    public void verifyValue(String mode) {
        if (this.isEnabled()) {
            this.onDisabled();
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}

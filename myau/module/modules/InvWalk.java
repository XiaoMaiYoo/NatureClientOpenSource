package myau.module.modules;

import com.google.common.base.CaseFormat;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.events.TickEvent;
import myau.events.UpdateEvent;
import myau.mixin.IAccessorC0DPacketCloseWindow;
import myau.module.Module;
import myau.module.modules.Sprint;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import myau.ui.ClickGui;
import myau.util.KeyBindUtil;
import myau.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;

public class InvWalk
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final Queue<C0EPacketClickWindow> clickQueue = new ConcurrentLinkedQueue<C0EPacketClickWindow>();
    private boolean keysPressed = false;
    private C16PacketClientStatus pendingStatus = null;
    private int delayTicks = 0;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"VANILLA", "LEGIT", "HYPIXEL"});
    public final BooleanProperty guiEnabled = new BooleanProperty("ClickGUI", true);

    public InvWalk() {
        super("InvWalk", false);
    }

    public void pressMovementKeys() {
        KeyBinding[] movementKeys;
        for (KeyBinding keyBinding : movementKeys = new KeyBinding[]{InvWalk.mc.field_71474_y.field_74351_w, InvWalk.mc.field_71474_y.field_74368_y, InvWalk.mc.field_71474_y.field_74370_x, InvWalk.mc.field_71474_y.field_74366_z, InvWalk.mc.field_71474_y.field_74314_A, InvWalk.mc.field_71474_y.field_151444_V}) {
            KeyBindUtil.updateKeyState(keyBinding.func_151463_i());
        }
        if (Myau.moduleManager.getModule(Sprint.class).isEnabled()) {
            KeyBindUtil.setKeyBindState(InvWalk.mc.field_71474_y.field_151444_V.func_151463_i(), true);
        }
        this.keysPressed = true;
    }

    public boolean canInvWalk() {
        if (!(InvWalk.mc.field_71462_r instanceof GuiContainer)) {
            return false;
        }
        if (InvWalk.mc.field_71462_r instanceof GuiContainerCreative) {
            return false;
        }
        switch ((Integer)this.mode.getValue()) {
            case 0: {
                return InvWalk.mc.field_71462_r instanceof GuiInventory && this.pendingStatus != null && this.clickQueue.isEmpty();
            }
            case 1: {
                return this.clickQueue.isEmpty();
            }
            case 2: {
                return true;
            }
        }
        return false;
    }

    @EventTarget(value=4)
    public void onTick(TickEvent event) {
        if (event.getType() == EventType.PRE) {
            while (!this.clickQueue.isEmpty()) {
                PacketUtil.sendPacketNoEvent((Packet)this.clickQueue.poll());
            }
        }
    }

    @EventTarget(value=4)
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled() || event.getType() != EventType.PRE) {
            return;
        }
        if (InvWalk.mc.field_71462_r instanceof ClickGui && ((Boolean)this.guiEnabled.getValue()).booleanValue()) {
            this.pressMovementKeys();
            return;
        }
        if (this.canInvWalk() && this.delayTicks == 0) {
            this.pressMovementKeys();
        } else {
            if (this.keysPressed) {
                if (InvWalk.mc.field_71462_r != null) {
                    KeyBinding.func_74506_a();
                }
                this.keysPressed = false;
            }
            if (this.pendingStatus != null) {
                PacketUtil.sendPacketNoEvent(this.pendingStatus);
                this.pendingStatus = null;
            }
            if (this.delayTicks > 0) {
                --this.delayTicks;
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled() || event.getType() != EventType.SEND) {
            return;
        }
        if (event.getPacket() instanceof C16PacketClientStatus) {
            C16PacketClientStatus packet = (C16PacketClientStatus)event.getPacket();
            if ((Integer)this.mode.getValue() == 0 && packet.func_149435_c() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                event.setCancelled(true);
                this.pendingStatus = packet;
            }
        } else if (event.getPacket() instanceof C0DPacketCloseWindow) {
            C0DPacketCloseWindow packet = (C0DPacketCloseWindow)event.getPacket();
            if (this.pendingStatus != null && ((IAccessorC0DPacketCloseWindow)packet).getWindowId() == 0) {
                this.pendingStatus = null;
                event.setCancelled(true);
            }
        } else if (event.getPacket() instanceof C0EPacketClickWindow) {
            C0EPacketClickWindow packet = (C0EPacketClickWindow)event.getPacket();
            switch ((Integer)this.mode.getValue()) {
                case 0: {
                    if (packet.func_149548_c() != 0) break;
                    if ((packet.func_149542_h() == 3 || packet.func_149542_h() == 4) && packet.func_149544_d() == -999) {
                        event.setCancelled(true);
                        return;
                    }
                    if (this.pendingStatus == null) break;
                    KeyBinding.func_74506_a();
                    event.setCancelled(true);
                    this.clickQueue.offer(packet);
                    break;
                }
                case 1: {
                    if ((packet.func_149542_h() == 3 || packet.func_149542_h() == 4) && packet.func_149544_d() == -999) {
                        event.setCancelled(true);
                        break;
                    }
                    KeyBinding.func_74506_a();
                    event.setCancelled(true);
                    this.clickQueue.offer(packet);
                    this.delayTicks = 8;
                    break;
                }
                case 2: {
                    if (packet.func_149542_h() != 3 && packet.func_149542_h() != 4 || packet.func_149544_d() != -999) break;
                    event.setCancelled(true);
                }
            }
            if (this.pendingStatus != null) {
                PacketUtil.sendPacketNoEvent(this.pendingStatus);
                this.pendingStatus = null;
            }
        }
    }

    @Override
    public void onDisabled() {
        if (this.keysPressed) {
            if (InvWalk.mc.field_71462_r != null) {
                KeyBinding.func_74506_a();
            }
            this.keysPressed = false;
        }
        if (this.pendingStatus != null) {
            PacketUtil.sendPacketNoEvent(this.pendingStatus);
            this.pendingStatus = null;
        }
        this.delayTicks = 0;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}

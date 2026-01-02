package myau.mixin;

import myau.Myau;
import myau.event.EventManager;
import myau.event.types.EventType;
import myau.events.HitBlockEvent;
import myau.events.KeyEvent;
import myau.events.LeftClickMouseEvent;
import myau.events.LoadWorldEvent;
import myau.events.ResizeEvent;
import myau.events.RightClickMouseEvent;
import myau.events.SwapItemEvent;
import myau.events.TickEvent;
import myau.init.Initializer;
import myau.module.modules.NoHitDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(value=Side.CLIENT)
@Mixin(value={Minecraft.class})
public abstract class MixinMinecraft {
    @Shadow
    private int field_71429_W;
    @Shadow
    public PlayerControllerMP field_71442_b;
    @Shadow
    public WorldClient field_71441_e;
    @Shadow
    public EntityPlayerSP field_71439_g;
    @Shadow
    public GuiScreen field_71462_r;

    @Inject(method={"startGame"}, at={@At(value="HEAD")})
    private void startGame(CallbackInfo callbackInfo) {
        new Initializer();
    }

    @Inject(method={"startGame"}, at={@At(value="RETURN")})
    private void postStartGame(CallbackInfo callbackInfo) {
        new Myau();
    }

    @Inject(method={"runTick"}, at={@At(value="HEAD")})
    private void runTick(CallbackInfo callbackInfo) {
        if (this.field_71441_e != null && this.field_71439_g != null) {
            EventManager.call(new TickEvent(EventType.PRE));
        }
    }

    @Inject(method={"runTick"}, at={@At(value="RETURN")})
    private void postRunTick(CallbackInfo callbackInfo) {
        if (this.field_71441_e != null && this.field_71439_g != null) {
            EventManager.call(new TickEvent(EventType.POST));
        }
    }

    @Inject(method={"loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V"}, at={@At(value="HEAD")})
    private void loadWorld(WorldClient worldClient, String string, CallbackInfo callbackInfo) {
        EventManager.call(new LoadWorldEvent());
    }

    @Inject(method={"updateFramebufferSize"}, at={@At(value="RETURN")})
    private void updateFramebufferSize(CallbackInfo callbackInfo) {
        EventManager.call(new ResizeEvent());
    }

    @Inject(method={"clickMouse"}, at={@At(value="HEAD")}, cancellable=true)
    private void clickMouse(CallbackInfo callbackInfo) {
        if (Myau.moduleManager != null && Myau.moduleManager.modules.get(NoHitDelay.class).isEnabled()) {
            this.field_71429_W = 0;
        }
        LeftClickMouseEvent event = new LeftClickMouseEvent();
        EventManager.call(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"rightClickMouse"}, at={@At(value="HEAD")}, cancellable=true)
    private void rightClickMouse(CallbackInfo callbackInfo) {
        RightClickMouseEvent event = new RightClickMouseEvent();
        EventManager.call(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"sendClickBlockToController"}, at={@At(value="HEAD")}, cancellable=true)
    private void sendClickBlockToController(CallbackInfo callbackInfo) {
        HitBlockEvent event = new HitBlockEvent();
        EventManager.call(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
            this.field_71442_b.func_78767_c();
        }
    }

    @Redirect(method={"runTick"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/settings/KeyBinding;setKeyBindState(IZ)V"))
    private void setKeyBindState(int integer, boolean boolean2) {
        KeyBinding.func_74510_a((int)integer, (boolean)boolean2);
        if (boolean2 && this.field_71462_r == null) {
            EventManager.call(new KeyEvent(integer, true));
        }
    }

    @Redirect(method={"runTick"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"))
    private void changeCurrentItem(InventoryPlayer inventoryPlayer, int slot) {
        SwapItemEvent event = new SwapItemEvent(-1, slot);
        EventManager.call(event);
        if (!event.isCancelled()) {
            inventoryPlayer.func_70453_c(slot);
        }
    }
}

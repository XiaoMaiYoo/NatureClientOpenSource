package myau;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import myau.command.CommandManager;
import myau.command.commands.BindCommand;
import myau.command.commands.ConfigCommand;
import myau.command.commands.DenickCommand;
import myau.command.commands.FriendCommand;
import myau.command.commands.HelpCommand;
import myau.command.commands.HideCommand;
import myau.command.commands.IgnCommand;
import myau.command.commands.ItemCommand;
import myau.command.commands.ListCommand;
import myau.command.commands.ModuleCommand;
import myau.command.commands.PlayerCommand;
import myau.command.commands.ShowCommand;
import myau.command.commands.TargetCommand;
import myau.command.commands.ToggleCommand;
import myau.command.commands.VclipCommand;
import myau.config.Config;
import myau.event.EventManager;
import myau.management.BlinkManager;
import myau.management.DelayManager;
import myau.management.FloatManager;
import myau.management.FriendManager;
import myau.management.LagManager;
import myau.management.PlayerStateManager;
import myau.management.RotationManager;
import myau.management.TargetManager;
import myau.module.Module;
import myau.module.ModuleManager;
import myau.module.modules.AimAssist;
import myau.module.modules.AntiDebuff;
import myau.module.modules.AntiVoid;
import myau.module.modules.AutoClicker;
import myau.module.modules.AutoHeal;
import myau.module.modules.AutoTool;
import myau.module.modules.BedESP;
import myau.module.modules.BedNuker;
import myau.module.modules.Blink;
import myau.module.modules.Block;
import myau.module.modules.Buffer;
import myau.module.modules.Chams;
import myau.module.modules.ChestESP;
import myau.module.modules.ChestStealer;
import myau.module.modules.ESP;
import myau.module.modules.Eagle;
import myau.module.modules.FastPlace;
import myau.module.modules.Fly;
import myau.module.modules.FullBright;
import myau.module.modules.GhostHand;
import myau.module.modules.GlobalFont;
import myau.module.modules.GuiModule;
import myau.module.modules.HUD;
import myau.module.modules.HitBox;
import myau.module.modules.HitSelect;
import myau.module.modules.Indicators;
import myau.module.modules.InvManager;
import myau.module.modules.InvWalk;
import myau.module.modules.ItemESP;
import myau.module.modules.Jesus;
import myau.module.modules.KeepSprint;
import myau.module.modules.KillAura;
import myau.module.modules.LagRange;
import myau.module.modules.LightningTracker;
import myau.module.modules.LongJump;
import myau.module.modules.MoreKB;
import myau.module.modules.NameTags;
import myau.module.modules.NickHider;
import myau.module.modules.NoFall;
import myau.module.modules.NoHitDelay;
import myau.module.modules.NoHurtCam;
import myau.module.modules.NoJumpDelay;
import myau.module.modules.NoRotate;
import myau.module.modules.NoSlow;
import myau.module.modules.NotificationModule;
import myau.module.modules.Reach;
import myau.module.modules.SafeWalk;
import myau.module.modules.Scaffold;
import myau.module.modules.Spammer;
import myau.module.modules.Speed;
import myau.module.modules.Sprint;
import myau.module.modules.TargetHUD;
import myau.module.modules.TargetStrafe;
import myau.module.modules.Tracers;
import myau.module.modules.Trajectories;
import myau.module.modules.Velocity;
import myau.module.modules.ViewClip;
import myau.module.modules.Wtap;
import myau.module.modules.Xray;
import myau.property.Property;
import myau.property.PropertyManager;

public class Myau {
    public static String clientName = "&7[&cN&6a&et&au&7r&ee&7]&r ";
    public static String version;
    public static RotationManager rotationManager;
    public static FloatManager floatManager;
    public static BlinkManager blinkManager;
    public static DelayManager delayManager;
    public static LagManager lagManager;
    public static PlayerStateManager playerStateManager;
    public static FriendManager friendManager;
    public static TargetManager targetManager;
    public static PropertyManager propertyManager;
    public static ModuleManager moduleManager;
    public static CommandManager commandManager;

    public Myau() {
        this.init();
    }

    public void init() {
        rotationManager = new RotationManager();
        floatManager = new FloatManager();
        blinkManager = new BlinkManager();
        delayManager = new DelayManager();
        lagManager = new LagManager();
        playerStateManager = new PlayerStateManager();
        friendManager = new FriendManager();
        targetManager = new TargetManager();
        propertyManager = new PropertyManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        EventManager.register(rotationManager);
        EventManager.register(floatManager);
        EventManager.register(blinkManager);
        EventManager.register(delayManager);
        EventManager.register(lagManager);
        EventManager.register(moduleManager);
        EventManager.register(commandManager);
        Myau.moduleManager.modules.put(AimAssist.class, new AimAssist());
        Myau.moduleManager.modules.put(AntiDebuff.class, new AntiDebuff());
        Myau.moduleManager.modules.put(AntiVoid.class, new AntiVoid());
        Myau.moduleManager.modules.put(AutoClicker.class, new AutoClicker());
        Myau.moduleManager.modules.put(AutoHeal.class, new AutoHeal());
        Myau.moduleManager.modules.put(AutoTool.class, new AutoTool());
        Myau.moduleManager.modules.put(BedNuker.class, new BedNuker());
        Myau.moduleManager.modules.put(BedESP.class, new BedESP());
        Myau.moduleManager.modules.put(Blink.class, new Blink());
        Myau.moduleManager.modules.put(Chams.class, new Chams());
        Myau.moduleManager.modules.put(ChestESP.class, new ChestESP());
        Myau.moduleManager.modules.put(ChestStealer.class, new ChestStealer());
        Myau.moduleManager.modules.put(Eagle.class, new Eagle());
        Myau.moduleManager.modules.put(ESP.class, new ESP());
        Myau.moduleManager.modules.put(FastPlace.class, new FastPlace());
        Myau.moduleManager.modules.put(Buffer.class, new Buffer());
        Myau.moduleManager.modules.put(Fly.class, new Fly());
        Myau.moduleManager.modules.put(FullBright.class, new FullBright());
        Myau.moduleManager.modules.put(GhostHand.class, new GhostHand());
        Myau.moduleManager.modules.put(GuiModule.class, new GuiModule());
        Myau.moduleManager.modules.put(HitSelect.class, new HitSelect());
        Myau.moduleManager.modules.put(HUD.class, new HUD());
        Myau.moduleManager.modules.put(MoreKB.class, new MoreKB());
        Myau.moduleManager.modules.put(Indicators.class, new Indicators());
        Myau.moduleManager.modules.put(InvManager.class, new InvManager());
        Myau.moduleManager.modules.put(InvWalk.class, new InvWalk());
        Myau.moduleManager.modules.put(ItemESP.class, new ItemESP());
        Myau.moduleManager.modules.put(Jesus.class, new Jesus());
        Myau.moduleManager.modules.put(KeepSprint.class, new KeepSprint());
        Myau.moduleManager.modules.put(HitBox.class, new HitBox());
        Myau.moduleManager.modules.put(KillAura.class, new KillAura());
        Myau.moduleManager.modules.put(LagRange.class, new LagRange());
        Myau.moduleManager.modules.put(LightningTracker.class, new LightningTracker());
        Myau.moduleManager.modules.put(LongJump.class, new LongJump());
        Myau.moduleManager.modules.put(NameTags.class, new NameTags());
        Myau.moduleManager.modules.put(NickHider.class, new NickHider());
        Myau.moduleManager.modules.put(NoFall.class, new NoFall());
        Myau.moduleManager.modules.put(NoHitDelay.class, new NoHitDelay());
        Myau.moduleManager.modules.put(NoHurtCam.class, new NoHurtCam());
        Myau.moduleManager.modules.put(NoJumpDelay.class, new NoJumpDelay());
        Myau.moduleManager.modules.put(NoRotate.class, new NoRotate());
        Myau.moduleManager.modules.put(NoSlow.class, new NoSlow());
        Myau.moduleManager.modules.put(Reach.class, new Reach());
        Myau.moduleManager.modules.put(SafeWalk.class, new SafeWalk());
        Myau.moduleManager.modules.put(Scaffold.class, new Scaffold());
        Myau.moduleManager.modules.put(Block.class, new Block());
        Myau.moduleManager.modules.put(Spammer.class, new Spammer());
        Myau.moduleManager.modules.put(Speed.class, new Speed());
        Myau.moduleManager.modules.put(Sprint.class, new Sprint());
        Myau.moduleManager.modules.put(TargetHUD.class, new TargetHUD());
        Myau.moduleManager.modules.put(TargetStrafe.class, new TargetStrafe());
        Myau.moduleManager.modules.put(Tracers.class, new Tracers());
        Myau.moduleManager.modules.put(Trajectories.class, new Trajectories());
        Myau.moduleManager.modules.put(GlobalFont.class, new GlobalFont());
        Myau.moduleManager.modules.put(NotificationModule.class, new NotificationModule());
        Myau.moduleManager.modules.put(Velocity.class, new Velocity());
        Myau.moduleManager.modules.put(ViewClip.class, new ViewClip());
        Myau.moduleManager.modules.put(Wtap.class, new Wtap());
        Myau.moduleManager.modules.put(Xray.class, new Xray());
        Myau.commandManager.commands.add(new BindCommand());
        Myau.commandManager.commands.add(new ConfigCommand());
        Myau.commandManager.commands.add(new DenickCommand());
        Myau.commandManager.commands.add(new FriendCommand());
        Myau.commandManager.commands.add(new HelpCommand());
        Myau.commandManager.commands.add(new HideCommand());
        Myau.commandManager.commands.add(new IgnCommand());
        Myau.commandManager.commands.add(new ItemCommand());
        Myau.commandManager.commands.add(new ListCommand());
        Myau.commandManager.commands.add(new ModuleCommand());
        Myau.commandManager.commands.add(new PlayerCommand());
        Myau.commandManager.commands.add(new ShowCommand());
        Myau.commandManager.commands.add(new TargetCommand());
        Myau.commandManager.commands.add(new ToggleCommand());
        Myau.commandManager.commands.add(new VclipCommand());
        for (Module module : Myau.moduleManager.modules.values()) {
            ArrayList<Property> properties = new ArrayList<Property>();
            for (Field field : module.getClass().getDeclaredFields()) {
                Object obj;
                field.setAccessible(true);
                try {
                    obj = field.get(module);
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (!(obj instanceof Property)) continue;
                ((Property)obj).setOwner(module);
                properties.add((Property)obj);
            }
            Myau.propertyManager.properties.put(module.getClass(), properties);
            EventManager.register(module);
        }
        Config config = new Config("default", true);
        if (config.file.exists()) {
            config.load();
        }
        if (Myau.friendManager.file.exists()) {
            friendManager.load();
        }
        if (Myau.targetManager.file.exists()) {
            targetManager.load();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(config::save));
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(Myau.class.getResourceAsStream("/version.json")), StandardCharsets.UTF_8);){
            JsonObject modInfo = new JsonParser().parse(reader).getAsJsonObject();
            version = modInfo.get("version").getAsString();
        }
        catch (Exception e) {
            version = "dev";
        }
    }
}

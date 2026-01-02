package myau.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import myau.Myau;
import myau.module.Module;
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
import myau.ui.Component;
import myau.ui.components.CategoryComponent;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

public class ClickGui
extends GuiScreen {
    private static ClickGui instance;
    private final File configFile = new File("./config/Myau/", "clickgui.txt");
    private final ArrayList<CategoryComponent> categoryList;

    public ClickGui() {
        instance = this;
        ArrayList<Module> combatModules = new ArrayList<Module>();
        combatModules.add(Myau.moduleManager.getModule(AimAssist.class));
        combatModules.add(Myau.moduleManager.getModule(AutoClicker.class));
        combatModules.add(Myau.moduleManager.getModule(KillAura.class));
        combatModules.add(Myau.moduleManager.getModule(Wtap.class));
        combatModules.add(Myau.moduleManager.getModule(Velocity.class));
        combatModules.add(Myau.moduleManager.getModule(Buffer.class));
        combatModules.add(Myau.moduleManager.getModule(Reach.class));
        combatModules.add(Myau.moduleManager.getModule(TargetStrafe.class));
        combatModules.add(Myau.moduleManager.getModule(NoHitDelay.class));
        combatModules.add(Myau.moduleManager.getModule(LagRange.class));
        combatModules.add(Myau.moduleManager.getModule(HitBox.class));
        combatModules.add(Myau.moduleManager.getModule(MoreKB.class));
        combatModules.add(Myau.moduleManager.getModule(HitSelect.class));
        ArrayList<Module> movementModules = new ArrayList<Module>();
        movementModules.add(Myau.moduleManager.getModule(Fly.class));
        movementModules.add(Myau.moduleManager.getModule(Speed.class));
        movementModules.add(Myau.moduleManager.getModule(LongJump.class));
        movementModules.add(Myau.moduleManager.getModule(Sprint.class));
        movementModules.add(Myau.moduleManager.getModule(SafeWalk.class));
        movementModules.add(Myau.moduleManager.getModule(Jesus.class));
        movementModules.add(Myau.moduleManager.getModule(Blink.class));
        movementModules.add(Myau.moduleManager.getModule(NoFall.class));
        movementModules.add(Myau.moduleManager.getModule(NoSlow.class));
        movementModules.add(Myau.moduleManager.getModule(KeepSprint.class));
        movementModules.add(Myau.moduleManager.getModule(Eagle.class));
        movementModules.add(Myau.moduleManager.getModule(NoJumpDelay.class));
        movementModules.add(Myau.moduleManager.getModule(AntiVoid.class));
        ArrayList<Module> renderModules = new ArrayList<Module>();
        renderModules.add(Myau.moduleManager.getModule(ESP.class));
        renderModules.add(Myau.moduleManager.getModule(Chams.class));
        renderModules.add(Myau.moduleManager.getModule(FullBright.class));
        renderModules.add(Myau.moduleManager.getModule(Tracers.class));
        renderModules.add(Myau.moduleManager.getModule(NameTags.class));
        renderModules.add(Myau.moduleManager.getModule(Xray.class));
        renderModules.add(Myau.moduleManager.getModule(TargetHUD.class));
        renderModules.add(Myau.moduleManager.getModule(Indicators.class));
        renderModules.add(Myau.moduleManager.getModule(BedESP.class));
        renderModules.add(Myau.moduleManager.getModule(ItemESP.class));
        renderModules.add(Myau.moduleManager.getModule(ViewClip.class));
        renderModules.add(Myau.moduleManager.getModule(NoHurtCam.class));
        renderModules.add(Myau.moduleManager.getModule(HUD.class));
        renderModules.add(Myau.moduleManager.getModule(GuiModule.class));
        renderModules.add(Myau.moduleManager.getModule(ChestESP.class));
        renderModules.add(Myau.moduleManager.getModule(Trajectories.class));
        renderModules.add(Myau.moduleManager.getModule(NotificationModule.class));
        renderModules.add(Myau.moduleManager.getModule(GlobalFont.class));
        ArrayList<Module> playerModules = new ArrayList<Module>();
        playerModules.add(Myau.moduleManager.getModule(AutoHeal.class));
        playerModules.add(Myau.moduleManager.getModule(AutoTool.class));
        playerModules.add(Myau.moduleManager.getModule(ChestStealer.class));
        playerModules.add(Myau.moduleManager.getModule(InvManager.class));
        playerModules.add(Myau.moduleManager.getModule(InvWalk.class));
        playerModules.add(Myau.moduleManager.getModule(Scaffold.class));
        playerModules.add(Myau.moduleManager.getModule(Block.class));
        playerModules.add(Myau.moduleManager.getModule(FastPlace.class));
        playerModules.add(Myau.moduleManager.getModule(GhostHand.class));
        playerModules.add(Myau.moduleManager.getModule(AntiDebuff.class));
        ArrayList<Module> miscModules = new ArrayList<Module>();
        miscModules.add(Myau.moduleManager.getModule(Spammer.class));
        miscModules.add(Myau.moduleManager.getModule(BedNuker.class));
        miscModules.add(Myau.moduleManager.getModule(LightningTracker.class));
        miscModules.add(Myau.moduleManager.getModule(NoRotate.class));
        miscModules.add(Myau.moduleManager.getModule(NickHider.class));
        Comparator<Module> comparator = Comparator.comparing(m -> m.getName().toLowerCase());
        combatModules.sort(comparator);
        movementModules.sort(comparator);
        renderModules.sort(comparator);
        playerModules.sort(comparator);
        miscModules.sort(comparator);
        HashSet<Module> registered = new HashSet<Module>();
        registered.addAll(combatModules);
        registered.addAll(movementModules);
        registered.addAll(renderModules);
        registered.addAll(playerModules);
        registered.addAll(miscModules);
        for (Module module : Myau.moduleManager.modules.values()) {
            if (registered.contains(module)) continue;
            throw new RuntimeException(module.getClass().getName() + " is unregistered to click gui.");
        }
        this.categoryList = new ArrayList();
        int topOffset = 5;
        CategoryComponent combat = new CategoryComponent("Combat", combatModules);
        combat.setY(topOffset);
        this.categoryList.add(combat);
        CategoryComponent movement = new CategoryComponent("Movement", movementModules);
        movement.setY(topOffset += 20);
        this.categoryList.add(movement);
        CategoryComponent render = new CategoryComponent("Render", renderModules);
        render.setY(topOffset += 20);
        this.categoryList.add(render);
        CategoryComponent player = new CategoryComponent("Player", playerModules);
        player.setY(topOffset += 20);
        this.categoryList.add(player);
        CategoryComponent misc = new CategoryComponent("Misc", miscModules);
        misc.setY(topOffset += 20);
        this.categoryList.add(misc);
        this.loadPositions();
    }

    public static ClickGui getInstance() {
        return instance;
    }

    public void func_73866_w_() {
        super.func_73866_w_();
    }

    public void func_73863_a(int x, int y, float p) {
        ClickGui.func_73734_a((int)0, (int)0, (int)this.field_146294_l, (int)this.field_146295_m, (int)new Color(0, 0, 0, 100).getRGB());
        this.field_146297_k.field_71466_p.func_175063_a("Myau " + Myau.version, 4.0f, (float)(this.field_146295_m - 3 - this.field_146297_k.field_71466_p.field_78288_b * 2), new Color(60, 162, 253).getRGB());
        this.field_146297_k.field_71466_p.func_175063_a("dev, ksyz", 4.0f, (float)(this.field_146295_m - 3 - this.field_146297_k.field_71466_p.field_78288_b), new Color(60, 162, 253).getRGB());
        for (CategoryComponent category : this.categoryList) {
            category.render(this.field_146289_q);
            category.handleDrag(x, y);
            for (Component module : category.getModules()) {
                module.update(x, y);
            }
        }
        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            int scrollDir = wheel > 0 ? 1 : -1;
            for (CategoryComponent category : this.categoryList) {
                category.onScroll(x, y, scrollDir);
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    public void func_73864_a(int x, int y, int mouseButton) {
        btnCat = this.categoryList.iterator();
        block0: while (true) {
            if (!btnCat.hasNext()) {
                return;
            }
            category = btnCat.next();
            if (category.insideArea(x, y) && !category.isHovered(x, y) && !category.mousePressed(x, y) && mouseButton == 0) {
                category.mousePressed(true);
                category.xx = x - category.getX();
                category.yy = y - category.getY();
            }
            if (category.mousePressed(x, y) && mouseButton == 0) {
                category.setOpened(category.isOpened() == false);
            }
            if (category.isHovered(x, y) && mouseButton == 0) {
                category.setPin(category.isPin() == false);
            }
            if (!category.isOpened() || category.getModules().isEmpty()) continue;
            var6_6 = category.getModules().iterator();
            while (true) {
                if (var6_6.hasNext()) ** break;
                continue block0;
                c = var6_6.next();
                c.mouseDown(x, y, mouseButton);
            }
            break;
        }
    }

    /*
     * Unable to fully structure code
     */
    public void func_146286_b(int x, int y, int mouseButton) {
        for (CategoryComponent categoryComponent : this.categoryList) {
            if (mouseButton != 0) continue;
            categoryComponent.mousePressed(false);
        }
        iterator = this.categoryList.iterator();
        block1: while (true) {
            if (!iterator.hasNext()) {
                return;
            }
            categoryComponent = iterator.next();
            if (!categoryComponent.isOpened() || categoryComponent.getModules().isEmpty()) continue;
            var6_6 = categoryComponent.getModules().iterator();
            while (true) {
                if (var6_6.hasNext()) ** break;
                continue block1;
                component = var6_6.next();
                component.mouseReleased(x, y, mouseButton);
            }
            break;
        }
    }

    /*
     * Unable to fully structure code
     */
    public void func_73869_a(char typedChar, int key) {
        if (key != 1) {
            btnCat = this.categoryList.iterator();
            while (true) {
                if (!btnCat.hasNext()) {
                    return;
                }
                cat = btnCat.next();
                if (!cat.isOpened() || cat.getModules().isEmpty()) continue;
                var5_5 = cat.getModules().iterator();
                while (true) {
                    if (!var5_5.hasNext()) ** break;
                    component = var5_5.next();
                    component.keyTyped(typedChar, key);
                }
                break;
            }
        }
        this.field_146297_k.func_147108_a(null);
    }

    public void func_146281_b() {
        this.savePositions();
    }

    public boolean func_73868_f() {
        return false;
    }

    private void savePositions() {
        JsonObject json = new JsonObject();
        for (CategoryComponent cat : this.categoryList) {
            JsonObject pos = new JsonObject();
            pos.addProperty("x", cat.getX());
            pos.addProperty("y", cat.getY());
            pos.addProperty("open", cat.isOpened());
            json.add(cat.getName(), pos);
        }
        try (FileWriter writer = new FileWriter(this.configFile);){
            new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)json, (Appendable)writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPositions() {
        if (!this.configFile.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(this.configFile);){
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            for (CategoryComponent cat : this.categoryList) {
                if (!json.has(cat.getName())) continue;
                JsonObject pos = json.getAsJsonObject(cat.getName());
                cat.setX(pos.get("x").getAsInt());
                cat.setY(pos.get("y").getAsInt());
                cat.setOpened(pos.get("open").getAsBoolean());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

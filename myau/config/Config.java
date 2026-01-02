package myau.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import myau.Myau;
import myau.mixin.IAccessorMinecraft;
import myau.module.Module;
import myau.property.Property;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;

public class Config {
    public static Minecraft mc = Minecraft.func_71410_x();
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public String name;
    public File file;
    public static String lastConfig;

    public Config(String name, boolean newConfig) {
        this.name = name;
        lastConfig = name;
        if (name.equals("!") || name.equals("default")) {
            this.name = "default";
        }
        this.file = new File("./config/Myau/", String.format("%s.json", this.name));
        try {
            this.file.getParentFile().mkdirs();
            if (newConfig) {
                ((IAccessorMinecraft)mc).getLogger().info(String.format("Created: %s", this.file.getName()));
            }
        }
        catch (Exception e) {
            ((IAccessorMinecraft)mc).getLogger().error(e.getMessage());
        }
    }

    public void load() {
        try {
            if (!this.file.exists()) {
                ChatUtil.sendFormatted(String.format("%sConfig file not found (&c&o%s&r). Creating default config...&r", Myau.clientName, this.file.getName()));
                this.save();
                return;
            }
            JsonElement parsed = new JsonParser().parse(new BufferedReader(new FileReader(this.file)));
            if (parsed == null || !parsed.isJsonObject()) {
                ChatUtil.sendFormatted(String.format("%sInvalid config format (&c&o%s&r)&r", Myau.clientName, this.file.getName()));
                return;
            }
            JsonObject jsonObject = parsed.getAsJsonObject();
            for (Module module : Myau.moduleManager.modules.values()) {
                JsonElement hidden;
                JsonElement key;
                JsonElement toggled;
                JsonElement moduleObj = jsonObject.get(module.getName());
                if (moduleObj == null || !moduleObj.isJsonObject()) continue;
                JsonObject object = moduleObj.getAsJsonObject();
                ArrayList<Property<?>> list = Myau.propertyManager.properties.get(module.getClass());
                if (list != null) {
                    for (Property<?> property : list) {
                        if (!object.has(property.getName())) continue;
                        try {
                            property.read(object);
                        }
                        catch (Exception e) {
                            ((IAccessorMinecraft)mc).getLogger().warn(String.format("Failed to load property %s for module %s", property.getName(), module.getName()));
                        }
                    }
                }
                if (object.has("toggled") && (toggled = object.get("toggled")) != null && toggled.isJsonPrimitive()) {
                    module.setEnabled(toggled.getAsBoolean());
                }
                if (object.has("key") && (key = object.get("key")) != null && key.isJsonPrimitive()) {
                    module.setKey(key.getAsInt());
                }
                if (!object.has("hidden") || (hidden = object.get("hidden")) == null || !hidden.isJsonPrimitive()) continue;
                module.setHidden(hidden.getAsBoolean());
            }
            ChatUtil.sendFormatted(String.format("%sConfig has been loaded (&a&o%s&r)&r", Myau.clientName, this.file.getName()));
        }
        catch (FileNotFoundException e) {
            ChatUtil.sendFormatted(String.format("%sConfig file not found (&c&o%s&r)&r", Myau.clientName, this.file.getName()));
        }
        catch (JsonSyntaxException e) {
            ChatUtil.sendFormatted(String.format("%sConfig has invalid JSON syntax (&c&o%s&r)&r", Myau.clientName, this.file.getName()));
            ((IAccessorMinecraft)mc).getLogger().error("JSON Syntax Error: " + e.getMessage());
        }
        catch (Exception e) {
            ((IAccessorMinecraft)mc).getLogger().error("Error loading config: " + e.getMessage());
            ChatUtil.sendFormatted(String.format("%sConfig couldn't be loaded (&c&o%s&r)&r", Myau.clientName, this.file.getName()));
        }
    }

    public void save() {
        try {
            if (!this.file.getParentFile().exists()) {
                this.file.getParentFile().mkdirs();
            }
            JsonObject object = new JsonObject();
            for (Module module : Myau.moduleManager.modules.values()) {
                JsonObject moduleObject = new JsonObject();
                moduleObject.addProperty("toggled", module.isEnabled());
                moduleObject.addProperty("key", module.getKey());
                moduleObject.addProperty("hidden", module.isHidden());
                ArrayList<Property<?>> list = Myau.propertyManager.properties.get(module.getClass());
                if (list != null) {
                    for (Property<?> property : list) {
                        try {
                            property.write(moduleObject);
                        }
                        catch (Exception e) {
                            ((IAccessorMinecraft)mc).getLogger().warn(String.format("Failed to save property %s for module %s", property.getName(), module.getName()));
                        }
                    }
                }
                object.add(module.getName(), moduleObject);
            }
            PrintWriter printWriter = new PrintWriter(new FileWriter(this.file));
            printWriter.println(gson.toJson(object));
            printWriter.close();
            ChatUtil.sendFormatted(String.format("%sConfig has been saved (&a&o%s&r)&r", Myau.clientName, this.file.getName()));
        }
        catch (IOException e) {
            ((IAccessorMinecraft)mc).getLogger().error("Error saving config: " + e.getMessage());
            ChatUtil.sendFormatted(String.format("%sConfig couldn't be saved (&c&o%s&r)&r", Myau.clientName, this.file.getName()));
        }
    }
}

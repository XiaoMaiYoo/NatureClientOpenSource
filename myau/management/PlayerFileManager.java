package myau.management;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;

public abstract class PlayerFileManager {
    public static Minecraft mc = Minecraft.func_71410_x();
    public ArrayList<String> players = new ArrayList();
    public File file;
    public Color color;

    public PlayerFileManager(File file, Color color) {
        this.file = file;
        this.color = color;
    }

    public void load() {
        if (!this.file.exists()) {
            try {
                if ((this.file.getParentFile().exists() || this.file.getParentFile().mkdirs()) && this.file.createNewFile()) {
                    System.out.printf("File created: %s%n", this.file.getName());
                }
            }
            catch (IOException e) {
                System.err.println("Error creating file: " + e.getMessage());
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file));){
            this.players.clear();
            this.players.addAll(reader.lines().map(String::trim).collect(Collectors.toList()));
        }
        catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public void save() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(this.file));){
            writer.print(String.join((CharSequence)"\n", this.players));
        }
        catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    public String add(String name) {
        if (this.isFriend(name)) {
            return null;
        }
        this.players.add(name);
        this.save();
        return name;
    }

    public String remove(String name) {
        for (String player : this.players) {
            if (!player.equalsIgnoreCase(name)) continue;
            this.players.remove(player);
            this.save();
            return player;
        }
        return null;
    }

    public void clear() {
        this.players.clear();
        this.save();
    }

    public boolean isFriend(String string) {
        return this.players.stream().anyMatch(string2 -> string2.equalsIgnoreCase(string));
    }

    public ArrayList<String> getPlayers() {
        return this.players;
    }

    public Color getColor() {
        return this.color;
    }
}

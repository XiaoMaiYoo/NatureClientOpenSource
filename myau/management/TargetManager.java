package myau.management;

import java.awt.Color;
import java.io.File;
import myau.enums.ChatColors;
import myau.management.PlayerFileManager;

public class TargetManager
extends PlayerFileManager {
    public TargetManager() {
        super(new File("./config/Myau/", "enemies.txt"), new Color(ChatColors.DARK_RED.toAwtColor()));
    }
}

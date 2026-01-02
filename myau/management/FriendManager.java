package myau.management;

import java.awt.Color;
import java.io.File;
import myau.enums.ChatColors;
import myau.management.PlayerFileManager;

public class FriendManager
extends PlayerFileManager {
    public FriendManager() {
        super(new File("./config/Myau/", "friends.txt"), new Color(ChatColors.DARK_GREEN.toAwtColor()));
    }
}

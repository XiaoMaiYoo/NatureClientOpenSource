package myau.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import myau.Myau;
import myau.command.Command;
import myau.enums.ChatColors;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PlayerCommand
extends Command {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public PlayerCommand() {
        super(new ArrayList<String>(Arrays.asList("playerlist", "players")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        ArrayList<String> players = new ArrayList<String>();
        for (NetworkPlayerInfo playerInfo : mc.func_147114_u().func_175106_d()) {
            players.add(playerInfo.func_178845_a().getName().replace("\u00a7", "&"));
        }
        if (players.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%sNo players&r", Myau.clientName));
        } else {
            ChatUtil.sendRaw(String.format(ChatColors.formatColor("%sPlayers:&r %s"), ChatColors.formatColor(Myau.clientName), String.join((CharSequence)", ", players)));
        }
    }
}

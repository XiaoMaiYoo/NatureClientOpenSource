package myau.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import myau.Myau;
import myau.command.Command;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.util.ChatUtil;
import net.minecraft.network.play.client.C01PacketChatMessage;

public class CommandManager {
    public ArrayList<Command> commands = new ArrayList();

    public void handleCommand(String string) {
        List<String> params = Arrays.asList(string.substring(1).trim().split("\\s+"));
        ArrayList<String> arrayList = new ArrayList<String>(params);
        if (params.get(0).isEmpty()) {
            ChatUtil.sendFormatted(String.format("%sUnknown command&r", Myau.clientName).replace("&", "\u00a7"));
        } else {
            for (Command command : Myau.commandManager.commands) {
                for (String name : command.names) {
                    if (!params.get(0).equalsIgnoreCase(name)) continue;
                    command.runCommand(arrayList);
                    return;
                }
            }
            ChatUtil.sendFormatted(String.format("%sUnknown command (&o%s&r)&r", Myau.clientName, params.get(0)).replace("&", "\u00a7"));
        }
    }

    public boolean isTypingCommand(String string) {
        if (string == null || string.length() < 2) {
            return false;
        }
        return string.charAt(0) == '.' && Character.isLetterOrDigit(string.charAt(1));
    }

    @EventTarget(value=0)
    public void onPacket(PacketEvent event) {
        String msg;
        if (event.getType() == EventType.SEND && event.getPacket() instanceof C01PacketChatMessage && this.isTypingCommand(msg = ((C01PacketChatMessage)event.getPacket()).func_149439_c())) {
            event.setCancelled(true);
            this.handleCommand(msg);
        }
    }
}

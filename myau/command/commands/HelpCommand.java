package myau.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import myau.Myau;
import myau.command.Command;
import myau.command.commands.ModuleCommand;
import myau.util.ChatUtil;

public class HelpCommand
extends Command {
    public HelpCommand() {
        super(new ArrayList<String>(Arrays.asList("help", "commands")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (!Myau.moduleManager.modules.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%sCommands:&r", Myau.clientName));
            for (Command command : Myau.commandManager.commands) {
                if (command instanceof ModuleCommand) continue;
                ChatUtil.sendFormatted(String.format("&7\u00bb&r .%s&r", String.join((CharSequence)" &7/&r .", command.names)));
            }
        }
    }
}

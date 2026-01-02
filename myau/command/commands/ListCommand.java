package myau.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import myau.Myau;
import myau.command.Command;
import myau.module.Module;
import myau.util.ChatUtil;

public class ListCommand
extends Command {
    public ListCommand() {
        super(new ArrayList<String>(Arrays.asList("list", "l", "modules", "myau")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (!Myau.moduleManager.modules.isEmpty()) {
            ChatUtil.sendFormatted(String.format("%sModules:&r", Myau.clientName));
            for (Module module : Myau.moduleManager.modules.values()) {
                ChatUtil.sendFormatted(String.format("%s\u00bb&r %s&r", module.isHidden() ? "&8" : "&7", module.formatModule()));
            }
        }
    }
}

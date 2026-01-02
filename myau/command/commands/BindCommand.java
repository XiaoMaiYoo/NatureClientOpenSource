package myau.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import myau.Myau;
import myau.command.Command;
import myau.module.Module;
import myau.util.ChatUtil;
import myau.util.KeyBindUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class BindCommand
extends Command {
    public BindCommand() {
        super(new ArrayList<String>(Arrays.asList("bind", "b")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() < 3) {
            if (args.size() == 2 && (args.get(1).equalsIgnoreCase("l") || args.get(1).equalsIgnoreCase("list"))) {
                List modules = Myau.moduleManager.modules.values().stream().filter(module -> module.getKey() != 0).collect(Collectors.toList());
                if (modules.isEmpty()) {
                    ChatUtil.sendFormatted(String.format("%sNo binds&r", Myau.clientName));
                } else {
                    ChatUtil.sendFormatted(String.format("%sBinds:&r", Myau.clientName));
                    for (Module module2 : modules) {
                        ChatUtil.sendFormatted(String.format("%s\u00bb&r %s&r", module2.isHidden() ? "&8" : "&7", module2.formatModule()));
                    }
                }
            } else {
                ChatUtil.sendFormatted(String.format("%sUsage: .%s <&omodule&r> <&okey&r>&r | .%s <&omodule&r> &onone&r | .%s &olist&r", Myau.clientName, args.get(0).toLowerCase(Locale.ROOT), args.get(0).toLowerCase(Locale.ROOT), args.get(0).toLowerCase(Locale.ROOT)));
            }
        } else {
            String keyInput = args.get(2).toUpperCase();
            int keyIndex = 0;
            if (keyInput.equalsIgnoreCase("NONE") || keyInput.equalsIgnoreCase("NULL") || keyInput.equalsIgnoreCase("0")) {
                keyIndex = 0;
            } else {
                int buttonIndex;
                keyIndex = Keyboard.getKeyIndex((String)keyInput);
                if (keyIndex == 0 && (buttonIndex = Mouse.getButtonIndex((String)keyInput)) != -1) {
                    keyIndex = buttonIndex - 100;
                }
            }
            if (!args.get(1).equals("*")) {
                Module module3 = Myau.moduleManager.getModule(args.get(1));
                if (module3 == null) {
                    ChatUtil.sendFormatted(String.format("%sModule not found (&o%s&r)&r", Myau.clientName, args.get(1)));
                } else {
                    module3.setKey(keyIndex);
                    if (keyIndex == 0) {
                        ChatUtil.sendFormatted(String.format("%sUnbind &o%s&r", Myau.clientName, module3.getName()));
                    } else {
                        ChatUtil.sendFormatted(String.format("%sBound &o%s&r to &l[%s]&r", Myau.clientName, module3.getName(), KeyBindUtil.getKeyName(keyIndex)));
                    }
                }
            } else {
                for (Module module4 : Myau.moduleManager.modules.values()) {
                    module4.setKey(keyIndex);
                }
                if (keyIndex == 0) {
                    ChatUtil.sendFormatted(String.format("%sUnbind all modules&r", Myau.clientName));
                } else {
                    ChatUtil.sendFormatted(String.format("%sBind all modules to &l[%s]&r", Myau.clientName, KeyBindUtil.getKeyName(keyIndex)));
                }
            }
        }
    }
}

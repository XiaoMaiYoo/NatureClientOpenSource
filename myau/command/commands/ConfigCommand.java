package myau.command.commands;

import java.awt.Desktop;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import myau.Myau;
import myau.command.Command;
import myau.config.Config;
import myau.enums.ChatColors;
import myau.util.ChatUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class ConfigCommand
extends Command {
    private static final FileFilter FILE_FILTER = new WildcardFileFilter("*.json", IOCase.INSENSITIVE);

    public ConfigCommand() {
        super(new ArrayList<String>(Arrays.asList("config", "cfg", "c")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() < 2) {
            String command = args.get(0).toLowerCase(Locale.ROOT);
            ChatUtil.sendFormatted(String.format("%sUsage: .%s &oload&r/&osave&r <&oname&r> | .%s &olist&r | .%s &ofolder&r", Myau.clientName, command, command, command));
        } else {
            String sub;
            String subCommand = args.get(1);
            if (subCommand.equalsIgnoreCase("l")) {
                subCommand = args.size() < 3 ? "list" : "load";
            }
            switch (sub = subCommand.toLowerCase(Locale.ROOT)) {
                case "load": 
                case "reload": {
                    if (args.size() < 3) {
                        ChatUtil.sendFormatted(String.format("%sMissing config name (use '&odefault&r' or '&o!&r' to load default config)&r", Myau.clientName));
                        return;
                    }
                    new Config(args.get(2), false).load();
                    return;
                }
                case "s": 
                case "save": {
                    if (args.size() < 3) {
                        new Config(Config.lastConfig, true).save();
                        return;
                    }
                    new Config(args.get(2), true).save();
                    return;
                }
                case "list": {
                    try {
                        File[] configs = new File("./config/Myau/").listFiles(FILE_FILTER);
                        if (configs == null) {
                            throw new Exception();
                        }
                        if (configs.length == 0) {
                            ChatUtil.sendFormatted(String.format("%sNo configs found (&o%s&r)&r", Myau.clientName, "./config/Myau/"));
                        }
                        Arrays.sort(configs, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
                        ChatUtil.sendFormatted(String.format("%sConfigs:&r", Myau.clientName));
                        for (File file : configs) {
                            String formatted = ChatColors.formatColor(String.format("&7\u00bb&r &o%s&r", file.getName()));
                            String config = String.format(".config load %s", FilenameUtils.removeExtension((String)file.getName()));
                            ChatUtil.send(new ChatComponentText(formatted).func_150255_a(new ChatStyle().func_150241_a(new ClickEvent(ClickEvent.Action.RUN_COMMAND, config)).func_150209_a(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (IChatComponent)new ChatComponentText(config)))));
                        }
                    }
                    catch (Exception e) {
                        ChatUtil.sendFormatted(String.format("%sFailed to read (&o%s&r)&r", Myau.clientName, "./config/Myau/"));
                    }
                    return;
                }
                case "f": 
                case "folder": 
                case "dir": 
                case "directory": {
                    try {
                        Desktop.getDesktop().open(new File("./config/Myau/"));
                    }
                    catch (Exception e) {
                        ChatUtil.sendFormatted(String.format("%sFailed to open (&o%s&r)&r", Myau.clientName, "./config/Myau/"));
                    }
                    return;
                }
            }
            ChatUtil.sendFormatted(String.format("%sInvalid argument (&o%s&r)&r", Myau.clientName, args.get(1)));
        }
    }
}

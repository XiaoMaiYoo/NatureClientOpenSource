package myau.command.commands;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import myau.Myau;
import myau.command.Command;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.StringUtils;

public class IgnCommand
extends Command {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public IgnCommand() {
        super(new ArrayList<String>(Arrays.asList("username", "name", "ign")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        String username;
        Session session = mc.func_110432_I();
        if (session != null && !StringUtils.func_151246_b((String)(username = session.func_111285_a()))) {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(username), null);
                ChatUtil.sendFormatted(String.format("%sYour username has been copied to the clipboard (&o%s&r)&r", Myau.clientName, username));
            }
            catch (Exception e) {
                ChatUtil.sendFormatted(String.format("%sFailed to copy&r", Myau.clientName));
            }
        }
    }
}

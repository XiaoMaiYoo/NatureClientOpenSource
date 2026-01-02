package myau.command.commands;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import myau.Myau;
import myau.command.Command;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;

public class VclipCommand
extends Command {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    public VclipCommand() {
        super(new ArrayList<String>(Collections.singletonList("vclip")));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    @Override
    public void runCommand(ArrayList<String> args) {
        block4: {
            if (args.size() < 2) break block4;
            double distance = 0.0;
            try {
                distance = Double.parseDouble(args.get(1));
            }
            catch (NumberFormatException numberFormatException) {
                VclipCommand.mc.field_71439_g.func_70634_a(VclipCommand.mc.field_71439_g.field_70165_t, VclipCommand.mc.field_71439_g.field_70163_u + distance, VclipCommand.mc.field_71439_g.field_70161_v);
                ChatUtil.sendFormatted(String.format("%sClipped (%s blocks)", Myau.clientName, df.format(distance)));
                catch (Throwable throwable) {
                    VclipCommand.mc.field_71439_g.func_70634_a(VclipCommand.mc.field_71439_g.field_70165_t, VclipCommand.mc.field_71439_g.field_70163_u + distance, VclipCommand.mc.field_71439_g.field_70161_v);
                    ChatUtil.sendFormatted(String.format("%sClipped (%s blocks)", Myau.clientName, df.format(distance)));
                    throw throwable;
                }
            }
            VclipCommand.mc.field_71439_g.func_70634_a(VclipCommand.mc.field_71439_g.field_70165_t, VclipCommand.mc.field_71439_g.field_70163_u + distance, VclipCommand.mc.field_71439_g.field_70161_v);
            ChatUtil.sendFormatted(String.format("%sClipped (%s blocks)", Myau.clientName, df.format(distance)));
            return;
        }
        ChatUtil.sendFormatted(String.format("%sUsage: .%s <&odistance&r>&r", Myau.clientName, args.get(0).toLowerCase(Locale.ROOT)));
    }
}

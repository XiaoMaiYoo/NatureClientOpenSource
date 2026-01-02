package myau.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import myau.Myau;
import myau.command.Command;
import myau.module.Module;
import myau.property.Property;
import myau.property.properties.BooleanProperty;
import myau.util.ChatUtil;

public class ModuleCommand
extends Command {
    public ModuleCommand() {
        super(new ArrayList<String>(Myau.moduleManager.modules.values().stream().map(Module::getName).collect(Collectors.toList())));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        Module module = Myau.moduleManager.getModule(args.get(0));
        if (args.size() >= 2) {
            Property<?> property = Myau.propertyManager.getProperty(module, args.get(1));
            if (property == null) {
                ChatUtil.sendFormatted(String.format("%s%s has no property &o%s&r", Myau.clientName, module.getName(), args.get(1)));
            } else if (args.size() < 3 && !(property instanceof BooleanProperty)) {
                ChatUtil.sendFormatted(String.format("%s%s: &o%s&r is set to %s&r (%s)&r", Myau.clientName, module.getName(), property.getName(), property.formatValue(), property.getValuePrompt()));
            } else {
                String newValue = args.size() < 3 ? null : String.join((CharSequence)" ", args.subList(2, args.size()));
                try {
                    if (property.parseString(newValue)) {
                        ChatUtil.sendFormatted(String.format("%s%s: &o%s&r has been set to %s&r", Myau.clientName, module.getName(), property.getName(), property.formatValue()));
                        return;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                ChatUtil.sendFormatted(String.format("%sInvalid value for property &o%s&r (%s)&r", Myau.clientName, property.getName(), property.getValuePrompt()));
            }
        } else {
            List visible;
            List properties = Myau.propertyManager.properties.get(module.getClass());
            if (properties != null && !(visible = properties.stream().filter(Property::isVisible).collect(Collectors.toList())).isEmpty()) {
                ChatUtil.sendFormatted(String.format("%s%s:&r", Myau.clientName, module.formatModule()));
                for (Property property : visible) {
                    ChatUtil.sendFormatted(String.format("&7\u00bb&r %s: %s&r", property.getName(), property.formatValue()));
                }
                return;
            }
            ChatUtil.sendFormatted(String.format("%s%s has no properties&r", Myau.clientName, module.formatModule()));
        }
    }
}

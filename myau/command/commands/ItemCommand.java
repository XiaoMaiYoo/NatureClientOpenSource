package myau.command.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import myau.Myau;
import myau.command.Command;
import myau.enums.ChatColors;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemCommand
extends Command {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public ItemCommand() {
        super(new ArrayList<String>(Arrays.asList("itemname", "item")));
    }

    private String getItemRegistryName(Item item) {
        if (item == null) {
            return "null";
        }
        try {
            Method getRegistryNameMethod = Item.class.getMethod("getRegistryName", new Class[0]);
            Object result = getRegistryNameMethod.invoke((Object)item, new Object[0]);
            if (result != null) {
                return result.toString();
            }
        }
        catch (NoSuchMethodException e) {
            return this.getLegacyItemName(item);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return this.getLegacyItemName(item);
    }

    private String getLegacyItemName(Item item) {
        try {
            Object registryName = Item.field_150901_e.func_177774_c((Object)item);
            if (registryName != null) {
                return registryName.toString();
            }
            int itemId = Item.func_150891_b((Item)item);
            return "item_" + itemId;
        }
        catch (Exception e) {
            return "unknown_item";
        }
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        ItemStack stack = ItemCommand.mc.field_71439_g.field_71071_by.func_70448_g();
        if (stack != null && stack.func_77973_b() != null) {
            String display = stack.func_82833_r().replace('\u00a7', '&');
            String registryName = this.getItemRegistryName(stack.func_77973_b());
            String compound = stack.func_77942_o() ? stack.func_77978_p().toString().replace('\u00a7', '&') : "";
            ChatUtil.sendRaw(String.format("%s%s (%s) %s", ChatColors.formatColor(Myau.clientName), display, registryName, compound));
        } else {
            ChatUtil.sendRaw(String.format("%sNo item in hand!", ChatColors.formatColor(Myau.clientName)));
        }
    }
}

package me.zford.jobs.bukkit.actions;

import me.zford.jobs.container.ActionInfo;
import me.zford.jobs.container.ActionType;
import me.zford.jobs.container.BaseActionInfo;

import org.bukkit.inventory.ItemStack;

public class ItemActionInfo extends BaseActionInfo implements ActionInfo {
    private ItemStack items;
    public ItemActionInfo(ItemStack items, ActionType type) {
        super(type);
        this.items = items;
    }
    
    @Override
    public String getName() {
        return items.getType().toString();
    }

    @Override
    public String getNameWithSub() {
        return getName()+":"+items.getData();
    }
}

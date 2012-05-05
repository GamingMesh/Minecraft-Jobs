package me.zford.jobs.actions;

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

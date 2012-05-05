package me.zford.jobs.actions;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockActionInfo extends BaseActionInfo implements ActionInfo {
    private Block block;
    public BlockActionInfo(Block block, ActionType type) {
        super(type);
        this.block = block;
    }
    
    @Override
    public String getName() {
        // Normalize GLOWING_REDSTONE_ORE to REDSTONE_ORE
        if (block.getType().equals(Material.GLOWING_REDSTONE_ORE))
            return Material.REDSTONE_ORE.toString();
        
        return block.getType().toString();
    }

    @Override
    public String getNameWithSub() {
        return getName()+":"+block.getData();
    }
}

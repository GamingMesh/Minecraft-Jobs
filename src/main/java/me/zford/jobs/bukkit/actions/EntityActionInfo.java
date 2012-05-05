package me.zford.jobs.bukkit.actions;

import me.zford.jobs.container.ActionInfo;
import me.zford.jobs.container.ActionType;
import me.zford.jobs.container.BaseActionInfo;

import org.bukkit.entity.EntityType;

public class EntityActionInfo extends BaseActionInfo implements ActionInfo {
    private EntityType entity;
    public EntityActionInfo(EntityType entity, ActionType type) {
        super(type);
        this.entity = entity;
    }
    
    @Override
    public String getName() {
        return entity.toString();
    }

    @Override
    public String getNameWithSub() {
        return getName();
    }
}

package me.zford.jobs.actions;

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

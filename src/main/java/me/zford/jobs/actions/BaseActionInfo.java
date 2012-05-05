package me.zford.jobs.actions;

public abstract class BaseActionInfo implements ActionInfo {
    private ActionType type;
    protected BaseActionInfo(ActionType type) {
        this.type = type;
    }

    @Override
    public ActionType getType() {
        return this.type;
    }
}

package me.zford.jobs.container;

public class JobPermission {
    private String node;
    private boolean value;
    private int levelRequirement;
    public JobPermission(String node, boolean value, int levelRequirement) {
        this.node = node;
        this.value = value;
        this.levelRequirement = levelRequirement;
    }
    
    public String getNode() {
        return node;
    }
    
    public boolean getValue() {
        return value;
    }
    
    public int getLevelRequirement() {
        return levelRequirement;
    }
}

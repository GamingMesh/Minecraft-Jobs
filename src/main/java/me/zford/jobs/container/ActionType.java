package me.zford.jobs.container;

public enum ActionType {
    BREAK("Break"),
    PLACE("Place"),
    KILL("Kill"),
    FISH("Fish"),
    CRAFT("Craft"),
    SMELT("Smelt"),
    BREW("Brew"),
    ENCHANT("Enchant"),
    REPAIR("Repair");
    
    private String name;
    private ActionType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}

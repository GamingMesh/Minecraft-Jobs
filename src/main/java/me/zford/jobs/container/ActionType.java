package me.zford.jobs.container;

public enum ActionType {
    BREAK("break"),
    PLACE("place"),
    KILL("kill"),
    FISH("fish"),
    CRAFT("craft"),
    SMELT("smelt"),
    BREW("brew"),
    ENCHANT("enchant");
    
    private String name;
    private ActionType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}

package me.zford.jobs.container;

import me.zford.jobs.resources.jfep.Parser;

public class JobInfo {
    private String name;
    private double baseIncome, baseXp;
    private Parser moneyEquation, xpEquation;
    public JobInfo(String name, double baseIncome, Parser moneyEquation, double baseXp, Parser xpEquation) {
        this.name = name;
        this.baseIncome = baseIncome;
        this.moneyEquation = moneyEquation;
        this.baseXp = baseXp;
        this.xpEquation = xpEquation;
    }
    
    public String getName() {
        return name;
    }
    
    public double getBaseIncome() {
        return baseIncome;
    }
    
    public double getBaseXp() {
        return baseXp;
    }
    
    public double getIncome(int level, int numjobs) {
        moneyEquation.setVariable("joblevel", level);
        moneyEquation.setVariable("numjobs", numjobs);
        moneyEquation.setVariable("baseincome", baseIncome);
        return moneyEquation.getValue();
    }
    
    public double getExperience(int level, int numjobs) {
        xpEquation.setVariable("joblevel", level);
        xpEquation.setVariable("numjobs", numjobs);
        xpEquation.setVariable("baseexperience", baseXp);
        return xpEquation.getValue();
    }
}

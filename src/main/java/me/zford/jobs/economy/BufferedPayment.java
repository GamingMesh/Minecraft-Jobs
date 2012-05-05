package me.zford.jobs.economy;

public class BufferedPayment {
    private String playername;
    private double amount;
    public BufferedPayment(String playername, double amount) {
        this.playername = playername;
        this.amount = amount;
    }
    
    public String getPlayerName() {
        return playername;
    }
    
    public double getAmount() {
        return amount;
    }
}

package model;

import java.sql.Timestamp;

public class Bid {
    private int bidId;
    private int itemId;
    private int buyerId;
    private double amount;
    private Timestamp bidTime;
    private String status;

    public Bid() {
        this.bidTime = new Timestamp(System.currentTimeMillis()); // Initialize with current time
    }

    public Bid(int bidId, int itemId, int buyerId, double amount, Timestamp bidTime, String status) {
        this.bidId = bidId;
        this.itemId = itemId;
        this.buyerId = buyerId;
        this.amount = amount;
        this.bidTime = bidTime;
        this.status = status;
    }

    // Getters and Setters
    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Timestamp getBidTime() { return bidTime; }
    public void setBidTime(Timestamp bidTime) { this.bidTime = bidTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
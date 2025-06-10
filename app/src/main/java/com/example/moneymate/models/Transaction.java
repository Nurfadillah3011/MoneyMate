package com.example.moneymate.models;

import java.io.Serializable;
import java.util.Date;

public class Transaction implements Serializable {
    private int id;
    private double amount;
    private String description;
    private String type; // "income" or "expense"
    private String category;
    private Date date;

    public Transaction() {}

    public Transaction(int id, double amount, String description, String type, String category, Date date) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    // Getters
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public Date getDate() { return date; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setCategory(String category) { this.category = category; }
    public void setDate(Date date) { this.date = date; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", date=" + date +
                '}';
    }
}
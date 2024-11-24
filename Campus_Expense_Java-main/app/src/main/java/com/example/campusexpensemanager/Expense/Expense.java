package com.example.campusexpensemanager.Expense;

public class Expense {
    private int id;
    private String description;
    private String date;
    private double amount;

    public Expense(int id, String description, String date, double amount) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }
}
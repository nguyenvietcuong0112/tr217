package com.moneysaving.moneylove.moneymanager.finance.model;

import com.google.gson.Gson;

import java.io.Serializable;

public class TransactionModel implements Serializable {
    private String transactionType;
    private String amount;
    private String currency;
    private String categoryName;
    private int categoryIcon;
    private String budget;
    private String note;
    private String date;
    private String time;
    private String lender;


    public TransactionModel(String transactionType, String amount, String currency,  String categoryName, int categoryIcon, String budget, String note, String date, String time) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.budget = budget;
        this.note = note;
        this.date = date;
        this.time = time;
    }

    public static String toJson(TransactionModel transaction) {
        return new Gson().toJson(transaction);
    }

    public static TransactionModel fromJson(String json) {
        return new Gson().fromJson(json, TransactionModel.class);
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(int categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setLender(String lender) {
        this.lender = lender;
    }

    public String getTransactionType() { return transactionType; }
    public String getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getBudget() { return budget; }
    public String getNote() { return note; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLender() {
        return lender;
    }

}
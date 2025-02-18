package com.moneysaving.moneylove.moneymanager.finance.model;

import com.google.gson.Gson;

public class TransactionModel {
    private String transactionType;
    private String amount;
    private String currency;
    private String category;
    private String budget;
    private String note;
    private String date;
    private String time;

    public TransactionModel(String transactionType, String amount, String currency, String category, String budget, String note, String date, String time) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
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

    public String getTransactionType() { return transactionType; }
    public String getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getCategory() { return category; }
    public String getBudget() { return budget; }
    public String getNote() { return note; }
    public String getDate() { return date; }
    public String getTime() { return time; }
}
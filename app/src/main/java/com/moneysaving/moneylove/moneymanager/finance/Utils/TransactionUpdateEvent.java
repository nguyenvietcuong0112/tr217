package com.moneysaving.moneylove.moneymanager.finance.Utils;

import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import java.util.List;

public class TransactionUpdateEvent {
    private List<TransactionModel> transactionList;

    public TransactionUpdateEvent(List<TransactionModel> transactionList) {
        this.transactionList = transactionList;
    }

    public List<TransactionModel> getTransactionList() {
        return transactionList;
    }
}
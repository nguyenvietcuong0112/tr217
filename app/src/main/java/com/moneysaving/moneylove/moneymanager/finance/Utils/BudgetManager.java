package com.moneysaving.moneylove.moneymanager.finance.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moneysaving.moneylove.moneymanager.finance.model.BudgetItem;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BudgetManager {
    private static final String PREF_NAME = "BudgetManagerPrefs";
    private static final String KEY_BUDGETS = "budgets";
    private static final String KEY_TOTAL_BUDGET = "total_budget";
    private static final String KEY_TOTAL_EXPENSES = "total_expenses";
    private SharePreferenceUtils sharePreferenceUtils;

    private SharedPreferences prefs;
    private Gson gson;

    public BudgetManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.sharePreferenceUtils = new SharePreferenceUtils(context);
        gson = new Gson();
    }

    public void saveBudgetItem(BudgetItem item) {
        List<BudgetItem> budgets = getBudgetItems();
        budgets.add(item);
        saveBudgetItems(budgets);
    }

    public List<BudgetItem> getBudgetItems() {
        String json = prefs.getString(KEY_BUDGETS, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<BudgetItem>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private void saveBudgetItems(List<BudgetItem> items) {
        String json = gson.toJson(items);
        prefs.edit().putString(KEY_BUDGETS, json).apply();
    }

    public void setTotalBudget(double amount) {
        prefs.edit().putFloat(KEY_TOTAL_BUDGET, (float) amount).apply();
    }

    public double getTotalBudget() {
        return prefs.getFloat(KEY_TOTAL_BUDGET, 0f);
    }

    public void addExpense(double amount) {
        float currentExpenses = prefs.getFloat(KEY_TOTAL_EXPENSES, 0f);
        prefs.edit().putFloat(KEY_TOTAL_EXPENSES, currentExpenses + (float) amount).apply();
    }

    public double getTotalExpenses() {
        return prefs.getFloat(KEY_TOTAL_EXPENSES, 0f);
    }

    public void setTotalExpenses(double amount) {
        prefs.edit().putFloat(KEY_TOTAL_EXPENSES, (float) amount).apply();
    }

    public void updateBudgetExpense(String budgetName, double amount) {
        List<BudgetItem> budgets = getBudgetItems();
        for (BudgetItem budget : budgets) {
            if (budget.getName().equals(budgetName)) {
                budget.addExpense(amount);
                break;
            }
        }
        saveBudgetItems(budgets);
    }

    public double getExpensesForBudget(String budgetName) {
        List<TransactionModel> transactions = sharePreferenceUtils.getTransactionList();
        double totalExpenses = 0.0;
        for (TransactionModel transaction : transactions) {
            if ("Expend".equals(transaction.getTransactionType()) && budgetName.equals(transaction.getBudget())) {
                totalExpenses += Double.parseDouble(transaction.getAmount());
            }
        }
        return totalExpenses;
    }


    // Phương thức cập nhật chi phí của một budget
    public void updateBudgetExpenses(String budgetName) {
        // Lấy danh sách các giao dịch từ SharePreferenceUtils
        List<TransactionModel> transactions = sharePreferenceUtils.getTransactionList();

        // Tính toán tổng chi tiêu cho budget cụ thể
        double totalExpenses = 0.0;
        for (TransactionModel transaction : transactions) {
            if ("Expend".equals(transaction.getTransactionType()) && budgetName.equals(transaction.getBudget())) {
                totalExpenses += Double.parseDouble(transaction.getAmount());
            }
        }

        // Cập nhật spentAmount của budget tương ứng
        List<BudgetItem> budgetItems = getBudgetItems();
        for (BudgetItem item : budgetItems) {
            if (budgetName.equals(item.getName())) {
                item.setSpentAmount(totalExpenses); // Cập nhật spentAmount
                break;
            }
        }

        // Lưu lại danh sách budget đã cập nhật
        saveBudgetItems(budgetItems);
    }
}
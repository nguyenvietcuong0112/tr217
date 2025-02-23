package com.moneysaving.moneylove.moneymanager.finance.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.BudgetManager;
import com.moneysaving.moneylove.moneymanager.finance.Utils.CircularProgressView;
import com.moneysaving.moneylove.moneymanager.finance.adapter.BudgetAdapter;
import com.moneysaving.moneylove.moneymanager.finance.model.BudgetItem;
import java.util.List;

public class BudgetDetailActivity extends AppCompatActivity {
    private CircularProgressView mainProgressView;
    private TextView tvTotalBudget, tvExpenses, tvRemaining;
    private RecyclerView rvBudgets;
    private BudgetManager budgetManager;
    private BudgetAdapter budgetAdapter;
    private LinearLayout btnAddBudget;
    ImageView iv_back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_detail);

        mainProgressView = findViewById(R.id.main_progress_view);
        tvTotalBudget = findViewById(R.id.tv_total_budget);
        tvExpenses = findViewById(R.id.tv_expenses);
        tvRemaining = findViewById(R.id.tv_remaining);
        rvBudgets = findViewById(R.id.rv_budgets);
        iv_back = findViewById(R.id.iv_back);
        btnAddBudget = findViewById(R.id.btn_add_budget);
        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());
        iv_back.setOnClickListener(v -> onBackPressed());


        budgetManager = new BudgetManager(this);
        setupRecyclerView();
        updateUI();
    }

    private void setupRecyclerView() {
        List<BudgetItem> budgetItems = budgetManager.getBudgetItems();
        budgetAdapter = new BudgetAdapter(this, budgetItems, item -> {
            showBudgetDetailDialog(item);

        },budgetManager);
        rvBudgets.setLayoutManager(new GridLayoutManager(this, 2));
        rvBudgets.setAdapter(budgetAdapter);
    }

    private void updateUI() {
        double totalBudget = budgetManager.getTotalBudget();
        double totalExpenses = budgetManager.getTotalExpenses();
        double remaining = totalBudget - totalExpenses;

        tvTotalBudget.setText("Budget: " + String.format("$%,.0f", totalBudget));
        tvExpenses.setText("Expenses: " + String.format("$%,.0f", totalExpenses));
        tvRemaining.setText("Remain: " + String.format("$%,.0f", remaining));

        int progress = totalBudget > 0 ? (int) ((remaining / totalBudget) * 100) : 0;
        mainProgressView.setProgress(progress);
        mainProgressView.setShowRemainingText(true);

        budgetAdapter.updateBudgets(budgetManager.getBudgetItems());

    }

    private void showAddBudgetDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_budget_name);
        EditText etAmount = dialogView.findViewById(R.id.et_budget_amount);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString();
            String amountStr = etAmount.getText().toString();

            if (name.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            BudgetItem newBudget = new BudgetItem(name, amount);

            budgetManager.saveBudgetItem(newBudget);
            budgetAdapter.updateBudgets(budgetManager.getBudgetItems());
            updateUI();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showBudgetDetailDialog(BudgetItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getName());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_budget_detail, null);
        builder.setView(dialogView);

        TextView tvBudgetAmount = dialogView.findViewById(R.id.tv_budget_amount);
        TextView tvExpenses = dialogView.findViewById(R.id.tv_expenses);
        TextView tvRemaining = dialogView.findViewById(R.id.tv_remaining);
        CircularProgressView progressView = dialogView.findViewById(R.id.progress_view);

        double expenses = budgetManager.getExpensesForBudget(item.getName());
        double remaining = item.getTotalAmount() - expenses;

        tvBudgetAmount.setText("Budget: " + String.format("$%,.0f", item.getTotalAmount()));
        tvExpenses.setText("Expenses: " + String.format("$%,.0f", expenses));
        tvRemaining.setText("Remain: " + String.format("$%,.0f", remaining));

        int progress = item.getTotalAmount() > 0 ? (int) ((remaining / item.getTotalAmount()) * 100) : 0;
        progressView.setProgress(progress);
        progressView.setShowRemainingText(true);

        builder.setPositiveButton("OK", null);
        builder.show();
    }


}

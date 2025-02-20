package com.moneysaving.moneylove.moneymanager.finance.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.BudgetManager;
import com.moneysaving.moneylove.moneymanager.finance.Utils.CircularProgressView;
import com.moneysaving.moneylove.moneymanager.finance.activity.BudgetDetailActivity;
import com.moneysaving.moneylove.moneymanager.finance.adapter.BudgetAdapter;
import com.moneysaving.moneylove.moneymanager.finance.model.BudgetItem;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment implements BudgetAdapter.BudgetItemListener {
    private BudgetManager budgetManager;
    private CircularProgressView mainProgressView;
    private TextView tvTotalBudget, tvExpenses;
    private ImageView ivEditBudget;
    //    private RecyclerView rvBudgets;
    private Button btnBudgetDetail;
    private List<TransactionModel> allTransactionList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        initializeViews(view);
        setupBudgetManager();
        loadTransactionData();
//        setupRecyclerView();
        setupListeners();
        updateUI();
        return view;
    }

    private void initializeViews(View view) {
        mainProgressView = view.findViewById(R.id.main_progress_view);
        tvTotalBudget = view.findViewById(R.id.tv_total_budget);
        ivEditBudget = view.findViewById(R.id.iv_edit_budget);
        tvExpenses = view.findViewById(R.id.tv_expenses);
//        rvBudgets = view.findViewById(R.id.rv_budgets);
        btnBudgetDetail = view.findViewById(R.id.btn_budget_detail);
    }

    private void setupBudgetManager() {
        budgetManager = new BudgetManager(requireContext());
        if (budgetManager.getTotalBudget() == 0) {
            budgetManager.setTotalBudget(0); // Set ngân sách mặc định
        }
    }

//    private void setupRecyclerView() {
//        budgetAdapter = new BudgetAdapter(requireContext(), budgetManager.getBudgetItems(), this);
//        rvBudgets.setLayoutManager(new GridLayoutManager(requireContext(), 2));
//        rvBudgets.setAdapter(budgetAdapter);
//    }

    private void setupListeners() {
        // Cho phép chỉnh sửa tổng ngân sách khi click vào TextView hoặc icon
        View.OnClickListener editBudgetListener = v -> showEditBudgetDialog();
        tvTotalBudget.setOnClickListener(editBudgetListener);
        ivEditBudget.setOnClickListener(editBudgetListener);

//        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());
        btnBudgetDetail.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BudgetDetailActivity.class);
            startActivity(intent);
        });
    }

    private void loadTransactionData() {
        if (getArguments() == null || !getArguments().containsKey("transactionList")) {
            return;
        }

        String transactionListJson = getArguments().getString("transactionList");
        if (transactionListJson == null || transactionListJson.isEmpty()) {
            return;
        }

        Type type = new TypeToken<List<TransactionModel>>() {}.getType();
        allTransactionList = new Gson().fromJson(transactionListJson, type);

        if (allTransactionList == null) {
            allTransactionList = new ArrayList<>();
        }

        calculateAndUpdateTotalExpenses();
    }

    private void calculateAndUpdateTotalExpenses() {
        if (allTransactionList == null || allTransactionList.isEmpty()) {
            return;
        }

        double totalExpenseAmount = 0.0;
        for (TransactionModel transaction : allTransactionList) {
            if ("Expend".equals(transaction.getTransactionType())) {
                totalExpenseAmount += Double.parseDouble(transaction.getAmount());
            }
        }

        // Lưu tổng chi phí vào BudgetManager
        budgetManager.setTotalExpenses(totalExpenseAmount);
    }

    private void showEditBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Budget");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter new budget");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newBudget = input.getText().toString();
            if (!newBudget.isEmpty()) {
                double budgetAmount = Double.parseDouble(newBudget);
                budgetManager.setTotalBudget(budgetAmount);
                updateUI();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateUI() {
        double totalBudget = budgetManager.getTotalBudget();
        double totalExpenses = budgetManager.getTotalExpenses();
        double remaining = totalBudget - totalExpenses;

        // Update text views
        tvTotalBudget.setText(String.format("$%.2f", remaining));
        tvExpenses.setText(String.format("$%.2f", totalExpenses));
//        tvRemaining.setText(String.format("$%.2f", remaining));

        // Update main progress
        int progress = totalBudget > 0 ? (int) ((remaining / totalBudget) * 100) : 0;
        mainProgressView.setProgress(progress);
    }

    @Override
    public void onBudgetItemClick(BudgetItem item) {
        // Handle budget item click - show details, edit, etc.
    }
}
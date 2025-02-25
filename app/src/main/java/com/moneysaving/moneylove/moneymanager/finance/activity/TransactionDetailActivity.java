package com.moneysaving.moneylove.moneymanager.finance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.Utils.TransactionUpdateEvent;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import org.greenrobot.eventbus.EventBus;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionDetailActivity extends AppCompatActivity {
    private TextView tvDate, tvAmount, tvCategory, tvNote, tvTransactionType;
    private LinearLayout btnDelete;
    private ImageButton btnBack;
    private TransactionModel transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        initializeViews();
        setupBackButton();
        loadTransactionData();
        setupDeleteButton();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        tvDate = findViewById(R.id.tv_date);
        tvAmount = findViewById(R.id.tv_amount);
        tvCategory = findViewById(R.id.tv_category);
        tvNote = findViewById(R.id.tv_note);
        tvTransactionType = findViewById(R.id.tv_transaction_type);
        btnDelete = findViewById(R.id.btn_delete);
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadTransactionData() {
        Intent intent = getIntent();
        if (intent == null) {
            showError("Invalid transaction data");
            return;
        }

        transaction = (TransactionModel) intent.getSerializableExtra("transaction");

        if (transaction == null) {
            showError("Transaction details not found");
            return;
        }

        displayTransactionDetails();
    }

    private void displayTransactionDetails() {
        tvDate.setText(transaction.getDate());
        tvCategory.setText(transaction.getCategoryName());
        tvTransactionType.setText(transaction.getTransactionType());

        String note = transaction.getNote();
        tvNote.setText(note != null && !note.isEmpty() ? note : "");

        // Format amount with currency
        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        String formattedAmount = String.format("%s%s",
                currentCurrency,
                formatter.format(Double.parseDouble(transaction.getAmount()))
        );

        tvAmount.setText(formattedAmount);
        setAmountColor();
    }



    private void setAmountColor() {
        int colorResId;
        switch (transaction.getTransactionType()) {
            case "Income":
                colorResId = R.color.green;
                break;
            case "Expend":
                colorResId = R.color.red;
                break;
            default:
                colorResId = R.color.black;
        }
        tvAmount.setTextColor(getResources().getColor(colorResId));
    }

    private void setupDeleteButton() {
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTransaction())
                .setNegativeButton("Cancel", null)
                .show();
    }

    int indexToDelete = -1;
    private void deleteTransaction() {
        SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(this);
        List<TransactionModel> transactions = preferenceUtils.getTransactionList();

        if (transactions == null || transactions.isEmpty()) {
            showError("Unable to delete transaction");
            return;
        }


        for (int i = 0; i < transactions.size(); i++) {
            TransactionModel t = transactions.get(i);
            if (t.getDate().equals(transaction.getDate()) &&
                    t.getAmount().equals(transaction.getAmount()) &&
                    t.getCategoryName().equals(transaction.getCategoryName()) &&
                    t.getTransactionType().equals(transaction.getTransactionType()) &&
                    t.getTime().equals(transaction.getTime())) {
                indexToDelete = i;
                break;
            }
        }

        if (indexToDelete == -1) {
            showError("Transaction not found");
            return;
        }
        transactions.remove(indexToDelete);
        preferenceUtils.saveTransactionList(transactions);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleted_position", indexToDelete);
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}
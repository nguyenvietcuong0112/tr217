package com.moneysaving.moneylove.moneymanager.finance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;


public class AddTransactionActivity extends AppCompatActivity {

    private SharePreferenceUtils sharePreferenceUtils;

    private TextView tvCancel, tvSave;
    private Button btnExpend, btnIncome, btnLoan, btnDate, btnTime;
    private EditText etAmount, etNote;
    private Spinner spBudget, spCategory, spCurrency;

    private String transactionType = "Expend";
    private String selectedDate = "February, 13 2025";
    private String selectedTime = "11:50";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_transaction_activity);

        sharePreferenceUtils = new SharePreferenceUtils(this);

        tvCancel = findViewById(R.id.tv_cancel);
        tvSave = findViewById(R.id.tv_save);
        btnExpend = findViewById(R.id.btn_expend);
        btnIncome = findViewById(R.id.btn_income);
        btnLoan = findViewById(R.id.btn_loan);
        btnDate = findViewById(R.id.btn_date);
        btnTime = findViewById(R.id.btn_time);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        spBudget = findViewById(R.id.sp_budget);
        spCategory = findViewById(R.id.sp_category);
        spCurrency = findViewById(R.id.sp_currency);

        btnExpend.setOnClickListener(view -> selectTransactionType("Expend"));
        btnIncome.setOnClickListener(view -> selectTransactionType("Income"));
        btnLoan.setOnClickListener(view -> selectTransactionType("Loan"));

        tvSave.setOnClickListener(view -> saveTransactionData());

        loadPreviousData();
    }

    private void selectTransactionType(String type) {
        transactionType = type;
    }

    private void saveTransactionData() {
        String amount = etAmount.getText().toString();
        String currency = spCurrency.getSelectedItem().toString();
        String note = etNote.getText().toString();
        String budget = spBudget.getSelectedItem().toString();
        String category = spCategory.getSelectedItem().toString();

        TransactionModel transaction = new TransactionModel(transactionType, amount, currency, category, budget, note, selectedDate, selectedTime);
        sharePreferenceUtils.saveTransaction(transaction);

        // Dùng Gson để convert object sang JSON
        Gson gson = new Gson();
        String transactionJson = gson.toJson(transaction);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("transactionData", transactionJson);
        setResult(RESULT_OK, resultIntent);
        finish();

        finish();
    }

    private void loadPreviousData() {
        TransactionModel transaction = sharePreferenceUtils.getTransaction();
        if (transaction != null) {
            etAmount.setText(transaction.getAmount());
            etNote.setText(transaction.getNote());
            setSpinnerSelection(spCurrency, transaction.getCurrency());
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
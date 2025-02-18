package com.moneysaving.moneylove.moneymanager.finance.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AddTransactionActivity extends AppCompatActivity {

    private SharePreferenceUtils sharePreferenceUtils;

    private TextView tvCancel, tvSave;
    private Button btnExpend, btnIncome, btnLoan, btnDate, btnTime;
    private EditText etAmount, etNote;
    private Spinner spBudget, spCategory, spCurrency;
    String[] budgets = {"None", "Budget 1", "Budget 2", "Budget 3"};
    String[] category = {"Food", "Social", "Traffic", "Shopping",
            "Grocery", "Education"};
    private String transactionType = "Expend";
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private String selectedDate = "";
    private String selectedTime = "";

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

        selectedDate = dateFormat.format(new Date()); // Ngày hiện tại
        selectedTime = timeFormat.format(new Date());

        btnDate.setText(selectedDate);
        btnTime.setText(selectedTime);

        tvCancel.setOnClickListener(view -> onBackPressed());

        btnExpend.setOnClickListener(view -> selectTransactionType("Expend"));
        btnIncome.setOnClickListener(view -> selectTransactionType("Income"));
        btnLoan.setOnClickListener(view -> selectTransactionType("Loan"));

        btnDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        btnDate.setText(selectedDate); // Cập nhật UI
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Mở TimePicker khi click vào btnTime
        btnTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        selectedTime = hourOfDay + ":" + minute;
                        btnTime.setText(selectedTime); // Cập nhật UI
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, budgets);
        spBudget.setAdapter(adapter);

        // Xử lý sự kiện khi chọn Budget
        spBudget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBudget = budgets[position];
                Toast.makeText(getApplicationContext(), "Selected: " + selectedBudget, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, category);
        spCategory.setAdapter(adapterCategory);
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = category[position];
                Toast.makeText(getApplicationContext(), "Selected: " + selectedCategory, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì cả
            }
        });


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
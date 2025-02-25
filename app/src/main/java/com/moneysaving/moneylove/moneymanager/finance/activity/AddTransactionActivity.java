package com.moneysaving.moneylove.moneymanager.finance.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.BudgetManager;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.Utils.TransactionUpdateEvent;
import com.moneysaving.moneylove.moneymanager.finance.adapter.CategoryAdapter;
import com.moneysaving.moneylove.moneymanager.finance.model.BudgetItem;
import com.moneysaving.moneylove.moneymanager.finance.model.CategoryItem;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import org.greenrobot.eventbus.EventBus;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private SharePreferenceUtils sharePreferenceUtils;

    private TextView tvCancel, tvSave, tvCurrency;
    private LinearLayout rgTransactionType;
    private LinearLayout rbExpend, rbIncome, rbLoan;
    private EditText etAmount, etNote, etLender;
    private Spinner spBudget;
    private LinearLayout btnDate, btnTime;
    private TextView tv_date, tvTime;

    private RecyclerView rvCategories;
    private LinearLayout layoutLender, layoutBudget;
    private String currentTransactionType = "Expend";
    private int colorActive, colorInactive;
    private ImageView ivExpend, ivIncome, ivLoan;
    private TextView tvExpendLabel, tvIncomeLabel, tvLoanLabel;

    private String transactionType = "Expend";
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private String selectedDate = "";
    private String selectedTime = "";

    private CategoryAdapter categoryAdapter;
    private CategoryItem selectedCategory;

    private List<CategoryItem> expenseCategories;
    private List<CategoryItem> incomeCategories;
    private List<CategoryItem> loanCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_transaction_activity);

        sharePreferenceUtils = new SharePreferenceUtils(this);


        initViews();
        initCategories();
        setupListeners();
        initBudget();
        selectedDate = dateFormat.format(new Date());
        selectedTime = timeFormat.format(new Date());

        tv_date.setText(selectedDate);
        tvTime.setText(selectedTime);

        selectTransactionType("Expend");

        loadPreviousData();
    }

    private void initBudget() {
        BudgetManager budgetManager = new BudgetManager(this);
        List<BudgetItem> budgetItems = budgetManager.getBudgetItems();
        List<String> budgetNames = new ArrayList<>();
        budgetNames.add("None");
        for (BudgetItem item : budgetItems) {
            budgetNames.add(item.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, budgetNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBudget.setAdapter(adapter);
    }

    private void initViews() {
        tvCancel = findViewById(R.id.tv_cancel);
        tvSave = findViewById(R.id.tv_save);
        rgTransactionType = findViewById(R.id.ll_transaction_tabs);
        rbExpend = findViewById(R.id.ll_expend);
        rbIncome = findViewById(R.id.ll_income);
        rbLoan = findViewById(R.id.ll_loan);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        etLender = findViewById(R.id.et_lender);
        spBudget = findViewById(R.id.sp_budget);
        tvCurrency = findViewById(R.id.tv_currency);
        btnDate = findViewById(R.id.btn_date);
        btnTime = findViewById(R.id.btn_time);
        tv_date = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);
        rvCategories = findViewById(R.id.rv_categories);
        layoutLender = findViewById(R.id.layout_lender);
        layoutBudget = findViewById(R.id.layout_budget);

        ivExpend = rbExpend.findViewById(R.id.iv_expend);
        tvExpendLabel = rbExpend.findViewById(R.id.tv_expend_label);
        ivIncome = rbIncome.findViewById(R.id.iv_income);
        tvIncomeLabel = rbIncome.findViewById(R.id.tv_income_label);
        ivLoan = rbLoan.findViewById(R.id.iv_loan);
        tvLoanLabel = rbLoan.findViewById(R.id.tv_loan_label);

        colorActive = getResources().getColor(R.color.blue);
        colorInactive = getResources().getColor(R.color.icon_inactive);
        rbExpend.setBackgroundResource(R.drawable.bg_tab_item_true);
        rbIncome.setBackgroundResource(R.drawable.bg_tab_item_false);
        rbLoan.setBackgroundResource(R.drawable.bg_tab_item_false);

        updateTabColors("Expend");
        configAmount();


        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "USD";
        tvCurrency.setText(currentCurrency);
    }

    void configAmount() {
        etAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) {
                    return;
                }

                try {
                    isUpdating = true;

                    String str = s.toString();
                    if (str.equals(current)) {
                        isUpdating = false;
                        return;
                    }

                    String cleanString = str.replaceAll("[,]", "");

                    if (cleanString.isEmpty()) {
                        etAmount.setText("");
                        isUpdating = false;
                        return;
                    }

                    long parsed = Long.parseLong(cleanString);
                    String formatted = NumberFormat.getNumberInstance(Locale.US).format(parsed);

                    current = formatted;
                    etAmount.setText(formatted);
                    etAmount.setSelection(formatted.length());

                } catch (NumberFormatException e) {
                } finally {
                    isUpdating = false;
                }
            }
        });
    }

    private void updateTabSelection(LinearLayout selectedTab) {
        rbExpend.setSelected(false);
        rbIncome.setSelected(false);
        rbLoan.setSelected(false);

        selectedTab.setSelected(true);
    }

    private void updateTabColors(String selectedType) {
        currentTransactionType = selectedType;

        ivExpend.setColorFilter(selectedType.equals("Expend") ? colorActive : colorInactive);
        tvExpendLabel.setTextColor(selectedType.equals("Expend") ? colorActive : colorInactive);

        ivIncome.setColorFilter(selectedType.equals("Income") ? colorActive : colorInactive);
        tvIncomeLabel.setTextColor(selectedType.equals("Income") ? colorActive : colorInactive);

        ivLoan.setColorFilter(selectedType.equals("Loan") ? colorActive : colorInactive);
        tvLoanLabel.setTextColor(selectedType.equals("Loan") ? colorActive : colorInactive);
    }

    private void setupListeners() {
        tvCancel.setOnClickListener(view -> onBackPressed());
        tvSave.setOnClickListener(view -> saveTransactionData());

        rbExpend.setOnClickListener(v -> {
            rbExpend.setBackgroundResource(R.drawable.bg_tab_item_true);
            rbIncome.setBackgroundResource(R.drawable.bg_tab_item_false);
            rbLoan.setBackgroundResource(R.drawable.bg_tab_item_false);
            updateTabColors("Expend");
            updateTabSelection(rbExpend);
            selectTransactionType("Expend");
        });

        rbIncome.setOnClickListener(v -> {
            rbExpend.setBackgroundResource(R.drawable.bg_tab_item_false);
            rbIncome.setBackgroundResource(R.drawable.bg_tab_item_true);
            rbLoan.setBackgroundResource(R.drawable.bg_tab_item_false);
            updateTabColors("Income");
            updateTabSelection(rbIncome);
            selectTransactionType("Income");
        });

        rbLoan.setOnClickListener(v -> {
            rbExpend.setBackgroundResource(R.drawable.bg_tab_item_false);
            rbIncome.setBackgroundResource(R.drawable.bg_tab_item_false);
            rbLoan.setBackgroundResource(R.drawable.bg_tab_item_true);

            updateTabColors("Loan");

            updateTabSelection(rbLoan);
            selectTransactionType("Loan");
        });

        btnDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        tv_date.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btnTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        tvTime.setText(selectedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
        });
    }

    private void initCategories() {
        expenseCategories = new ArrayList<>();
        expenseCategories.add(new CategoryItem(R.drawable.ic_food, "Food"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_social, "Social"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_trafic, "Traffic"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_shopping, "Shopping"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_grocery, "Grocery"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_education, "Education"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_bills, "Bills"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_rentals, "Rentals"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_medical, "Medical"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_investment, "Investment"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_gift, "Gift"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_other, "Other"));

        incomeCategories = new ArrayList<>();
        incomeCategories.add(new CategoryItem(R.drawable.ic_salary, "Salary"));
        incomeCategories.add(new CategoryItem(R.drawable.ic_invest, "Invest"));
        incomeCategories.add(new CategoryItem(R.drawable.ic_business, "Business"));
        incomeCategories.add(new CategoryItem(R.drawable.ic_interest, "Interest"));
        incomeCategories.add(new CategoryItem(R.drawable.ic_extra_income, "Extra Income"));
        incomeCategories.add(new CategoryItem(R.drawable.ic_other, "Other"));

        loanCategories = new ArrayList<>();
        loanCategories.add(new CategoryItem(R.drawable.ic_loan, "Loan"));
        loanCategories.add(new CategoryItem(R.drawable.ic_borrow, "Borrow"));
    }

    private void setupCategoryGrid(List<CategoryItem> categories) {
        categoryAdapter = new CategoryAdapter(this, categories, (category, position) -> {
            selectedCategory = category;
        });

        rvCategories.setLayoutManager(new GridLayoutManager(this, 4)); // 4 columns
        rvCategories.setAdapter(categoryAdapter);
    }

    private void selectTransactionType(String type) {
        transactionType = type;

        switch (type) {
            case "Expend":
                setupCategoryGrid(expenseCategories);
                layoutBudget.setVisibility(View.VISIBLE);
                layoutLender.setVisibility(View.GONE);
                break;

            case "Income":
                setupCategoryGrid(incomeCategories);
                layoutBudget.setVisibility(View.GONE);
                layoutLender.setVisibility(View.GONE);
                break;

            case "Loan":
                setupCategoryGrid(loanCategories);
                layoutBudget.setVisibility(View.GONE);
                layoutLender.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void saveTransactionData() {
        if (etAmount.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "USD";

        String amount = etAmount.getText().toString().replaceAll(",", "");
        String note = etNote.getText().toString();
        String budget = "None";

        if ("Expend".equals(transactionType)) {
            budget = spBudget.getSelectedItem().toString();
        }

        TransactionModel transaction = new TransactionModel(
                transactionType,
                amount,
                currentCurrency,
                selectedCategory.getName(),
                selectedCategory.getIconResource(),
                budget,
                note,
                selectedDate,
                selectedTime
        );

        if ("Loan".equals(transactionType)) {
            String lenderText = etLender.getText().toString();
            transaction.setLender(lenderText);
        }

        sharePreferenceUtils.saveTransaction(transaction);
        EventBus.getDefault().post(new TransactionUpdateEvent(sharePreferenceUtils.getTransactionList()));

        if ("Expend".equals(transactionType) && !"None".equals(budget)) {
            BudgetManager budgetManager = new BudgetManager(this);
            budgetManager.updateBudgetExpenses(budget);
        }
        Gson gson = new Gson();
        String transactionJson = gson.toJson(transaction);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("transactionData", transactionJson);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();

        finish();
    }

    private void loadPreviousData() {
        TransactionModel transaction = sharePreferenceUtils.getTransaction();
        if (transaction != null) {
            etAmount.setText(transaction.getAmount());
            etNote.setText(transaction.getNote());

            switch (transaction.getTransactionType()) {
                case "Expend":
                    updateTabSelection(rbExpend);
                    updateTabColors("Expend");
                    selectTransactionType("Expend");
                    break;
                case "Income":
                    updateTabSelection(rbIncome);
                    updateTabColors("Income");
                    selectTransactionType("Income");
                    break;
                case "Loan":
                    updateTabSelection(rbLoan);
                    updateTabColors("Loan");
                    selectTransactionType("Loan");
                    if (transaction.getLender() != null) {
                        etLender.setText(transaction.getLender());
                    }
                    break;
            }

            if ("Expend".equals(transaction.getTransactionType())) {
                setSpinnerSelection(spBudget, transaction.getBudget());
            }

            if (transaction.getDate() != null) {
                selectedDate = transaction.getDate();
                tv_date.setText(selectedDate);
            }

            if (transaction.getTime() != null) {
                selectedTime = transaction.getTime();
                tvTime.setText(selectedTime);
            }
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
package com.moneysaving.moneylove.moneymanager.finance.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.Utils.TransactionUpdateEvent;
import com.moneysaving.moneylove.moneymanager.finance.adapter.TransactionAdapter;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private TextView tvTotalBalance, tvSelectedMonth, tvTotalExpenditure;
    private LinearLayout llExpend, llIncome, llLoan;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<TransactionModel> allTransactionList = new ArrayList<>();
    private List<TransactionModel> filteredTransactionList = new ArrayList<>();
    private String currentTransactionType = "Expend";
    private String currentMonth = "";
    private Map<String, List<TransactionModel>> transactionsByDate = new HashMap<>();
    String currentCurrency;
    ImageView ivEditBalance;

    String totalAmount;

    boolean isBalanceVisible = true;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        loadTransactionData();
        setupClickListeners();
        setupInitialFilter();


        return view;
    }

    private void initViews(View view) {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(getContext());
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvSelectedMonth = view.findViewById(R.id.tv_selected_month);
        tvTotalExpenditure = view.findViewById(R.id.tv_total_expenditure);
        llExpend = view.findViewById(R.id.ll_expend);
        llIncome = view.findViewById(R.id.ll_income);
        llLoan = view.findViewById(R.id.ll_loan);
        rvTransactions = view.findViewById(R.id.rv_transactions);
        ivEditBalance = view.findViewById(R.id.iv_edit_balance);

        // Thiết lập RecyclerView
        adapter = new TransactionAdapter(filteredTransactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(adapter);

        tvTotalExpenditure.setText(currentCurrency);
    }

    private void loadTransactionData() {
        if (getArguments() == null || !getArguments().containsKey("transactionList")) {
            return;
        }

        String transactionListJson = getArguments().getString("transactionList");
        if (transactionListJson == null || transactionListJson.isEmpty()) {
            return;
        }

        Type type = new TypeToken<List<TransactionModel>>() {
        }.getType();
        allTransactionList = new Gson().fromJson(transactionListJson, type);

        if (allTransactionList == null) {
            allTransactionList = new ArrayList<>();
        }
    }

    private void setupClickListeners() {
        llExpend.setOnClickListener(v -> {
            currentTransactionType = "Expend";
            updateTransactionTypeUI();
            filterTransactions();
        });

        llIncome.setOnClickListener(v -> {
            currentTransactionType = "Income";
            updateTransactionTypeUI();
            filterTransactions();
        });

        llLoan.setOnClickListener(v -> {
            currentTransactionType = "Loan";
            updateTransactionTypeUI();
            filterTransactions();
        });

        tvSelectedMonth.setOnClickListener(v -> {
            showMonthPickerDialog();
        });

        ivEditBalance.setOnClickListener(view -> {
            if (isBalanceVisible) {
                tvTotalBalance.setText("******");
                ivEditBalance.setImageResource(R.drawable.ic_visibility_off);
            } else {
                tvTotalBalance.setText(totalAmount);
                ivEditBalance.setImageResource(R.drawable.ic_visibility);
            }
            isBalanceVisible = !isBalanceVisible;
        });
    }

    private void setupInitialFilter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        currentMonth = dateFormat.format(calendar.getTime());
        tvSelectedMonth.setText(currentMonth);

        // Cập nhật UI và lọc dữ liệu
        updateTransactionTypeUI();
        filterTransactions();
    }

    private void updateTransactionTypeUI() {
        llExpend.setBackgroundResource(currentTransactionType.equals("Expend") ? R.drawable.bg_tab_item_true : R.drawable.bg_tab_item_false);
        llIncome.setBackgroundResource(currentTransactionType.equals("Income") ? R.drawable.bg_tab_item_true : R.drawable.bg_tab_item_false);
        llLoan.setBackgroundResource(currentTransactionType.equals("Loan") ? R.drawable.bg_tab_item_true : R.drawable.bg_tab_item_false);

        // Cập nhật màu sắc và style cho tab được chọn
        ImageView ivExpend = llExpend.findViewById(R.id.iv_expend);
        TextView tvExpendLabel = llExpend.findViewById(R.id.tv_expend_label);
        ImageView ivIncome = llIncome.findViewById(R.id.iv_income);
        TextView tvIncomeLabel = llIncome.findViewById(R.id.tv_income_label);
        ImageView ivLoan = llLoan.findViewById(R.id.iv_loan);
        TextView tvLoanLabel = llLoan.findViewById(R.id.tv_loan_label);

        int colorActive = getResources().getColor(R.color.blue);
        int colorInactive = getResources().getColor(R.color.icon_inactive);

        ivExpend.setColorFilter(currentTransactionType.equals("Expend") ? colorActive : colorInactive);
        tvExpendLabel.setTextColor(currentTransactionType.equals("Expend") ? colorActive : colorInactive);

        ivIncome.setColorFilter(currentTransactionType.equals("Income") ? colorActive : colorInactive);
        tvIncomeLabel.setTextColor(currentTransactionType.equals("Income") ? colorActive : colorInactive);

        ivLoan.setColorFilter(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
        tvLoanLabel.setTextColor(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
    }

    private void filterTransactions() {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMMM, d yyyy", Locale.US);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        SimpleDateFormat alternateInputFormat = new SimpleDateFormat("dd/M/yyyy", Locale.getDefault());

        filteredTransactionList.clear();
        transactionsByDate.clear();

        for (TransactionModel transaction : allTransactionList) {

            if (transaction.getDate() == null || transaction.getDate().trim().isEmpty()) {
                continue;
            }

            try {
                Date transactionDate;
                String dateStr = transaction.getDate();

                try {
                    transactionDate = inputDateFormat.parse(dateStr);
                } catch (ParseException e) {
                    try {
                        transactionDate = alternateInputFormat.parse(dateStr);
                    } catch (ParseException e2) {
                        System.err.println("Could not parse date: " + dateStr);
                        continue;
                    }
                }

                String transactionMonth = outputDateFormat.format(transactionDate);

                if (transaction.getTransactionType().equals(currentTransactionType) &&
                        transactionMonth.equals(currentMonth)) {

                    filteredTransactionList.add(transaction);

                    String dayKey = new SimpleDateFormat("EEE, dd MMMM", Locale.US).format(transactionDate);
                    if (!transactionsByDate.containsKey(dayKey)) {
                        transactionsByDate.put(dayKey, new ArrayList<>());
                    }
                    transactionsByDate.get(dayKey).add(transaction);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        updateTotalAmount();

        adapter.updateData(filteredTransactionList, transactionsByDate);
    }

    private void updateTotalAmount() {
        double totalBalance = 0;
        double totalExpenditure = 0;
        double totalIncome = 0;
        double totalLoan = 0;

        for (TransactionModel transaction : allTransactionList) {
            try {
                double amount = Double.parseDouble(transaction.getAmount());

                if (transaction.getTransactionType().equals("Income")) {
                    totalBalance += amount;
                } else if (transaction.getTransactionType().equals("Expend")) {
                    totalBalance -= amount;
                }
                if (transaction.getDate() != null && !transaction.getDate().trim().isEmpty()) {
                    try {
                        Date transactionDate = parseTransactionDate(transaction.getDate());
                        if (transactionDate != null) {
                            SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
                            String transactionMonth = outputDateFormat.format(transactionDate);

                            if (transactionMonth.equals(currentMonth)) {
                                switch (transaction.getTransactionType()) {
                                    case "Income":
                                        totalIncome += amount;
                                        break;
                                    case "Expend":
                                        totalExpenditure += amount;
                                        break;
                                    case "Loan":
                                        totalLoan += amount;
                                        break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        tvTotalBalance.setText(currentCurrency + formatter.format(totalBalance));
        totalAmount = currentCurrency + formatter.format(totalBalance);
        switch (currentTransactionType) {
            case "Income":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalIncome));
                break;
            case "Expend":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalExpenditure));
                break;
            case "Loan":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalLoan));
                break;
        }
    }

    private Date parseTransactionDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        // Try multiple date formats
        SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("MMMM, d yyyy", Locale.US),
                new SimpleDateFormat("dd/M/yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        };

        for (SimpleDateFormat format : dateFormats) {
            try {
                return format.parse(dateString);
            } catch (ParseException e) {
                // Try next format
            }
        }

        // If all formats fail, log and return null
        System.err.println("Could not parse date with any format: " + dateString);
        return null;
    }

    private void showMonthPickerDialog() {
        // Hiển thị dialog chọn tháng
        // Bạn có thể sử dụng DatePickerDialog, MonthYearPickerDialog, hoặc thư viện bên thứ ba
        // Ví dụ đơn giản:
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // Tạo mảng tháng
        String[] months = new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Choose Month")
                .setItems(months, (dialog, which) -> {
                    // Cập nhật tháng được chọn
                    currentMonth = months[which] + " " + year;
                    tvSelectedMonth.setText(currentMonth);
                    filterTransactions();
                });
        builder.create().show();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdated(TransactionUpdateEvent event) {
        allTransactionList = event.getTransactionList();
        filterTransactions();
    }
}
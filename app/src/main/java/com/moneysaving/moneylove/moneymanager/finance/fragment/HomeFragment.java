package com.moneysaving.moneylove.moneymanager.finance.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
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
import com.moneysaving.moneylove.moneymanager.finance.adapter.LoanTransactionAdapter;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private TextView tvTotalBalance, tvSelectedMonth, tvTotalExpenditure;
    private LinearLayout llExpend, llIncome, llLoan,headerTotal;
    private RecyclerView rvTransactions;
    private TransactionAdapter regularAdapter;
    private LoanTransactionAdapter loanAdapter;

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

        headerTotal = view.findViewById(R.id.header_total);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvSelectedMonth = view.findViewById(R.id.tv_selected_month);
        tvTotalExpenditure = view.findViewById(R.id.tv_total_expenditure);
        llExpend = view.findViewById(R.id.ll_expend);
        llIncome = view.findViewById(R.id.ll_income);
        llLoan = view.findViewById(R.id.ll_loan);
        rvTransactions = view.findViewById(R.id.rv_transactions);
        ivEditBalance = view.findViewById(R.id.iv_edit_balance);

        regularAdapter = new TransactionAdapter(filteredTransactionList);
        loanAdapter = new LoanTransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(regularAdapter);

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
                ivEditBalance.setImageResource(R.drawable.ic_visibility);
            } else {
                tvTotalBalance.setText(totalAmount);
                ivEditBalance.setImageResource(R.drawable.ic_visibility_off);
            }
            isBalanceVisible = !isBalanceVisible;
        });
    }

    private void setupInitialFilter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        currentMonth = dateFormat.format(calendar.getTime());
        tvSelectedMonth.setText(currentMonth);

        updateTransactionTypeUI();
        filterTransactions();
    }

    private void updateTransactionTypeUI() {
        llExpend.setBackgroundResource(currentTransactionType.equals("Expend") ? R.drawable.bg_tab_item_true : R.drawable.bg_tab_item_false);
        llIncome.setBackgroundResource(currentTransactionType.equals("Income") ? R.drawable.bg_tab_item_true : R.drawable.bg_tab_item_false);
        llLoan.setBackgroundResource(currentTransactionType.equals("Loan") ? R.drawable.bg_tab_item_true : R.drawable.bg_tab_item_false);

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

        double totalAmount = 0;

        if ("Loan".equals(currentTransactionType)) {
            rvTransactions.setAdapter(loanAdapter);
            headerTotal.setVisibility(View.GONE);

            List<TransactionModel> loanTransactions = new ArrayList<>();
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
                        loanTransactions.add(transaction);

                        try {
                            double amount = Double.parseDouble(transaction.getAmount());
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            loanAdapter.updateData(loanTransactions);

            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tvTotalExpenditure.setText(currentCurrency + formatter.format(totalAmount));

        } else {
            rvTransactions.setAdapter(regularAdapter);

            headerTotal.setVisibility(View.VISIBLE);

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

                        try {
                            double amount = Double.parseDouble(transaction.getAmount());
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            regularAdapter.updateData(filteredTransactionList, transactionsByDate);
            updateTotalAmount();

            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tvTotalExpenditure.setText(currentCurrency + formatter.format(totalAmount));
        }

        updateTotalAmount();
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
                tvTotalExpenditure.setTextColor(Color.parseColor("#17B26A"));
                break;
            case "Expend":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalExpenditure));
                tvTotalExpenditure.setTextColor(Color.parseColor("#F04438"));
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
            }
        }

        System.err.println("Could not parse date with any format: " + dateString);
        return null;
    }

    private void showMonthPickerDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.picker_month_dialog);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        TextView yearText = dialog.findViewById(R.id.tv_year);
        yearText.setText(String.valueOf(currentYear));

        String currentSelectedMonth = tvSelectedMonth.getText().toString();
        String[] currentParts = currentSelectedMonth.split(" ");
        if (currentParts.length > 0) {
            yearText.setText(currentParts[1]);
        }

        ImageButton prevYear = dialog.findViewById(R.id.btn_previous_year);
        ImageButton nextYear = dialog.findViewById(R.id.btn_next_year);

        prevYear.setOnClickListener(v -> {
            int year = Integer.parseInt(yearText.getText().toString()) - 1;
            yearText.setText(String.valueOf(year));
        });

        nextYear.setOnClickListener(v -> {
            int year = Integer.parseInt(yearText.getText().toString()) + 1;
            yearText.setText(String.valueOf(year));
        });

        int[] monthIds = {
                R.id.month_jan, R.id.month_feb, R.id.month_mar, R.id.month_apr,
                R.id.month_may, R.id.month_jun, R.id.month_jul, R.id.month_aug,
                R.id.month_sep, R.id.month_oct, R.id.month_nov, R.id.month_dec
        };

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        final TextView[] selectedMonthHolder = new TextView[1];

        String currentMonthAbbrev = currentSelectedMonth.split(" ")[0].substring(0, 3);
        for (int i = 0; i < monthIds.length; i++) {
            TextView monthView = dialog.findViewById(monthIds[i]);
            if (monthView.getText().toString().equals(currentMonthAbbrev)) {
                monthView.setBackgroundResource(R.drawable.bg_selected_month);
                monthView.setTextColor(getResources().getColor(android.R.color.white));
                selectedMonthHolder[0] = monthView;
            }
        }

        for (int i = 0; i < monthIds.length; i++) {
            TextView monthView = dialog.findViewById(monthIds[i]);

            monthView.setOnClickListener(v -> {
                for (int id : monthIds) {
                    dialog.findViewById(id).setBackgroundResource(android.R.color.transparent);
                    ((TextView) dialog.findViewById(id)).setTextColor(getResources().getColor(android.R.color.black));
                }

                v.setBackgroundResource(R.drawable.bg_selected_month);
                ((TextView) v).setTextColor(getResources().getColor(android.R.color.white));

                selectedMonthHolder[0] = (TextView) v;
            });
        }

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            if (selectedMonthHolder[0] != null) {
                String month = selectedMonthHolder[0].getText().toString();
                String year = yearText.getText().toString();
                String fullMonthName = months[Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").indexOf(month)];
                currentMonth = fullMonthName + " " + year;
                tvSelectedMonth.setText(currentMonth);
                filterTransactions();
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.9);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = width;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.show();
    }



    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdated(TransactionUpdateEvent event) {
        allTransactionList = event.getTransactionList();
        filterTransactions();
        updateTotalAmount();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            int deletedPosition = data.getIntExtra("deleted_position", -1);

            if (deletedPosition != -1) {
                loadTransactionData();
                filterTransactions();
                updateTotalAmount();
            }
        }
    }
}
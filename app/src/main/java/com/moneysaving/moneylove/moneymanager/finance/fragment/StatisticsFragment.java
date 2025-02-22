package com.moneysaving.moneylove.moneymanager.finance.fragment;

import android.graphics.Color;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;
import com.moneysaving.moneylove.moneymanager.finance.Utils.TransactionUpdateEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsFragment extends Fragment {
    private TextView tvTotalBalance, tvSelectedMonth;
    private LinearLayout llExpend, llIncome, llLoan,llSelectedMmonth;
    private LinearLayout llCategoryList;
    private BarChart barChart;

    private List<TransactionModel> allTransactionList = new ArrayList<>();
    private String currentTransactionType = "Expend";
    private String currentMonth;
    private Map<String, Double> categoryTotals = new HashMap<>();
    String currentCurrency;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
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
        llSelectedMmonth = view.findViewById(R.id.ll_selected_month);
        llExpend = view.findViewById(R.id.ll_expend);
        llIncome = view.findViewById(R.id.ll_income);
        llLoan = view.findViewById(R.id.ll_loan);
        llCategoryList = view.findViewById(R.id.ll_category_list);
        barChart = view.findViewById(R.id.bar_chart);

    }

    private void loadTransactionData() {
        if (getArguments() != null && getArguments().containsKey("transactionList")) {
            String transactionListJson = getArguments().getString("transactionList");
            if (transactionListJson != null && !transactionListJson.isEmpty()) {
                Type type = new TypeToken<List<TransactionModel>>() {}.getType();
                allTransactionList = new Gson().fromJson(transactionListJson, type);
                if (allTransactionList == null) {
                    allTransactionList = new ArrayList<>();
                }
            }
        }
    }

    private void setupClickListeners() {
        llExpend.setOnClickListener(v -> {
            currentTransactionType = "Expend";
            updateTransactionTypeUI();
            updateStatistics();
        });

        llIncome.setOnClickListener(v -> {
            currentTransactionType = "Income";
            updateTransactionTypeUI();
            updateStatistics();
        });

        llLoan.setOnClickListener(v -> {
            currentTransactionType = "Loan";
            updateTransactionTypeUI();
            updateStatistics();
        });

        llSelectedMmonth.setOnClickListener(v -> showMonthPickerDialog());
    }

    private void setupInitialFilter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        currentMonth = dateFormat.format(new Date());
        tvSelectedMonth.setText(currentMonth);
        updateTransactionTypeUI();
        updateStatistics();
    }

    private void updateTransactionTypeUI() {
        // Đặt lại trạng thái của tất cả các loại giao dịch
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

    private void updateStatistics() {
        // Clear previous data
        categoryTotals.clear();
        double totalAmount = 0;

        // Filter transactions by type and month
        List<TransactionModel> filteredTransactions = filterTransactionsByMonthAndType();

        // Calculate totals by category
        for (TransactionModel transaction : filteredTransactions) {
            String category = transaction.getCategoryName();
            double amount = Double.parseDouble(transaction.getAmount());
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            totalAmount += amount;
        }

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        tvTotalBalance.setText(currentCurrency + formatter.format(totalAmount));

        // Update category list
        updateCategoryList();

        // Update chart
        updateChart();
    }

    private List<TransactionModel> filterTransactionsByMonthAndType() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        return allTransactionList.stream()
                .filter(transaction -> {
                    try {
                        Date transactionDate = parseTransactionDate(transaction.getDate());
                        return transaction.getTransactionType().equals(currentTransactionType) &&
                                monthFormat.format(transactionDate).equals(currentMonth);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private void updateCategoryList() {
        llCategoryList.removeAllViews();

         List<Map.Entry<String, Double>> sortedCategories = new ArrayList<>(categoryTotals.entrySet());
        sortedCategories.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        for (Map.Entry<String, Double> entry : sortedCategories) {
            View categoryItem = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_statistics_category, llCategoryList, false);

            TextView tvCategory = categoryItem.findViewById(R.id.tvCategoryName);
            TextView tvAmount = categoryItem.findViewById(R.id.tvCategoryAmount);

            tvCategory.setText(entry.getKey());
            tvAmount.setText(String.format(Locale.US, "$%.2f", entry.getValue()));

            llCategoryList.addView(categoryItem);
        }
    }

    private void updateChart() {
        barChart.clear();

        List<BarEntry> entries = new ArrayList<>();
        List<String> categoryLabels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            categoryLabels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#FF86EC"),
                Color.parseColor("#9E86FF"),
                Color.parseColor("#FFB986"),
                Color.parseColor("#86DBFF"),
                Color.parseColor("#6EADFF"),
                Color.parseColor("#79F2C0"),
                Color.parseColor("#2FE3FF"),
                Color.parseColor("#FFD84C"));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setScaleEnabled(false);
        barData.setBarWidth(0.5f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                return index >= 0 && index < categoryLabels.size() ? categoryLabels.get(index) : "";
            }
        });
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        barChart.invalidate(); // Refresh chart
    }

    private void showMonthPickerDialog() {
        Calendar calendar = Calendar.getInstance();
        String[] months = new String[] {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Choose Month")
                .setItems(months, (dialog, which) -> {
                    currentMonth = months[which] + " " + calendar.get(Calendar.YEAR);
                    tvSelectedMonth.setText(currentMonth);
                    updateStatistics();
                });
        builder.create().show();
    }

    private Date parseTransactionDate(String dateString) {
        SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("MMMM, d yyyy", Locale.US),
                new SimpleDateFormat("dd/M/yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        };

        for (SimpleDateFormat format : dateFormats) {
            try {
                return format.parse(dateString);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
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
        updateStatistics();
    }
}
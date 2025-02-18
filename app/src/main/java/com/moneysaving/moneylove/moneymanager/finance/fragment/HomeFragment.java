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
import com.moneysaving.moneylove.moneymanager.finance.Utils.TransactionUpdateEvent;
import com.moneysaving.moneylove.moneymanager.finance.adapter.TransactionAdapter;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
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
    private String currentTransactionType = "Expend"; // Mặc định là chi tiêu
    private String currentMonth = ""; // Tháng hiện tại
    private Map<String, List<TransactionModel>> transactionsByDate = new HashMap<>();

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
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvSelectedMonth = view.findViewById(R.id.tv_selected_month);
        tvTotalExpenditure = view.findViewById(R.id.tv_total_expenditure);
        llExpend = view.findViewById(R.id.ll_expend);
        llIncome = view.findViewById(R.id.ll_income);
        llLoan = view.findViewById(R.id.ll_loan);
        rvTransactions = view.findViewById(R.id.rv_transactions);

        // Thiết lập RecyclerView
        adapter = new TransactionAdapter(filteredTransactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(adapter);
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
            // Hiển thị dialog chọn tháng
            showMonthPickerDialog();
        });
    }

    private void setupInitialFilter() {
        // Thiết lập tháng hiện tại
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        currentMonth = dateFormat.format(calendar.getTime());
        tvSelectedMonth.setText(currentMonth);

        // Cập nhật UI và lọc dữ liệu
        updateTransactionTypeUI();
        filterTransactions();
    }

    private void updateTransactionTypeUI() {
        // Cập nhật UI cho loại giao dịch được chọn
        llExpend.setSelected(currentTransactionType.equals("Expend"));
        llIncome.setSelected(currentTransactionType.equals("Income"));
        llLoan.setSelected(currentTransactionType.equals("Loan"));

        // Cập nhật màu sắc và style cho tab được chọn
        ImageView ivExpend = llExpend.findViewById(R.id.iv_expend);
        TextView tvExpendLabel = llExpend.findViewById(R.id.tv_expend_label);
        ImageView ivIncome = llIncome.findViewById(R.id.iv_income);
        TextView tvIncomeLabel = llIncome.findViewById(R.id.tv_income_label);
        ImageView ivLoan = llLoan.findViewById(R.id.iv_loan);
        TextView tvLoanLabel = llLoan.findViewById(R.id.tv_loan_label);

        int colorActive = getResources().getColor(R.color.blue);
        int colorInactive = getResources().getColor(R.color.icon_inactive);

        // Đặt màu và style cho tab Expend
        ivExpend.setColorFilter(currentTransactionType.equals("Expend") ? colorActive : colorInactive);
        tvExpendLabel.setTextColor(currentTransactionType.equals("Expend") ? colorActive : colorInactive);

        // Đặt màu và style cho tab Income
        ivIncome.setColorFilter(currentTransactionType.equals("Income") ? colorActive : colorInactive);
        tvIncomeLabel.setTextColor(currentTransactionType.equals("Income") ? colorActive : colorInactive);

        // Đặt màu và style cho tab Loan
        ivLoan.setColorFilter(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
        tvLoanLabel.setTextColor(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
    }

    private void filterTransactions() {
        // Lọc theo loại giao dịch và tháng
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMMM, d yyyy", Locale.US);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        SimpleDateFormat alternateInputFormat = new SimpleDateFormat("dd/M/yyyy", Locale.getDefault());

        filteredTransactionList.clear();
        transactionsByDate.clear();

        for (TransactionModel transaction : allTransactionList) {
            System.out.println("transactiontransactiontransaction:" + transaction.getDate());

            if (transaction.getDate() == null || transaction.getDate().trim().isEmpty()) {
                continue; // Skip transactions with empty date
            }

            try {
                Date transactionDate;
                String dateStr = transaction.getDate();

                try {
                    // First try to parse with the primary format
                    transactionDate = inputDateFormat.parse(dateStr);
                } catch (ParseException e) {
                    // If that fails, try with alternate format
                    try {
                        transactionDate = alternateInputFormat.parse(dateStr);
                    } catch (ParseException e2) {
                        // Log the error but don't crash
                        System.err.println("Could not parse date: " + dateStr);
                        continue; // Skip this transaction
                    }
                }

                String transactionMonth = outputDateFormat.format(transactionDate);

                if (transaction.getTransactionType().equals(currentTransactionType) &&
                        transactionMonth.equals(currentMonth)) {

                    filteredTransactionList.add(transaction);

                    // Nhóm theo ngày
                    String dayKey = new SimpleDateFormat("EEE, dd MMMM", Locale.US).format(transactionDate);
                    if (!transactionsByDate.containsKey(dayKey)) {
                        transactionsByDate.put(dayKey, new ArrayList<>());
                    }
                    transactionsByDate.get(dayKey).add(transaction);
                }
            } catch (Exception e) {
                // Handle any other potential exception
                e.printStackTrace();
            }
        }

        // Cập nhật tổng số tiền
        updateTotalAmount();

        // Cập nhật RecyclerView
        adapter.updateData(filteredTransactionList, transactionsByDate);
    }

    private void updateTotalAmount() {
        double totalBalance = 0;
        double totalExpenditure = 0;

        for (TransactionModel transaction : allTransactionList) {
            try {
                double amount = Double.parseDouble(transaction.getAmount());

                if (transaction.getTransactionType().equals("Income")) {
                    totalBalance += amount;
                } else if (transaction.getTransactionType().equals("Expend")) {
                    totalBalance -= amount;

                    // Chỉ tính tổng chi tiêu cho tháng đã chọn
                    if (transaction.getDate() != null && !transaction.getDate().trim().isEmpty()) {
                        try {
                            Date transactionDate = parseTransactionDate(transaction.getDate());
                            if (transactionDate != null) {
                                SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
                                String transactionMonth = outputDateFormat.format(transactionDate);

                                if (transactionMonth.equals(currentMonth)) {
                                    totalExpenditure += amount;
                                }
                            }
                        } catch (Exception e) {
                            // Log the error but continue processing
                            e.printStackTrace();
                        }
                    }
                }
                // Với Loan, có thể xử lý theo logic riêng nếu cần
            } catch (NumberFormatException e) {
                // Handle invalid amount format
                e.printStackTrace();
            }
        }

        // Hiển thị tổng số dư
        tvTotalBalance.setText(String.format(Locale.US, "$%.2f", totalBalance));

        // Hiển thị tổng chi tiêu nếu tab hiện tại là Expend
        if (currentTransactionType.equals("Expend")) {
            tvTotalExpenditure.setText(String.format(Locale.US, "$%.2f", totalExpenditure));
        } else {
            tvTotalExpenditure.setText("$0.00");
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
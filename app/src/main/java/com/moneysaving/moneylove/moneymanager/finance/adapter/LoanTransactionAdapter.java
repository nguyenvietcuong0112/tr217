package com.moneysaving.moneylove.moneymanager.finance.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.activity.TransactionDetailActivity;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

public class LoanTransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SUMMARY_HEADER = 0;
    private static final int TYPE_DATE_HEADER = 1;
    private static final int TYPE_TRANSACTION = 2;

    private List<Object> items = new ArrayList<>();
    private Map<String, List<TransactionModel>> transactionsByDate = new TreeMap<>();
    private double totalBorrow = 0;
    private double totalLoan = 0;

    public void updateData(List<TransactionModel> transactions) {
        items.clear();
        transactionsByDate.clear();
        totalBorrow = 0;
        totalLoan = 0;

        for (TransactionModel transaction : transactions) {
            double amount = Double.parseDouble(transaction.getAmount());
            if ("Borrow".equals(transaction.getCategoryName())) {
                totalBorrow += amount;
            } else if ("Loan".equals(transaction.getCategoryName())) {
                totalLoan += amount;
            }

            String date = transaction.getDate();
            if (!transactionsByDate.containsKey(date)) {
                transactionsByDate.put(date, new ArrayList<>());
            }
            transactionsByDate.get(date).add(transaction);
        }

        items.add(new SummaryHeader(totalBorrow, totalLoan));

        for (Map.Entry<String, List<TransactionModel>> entry : transactionsByDate.entrySet()) {
            items.add(new DateHeader(entry.getKey()));
            List<TransactionModel> dailyTransactions = entry.getValue();
            Collections.sort(dailyTransactions, (t1, t2) -> t2.getTime().compareTo(t1.getTime()));
            items.addAll(dailyTransactions);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof SummaryHeader) return TYPE_SUMMARY_HEADER;
        if (item instanceof DateHeader) return TYPE_DATE_HEADER;
        return TYPE_TRANSACTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_SUMMARY_HEADER:
                return new SummaryHeaderViewHolder(
                        inflater.inflate(R.layout.item_loan_summary_header, parent, false)
                );
            case TYPE_DATE_HEADER:
                return new DateHeaderViewHolder(
                        inflater.inflate(R.layout.item_date_header, parent, false)
                );
            default:
                return new TransactionViewHolder(
                        inflater.inflate(R.layout.item_transaction, parent, false)
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(holder.itemView.getContext());
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        if (holder instanceof SummaryHeaderViewHolder) {
            ((SummaryHeaderViewHolder) holder).bind((SummaryHeader) item, currentCurrency);
        } else if (holder instanceof DateHeaderViewHolder) {
            DateHeader dateHeader = (DateHeader) item;
            DateHeaderViewHolder dateHeaderViewHolder = (DateHeaderViewHolder) holder;

            dateHeaderViewHolder.bind(dateHeader.date);

            BigDecimal totalLoan = BigDecimal.ZERO;
            BigDecimal totalBorrow = BigDecimal.ZERO;
            List<TransactionModel> dateTransactions = transactionsByDate.get(dateHeader.date);

            if (dateTransactions != null) {
                for (TransactionModel transaction : dateTransactions) {
                    String category = transaction.getCategoryName();
                    BigDecimal amount = new BigDecimal(transaction.getAmount());

                    if ("Loan".equals(category)) {
                        totalLoan = totalLoan.add(amount);
                    } else if ("Borrow".equals(category)) {
                        totalBorrow = totalBorrow.add(amount);
                    }
                }
            }

            BigDecimal dateTotal = totalLoan.subtract(totalBorrow);

            double totalAmount = dateTotal.doubleValue();
            dateHeaderViewHolder.setTotalAmount(totalAmount, currentCurrency);
        } else {
            ((TransactionViewHolder) holder).bind((TransactionModel) item, currentCurrency);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SummaryHeader {
        double borrowTotal;
        double loanTotal;

        SummaryHeader(double borrowTotal, double loanTotal) {
            this.borrowTotal = borrowTotal;
            this.loanTotal = loanTotal;
        }
    }

    static class DateHeader {
        String date;

        DateHeader(String date) {
            this.date = date;
        }
    }

    static class SummaryHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvBorrowAmount, tvLoanAmount;

        SummaryHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBorrowAmount = itemView.findViewById(R.id.tv_borrow_amount);
            tvLoanAmount = itemView.findViewById(R.id.tv_loan_amount);
        }

        void bind(SummaryHeader header, String currency) {
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tvBorrowAmount.setText(currency + formatter.format(header.borrowTotal));
            tvLoanAmount.setText(currency + formatter.format(header.loanTotal));
        }
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTotalAmount;
        View divider;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            divider = itemView.findViewById(R.id.divider);
        }

        void bind(String date) {
            tvDate.setText(date);
        }

        void setTotalAmount(double amount, String currency) {
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);
            String formattedAmount = formatter.format(amount);
            tvTotalAmount.setText(currency + " " + formattedAmount);
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvTime;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(TransactionModel transaction, String currency) {
            tvCategory.setText(transaction.getCategoryName().isEmpty() ? "Category" : transaction.getCategoryName());
            tvTime.setText(transaction.getTime());

            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            String prefix = "Borrow".equals(transaction.getCategoryName()) ? "- " : "+ ";
            String amountText = prefix + currency + formatter.format(Double.parseDouble(transaction.getAmount()));

            int textColor = itemView.getContext().getResources().getColor(
                    "Borrow".equals(transaction.getCategoryName()) ? R.color.red : R.color.green
            );

            tvAmount.setText(amountText);
            tvAmount.setTextColor(textColor);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), TransactionDetailActivity.class);
                intent.putExtra("transaction", transaction);
                Fragment fragment = ((AppCompatActivity) itemView.getContext())
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (fragment != null) {
                    fragment.startActivityForResult(intent, 1001);
                }
            });
        }
    }
}
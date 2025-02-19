package com.moneysaving.moneylove.moneymanager.finance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;

    private List<Object> items = new ArrayList<>();
    private Map<String, List<TransactionModel>> transactionsByDate = new TreeMap<>();

    public TransactionAdapter(List<TransactionModel> transactionList) {
        updateData(transactionList, null);
    }

    public void updateData(List<TransactionModel> transactionList, Map<String, List<TransactionModel>> transactionsByDate) {
        items.clear();

        if (transactionsByDate != null) {
            this.transactionsByDate = new TreeMap<>(transactionsByDate);
            // Chuyển đổi map thành danh sách các mục để hiển thị
            for (String date : this.transactionsByDate.keySet()) {
                // Thêm header ngày
                items.add(date);
                // Thêm các giao dịch của ngày đó
                items.addAll(this.transactionsByDate.get(date));
            }
        } else {
            items.addAll(transactionList);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_DATE_HEADER;
        } else {
            return TYPE_TRANSACTION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_DATE_HEADER) {
            DateHeaderViewHolder headerHolder = (DateHeaderViewHolder) holder;
            String date = (String) items.get(position);
            headerHolder.bind(date);

            // Tính tổng số tiền cho ngày này
            double totalAmount = 0;
            if (transactionsByDate.containsKey(date)) {
                for (TransactionModel transaction : transactionsByDate.get(date)) {
                    totalAmount += Double.parseDouble(transaction.getAmount());
                }
            }
            headerHolder.setTotalAmount(totalAmount);

        } else {
            TransactionViewHolder transactionHolder = (TransactionViewHolder) holder;
            TransactionModel transaction = (TransactionModel) items.get(position);
            transactionHolder.bind(transaction);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTotalAmount;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
        }

        void bind(String date) {
            tvDate.setText(date);
        }

        void setTotalAmount(double amount) {
            tvTotalAmount.setText(String.format("$%.2f", amount));
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount,tvTime;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(TransactionModel transaction) {
            tvCategory.setText(transaction.getCategoryName());
            tvTime.setText(transaction.getTime());
            String amountPrefix = "";
            int textColor = 0;

            switch (transaction.getTransactionType()) {
                case "Income":
                    amountPrefix = "+ ";
                    textColor = itemView.getContext().getResources().getColor(R.color.green);
                    break;
                case "Expend":
                    amountPrefix = "- ";
                    textColor = itemView.getContext().getResources().getColor(R.color.red);
                    break;
                case "Loan":
                    amountPrefix = "~ ";
                    textColor = itemView.getContext().getResources().getColor(R.color.black);
                    break;
            }

            String amountText = amountPrefix + "$" + transaction.getAmount();
            tvAmount.setText(amountText);
            tvAmount.setTextColor(textColor);

//            tvTime.setText(transaction.getTime());
        }
    }
}
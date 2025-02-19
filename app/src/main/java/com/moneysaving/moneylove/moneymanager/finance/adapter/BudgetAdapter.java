package com.moneysaving.moneylove.moneymanager.finance.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.CircularProgressView;
import com.moneysaving.moneylove.moneymanager.finance.model.BudgetItem;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private Context context;
    private List<BudgetItem> budgets;
    private BudgetItemListener listener;

    public interface BudgetItemListener {
        void onBudgetItemClick(BudgetItem item);
    }

    public BudgetAdapter(Context context, List<BudgetItem> budgets, BudgetItemListener listener) {
        this.context = context;
        this.budgets = budgets;
        this.listener = listener;
    }

    public void updateBudgets(List<BudgetItem> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
    }

    @Override
    public BudgetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BudgetViewHolder holder, int position) {
        BudgetItem item = budgets.get(position);

        holder.tvName.setText(item.getName());
        holder.tvAmount.setText(String.format("$%.2f / $%.2f",
                item.getSpentAmount(), item.getTotalAmount()));

        holder.progressView.setProgress((int) item.getProgress());
        holder.progressView.setProgressColor(item.getColor());

        holder.itemView.setOnClickListener(v -> listener.onBudgetItemClick(item));
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount;
        CircularProgressView progressView;

        BudgetViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_budget_name);
            tvAmount = itemView.findViewById(R.id.tv_budget_amount);
            progressView = itemView.findViewById(R.id.progress_view);
        }
    }
}
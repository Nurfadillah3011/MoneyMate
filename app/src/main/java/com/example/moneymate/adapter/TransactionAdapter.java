package com.example.moneymate.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.R;
import com.example.moneymate.activitiy.TransactionDetailActivity;
import com.example.moneymate.models.Transaction;
import com.example.moneymate.services.CurrencyService;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;
    private OnTransactionDeleteListener deleteListener;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private String displayCurrency;
    private CurrencyService currencyService;
    private Map<String, String> currencySymbols;

    public interface OnTransactionDeleteListener {
        void onDeleteTransaction(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions, OnTransactionDeleteListener deleteListener) {
        this(transactions, deleteListener, "IDR", null);
    }

    public TransactionAdapter(List<Transaction> transactions, OnTransactionDeleteListener deleteListener,
                              String displayCurrency, CurrencyService currencyService) {
        this.transactions = transactions;
        this.deleteListener = deleteListener;
        this.displayCurrency = displayCurrency;
        this.currencyService = currencyService;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        initializeCurrencySymbols();
        updateCurrencyFormat();
    }

    private void initializeCurrencySymbols() {
        currencySymbols = new HashMap<>();
        currencySymbols.put("IDR", "Rp");
        currencySymbols.put("USD", "$");
        currencySymbols.put("EUR", "€");
        currencySymbols.put("JPY", "¥");
        currencySymbols.put("GBP", "£");
        currencySymbols.put("AUD", "A$");
        currencySymbols.put("CAD", "C$");
        currencySymbols.put("CHF", "CHF");
        currencySymbols.put("CNY", "¥");
        currencySymbols.put("SGD", "S$");
        currencySymbols.put("KRW", "₩");
        currencySymbols.put("THB", "฿");
        currencySymbols.put("MYR", "RM");
        currencySymbols.put("PHP", "₱");
        currencySymbols.put("VND", "₫");
    }

    private void updateCurrencyFormat() {
        String symbol = currencySymbols.getOrDefault(displayCurrency, displayCurrency);

        if (displayCurrency.equals("IDR")) {
            currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        } else {
            currencyFormat = new NumberFormat() {
                @Override
                public StringBuffer format(double number, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                    return toAppendTo.append(symbol).append(" ").append(df.format(number));
                }

                @Override
                public StringBuffer format(long number, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                    return format((double) number, toAppendTo, pos);
                }

                @Override
                public Number parse(String source, java.text.ParsePosition parsePosition) {
                    return null;
                }
            };
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        holder.tvDescription.setText(transaction.getDescription());
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDate.setText(dateFormat.format(transaction.getDate()));

        // Convert and display amount in selected currency
        double amount = transaction.getAmount();
        if (currencyService != null && !displayCurrency.equals("IDR")) {
            amount = currencyService.convertCurrency(amount, "IDR", displayCurrency);
        }

        holder.tvAmount.setText(currencyFormat.format(amount));

        // Set icon and color based on transaction type
        if (transaction.getType().equals("income")) {
            holder.ivIcon.setImageResource(R.drawable.ic_income);
            holder.tvAmount.setTextColor(Color.parseColor("#10A812"));
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_expense);
            holder.tvAmount.setTextColor(ResourcesCompat.getColor(
                    holder.itemView.getContext().getResources(),
                    android.R.color.holo_red_dark, null));
        }

        // Click to view details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), TransactionDetailActivity.class);
            intent.putExtra("transaction", transaction);
            holder.itemView.getContext().startActivity(intent);
        });

        // Long click to delete
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteTransaction(transaction);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateCurrency(String newCurrency, CurrencyService newCurrencyService) {
        this.displayCurrency = newCurrency;
        this.currencyService = newCurrencyService;
        updateCurrencyFormat();
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvDescription, tvCategory, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
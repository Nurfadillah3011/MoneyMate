package com.example.moneymate.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.widget.LinearLayout;

import com.example.moneymate.database.DatabaseHelper;
import com.example.moneymate.R;
import com.example.moneymate.models.Transaction;
import com.example.moneymate.adapter.TransactionAdapter;

public class MonthlyReportFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private TextView tvMonth, tvTotalIncome, tvTotalExpense, tvBalance, tvTransactionCount;
    private Button btnPrevMonth, btnNextMonth;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmptyState;
    private TransactionAdapter adapter;
    private Calendar currentMonth;
    private NumberFormat currencyFormat;
    private SimpleDateFormat monthFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_report, container, false);

        initViews(view);
        setupRecyclerView();
        loadMonthlyData();

        return view;
    }

    private void initViews(View view) {
        dbHelper = new DatabaseHelper(getContext());
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        currentMonth = Calendar.getInstance();

        tvMonth = view.findViewById(R.id.tv_month);
        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvBalance = view.findViewById(R.id.tv_balance);
        tvTransactionCount = view.findViewById(R.id.tv_transaction_count);
        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);
        recyclerView = view.findViewById(R.id.rv_transactions);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadMonthlyData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadMonthlyData();
        });
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(new java.util.ArrayList<>(), null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadMonthlyData() {
        tvMonth.setText(monthFormat.format(currentMonth.getTime()));

        // Add try-catch to handle potential database errors
        try {
            List<Transaction> transactions = dbHelper.getTransactionsForMonth(
                    currentMonth.get(Calendar.YEAR),
                    currentMonth.get(Calendar.MONTH));

            // Calculate totals
            double totalIncome = 0;
            double totalExpense = 0;

            for (Transaction transaction : transactions) {
                if (transaction.getType().equals("income")) {
                    totalIncome += transaction.getAmount();
                } else {
                    totalExpense += transaction.getAmount();
                }
            }

            double balance = totalIncome - totalExpense;

            // Update UI
            tvTotalIncome.setText(currencyFormat.format(totalIncome));
            tvTotalExpense.setText(currencyFormat.format(totalExpense));
            tvBalance.setText(currencyFormat.format(balance));
            tvTransactionCount.setText(String.valueOf(transactions.size()) + " transaksi");

            // Set balance color
            if (balance >= 0) {
                tvBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // Handle empty state
            if (transactions.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);

                // Update adapter
                adapter = new TransactionAdapter(transactions, null);
                recyclerView.setAdapter(adapter);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Handle error - show empty state
            recyclerView.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);

            // Reset values to 0
            tvTotalIncome.setText(currencyFormat.format(0));
            tvTotalExpense.setText(currencyFormat.format(0));
            tvBalance.setText(currencyFormat.format(0));
            tvTransactionCount.setText("0 transaksi");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
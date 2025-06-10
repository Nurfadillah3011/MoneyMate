package com.example.moneymate.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymate.services.CurrencyService;
import com.example.moneymate.database.DatabaseHelper;
import com.example.moneymate.R;
import com.example.moneymate.models.Transaction;
import com.example.moneymate.adapter.TransactionAdapter;
import com.example.moneymate.activitiy.ChartActivity;
import com.example.moneymate.activitiy.CurrencyActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactions = new ArrayList<>();
    private TextView tvBalance, tvIncome, tvExpense, tvCurrencyBalance;
    private LinearLayout emptyState;
    private ImageButton btnCurrencyConverter;
    private NumberFormat currencyFormat;
    private CurrencyService currencyService;
    private String displayCurrency = "IDR";
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "MoneyMatePrefs";
    private static final String KEY_DISPLAY_CURRENCY = "display_currency";
    private Map<String, String> currencySymbols;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            initViews(view);
            setupRecyclerView();
            loadTransactions();
            updateSummary();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            Toast.makeText(getContext(), "Error initializing fragment", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void initViews(View view) {
        try {
            dbHelper = new DatabaseHelper(getContext());

            // Initialize currency service with proper context check
            if (getContext() != null) {
                currencyService = new CurrencyService(getContext());
            } else {
                Log.e(TAG, "Context is null during initialization");
                return;
            }

            prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
            displayCurrency = prefs.getString(KEY_DISPLAY_CURRENCY, "IDR");
            initializeCurrencySymbols();
            updateCurrencyFormat();

            // Initialize UI components
            tvBalance = view.findViewById(R.id.tv_balance);
            tvIncome = view.findViewById(R.id.tv_income);
            tvExpense = view.findViewById(R.id.tv_expense);
            tvCurrencyBalance = view.findViewById(R.id.tv_currency_balance);
            btnCurrencyConverter = view.findViewById(R.id.action_currency_converter);
            recyclerView = view.findViewById(R.id.recycler_view);
            emptyState = view.findViewById(R.id.empty_state);

            // Set up currency button
            ImageButton btnCurrency = view.findViewById(R.id.btn_currency);
            btnCurrency.setOnClickListener(v -> {
                try {
                    showCurrencyDialog();
                } catch (Exception e) {
                    Log.e(TAG, "Error showing currency dialog", e);
                    Toast.makeText(getContext(), "Error changing currency", Toast.LENGTH_SHORT).show();
                }
            });

            // Set up chart button
            ImageButton btnChart = view.findViewById(R.id.btn_chart);
            btnChart.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(requireActivity(), ChartActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ChartActivity", e);
                    Toast.makeText(getContext(), "Error opening chart", Toast.LENGTH_SHORT).show();
                }
            });

            // Set up currency converter button
            btnCurrencyConverter.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(requireActivity(), CurrencyActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting CurrencyActivity", e);
                    Toast.makeText(getContext(), "Error opening currency converter", Toast.LENGTH_SHORT).show();
                }
            });

            loadCurrencyBalance();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(getContext(), "Error initializing views", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCurrencyDialog() {
        if (getContext() == null || currencyService == null) {
            Toast.makeText(requireActivity(), "Service not available", Toast.LENGTH_SHORT).show();
            return;
        }

        currencyService.getExchangeRates(new CurrencyService.CurrencyCallback() {
            @Override
            public void onSuccess(Map<String, Double> rates) {
                requireActivity().runOnUiThread(() -> {
                    try {
                        String[] currencies = currencyService.getAvailableCurrencies();
                        String[] currencyCodes = new String[currencies.length];
                        for (int i = 0; i < currencies.length; i++) {
                            currencyCodes[i] = currencies[i].contains(" - ") ?
                                    currencies[i].split(" - ")[0] : currencies[i];
                        }

                        int checkedItem = Arrays.asList(currencyCodes).indexOf(displayCurrency);

                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setTitle("Pilih Mata Uang Tampilan");
                        builder.setSingleChoiceItems(currencyCodes, checkedItem, (dialog, which) -> {
                            try {
                                String selected = currencyCodes[which];
                                prefs.edit().putString(KEY_DISPLAY_CURRENCY, selected).apply();
                                displayCurrency = selected;
                                updateCurrencyFormat();
                                updateSummary();
                                if (adapter != null) {
                                    adapter.updateCurrency(displayCurrency, currencyService);
                                }
                                refreshData();
                                Toast.makeText(getContext(), "Mata uang diubah ke: " + selected, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } catch (Exception e) {
                                Log.e(TAG, "Error selecting currency", e);
                            }
                        });
                        builder.setNegativeButton("Batal", null);
                        builder.show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error showing currency dialog", e);
                    }
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    try {
                        Log.e(TAG, "Currency service error: " + error);
                        String[] defaultCurrencies = {"IDR", "USD", "EUR", "JPY", "GBP"};
                        int checkedItem = Arrays.asList(defaultCurrencies).indexOf(displayCurrency);

                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setTitle("Pilih Mata Uang Tampilan");
                        builder.setSingleChoiceItems(defaultCurrencies, checkedItem, (dialog, which) -> {
                            try {
                                String selected = defaultCurrencies[which];
                                prefs.edit().putString(KEY_DISPLAY_CURRENCY, selected).apply();
                                displayCurrency = selected;
                                updateCurrencyFormat();
                                updateSummary();
                                if (adapter != null) {
                                    adapter.updateCurrency(displayCurrency, currencyService);
                                }
                                refreshData();
                                Toast.makeText(getContext(), "Mata uang diubah ke: " + selected, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } catch (Exception e) {
                                Log.e(TAG, "Error selecting fallback currency", e);
                            }
                        });
                        builder.setNegativeButton("Batal", null);
                        builder.show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error showing fallback currency dialog", e);
                    }
                });
            }

            @Override
            public void onNetworkError() {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Tidak ada koneksi internet, menggunakan data lokal",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
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
        try {
            String symbol = currencySymbols.getOrDefault(displayCurrency, displayCurrency);
            if (displayCurrency.equals("IDR")) {
                currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            } else {
                currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                currencyFormat.setCurrency(java.util.Currency.getInstance(displayCurrency));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating currency format", e);
            currencyFormat = NumberFormat.getCurrencyInstance();
        }
    }

    private void setupRecyclerView() {
        if (getContext() == null) return;

        adapter = new TransactionAdapter(transactions, this::deleteTransaction, displayCurrency, currencyService);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadTransactions() {
        try {
            if (dbHelper == null) {
                dbHelper = new DatabaseHelper(getContext());
            }

            transactions.clear();
            transactions.addAll(dbHelper.getAllTransactions());

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            if (transactions.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading transactions", e);
            Toast.makeText(getContext(), "Error loading transactions", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSummary() {
        try {
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
            final double finalTotalIncome = totalIncome;
            final double finalTotalExpense = totalExpense;
            final double finalBalance = balance;

            if (!displayCurrency.equals("IDR") && currencyService != null) {
                currencyService.getExchangeRates(new CurrencyService.CurrencyCallback() {
                    @Override
                    public void onSuccess(Map<String, Double> rates) {
                        requireActivity().runOnUiThread(() -> {
                            try {
                                double convertedBalance = currencyService.convertCurrency(finalBalance, "IDR", displayCurrency);
                                double convertedIncome = currencyService.convertCurrency(finalTotalIncome, "IDR", displayCurrency);
                                double convertedExpense = currencyService.convertCurrency(finalTotalExpense, "IDR", displayCurrency);
                                updateSummaryUI(convertedBalance, convertedIncome, convertedExpense);

                                if (!displayCurrency.equals("IDR")) {
                                    NumberFormat idrFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                                    tvCurrencyBalance.setText("(" + idrFormat.format(finalBalance) + ")");
                                    tvCurrencyBalance.setVisibility(View.VISIBLE);
                                } else {
                                    tvCurrencyBalance.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting currency", e);
                                updateSummaryUI(finalBalance, finalTotalIncome, finalTotalExpense);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            updateSummaryUI(finalBalance, finalTotalIncome, finalTotalExpense);
                            tvCurrencyBalance.setText("Gagal konversi mata uang");
                            tvCurrencyBalance.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onNetworkError() {
                        requireActivity().runOnUiThread(() -> {
                            updateSummaryUI(finalBalance, finalTotalIncome, finalTotalExpense);
                            tvCurrencyBalance.setText("Menggunakan kurs lokal");
                            tvCurrencyBalance.setVisibility(View.VISIBLE);
                        });
                    }
                });
            } else {
                updateSummaryUI(balance, totalIncome, totalExpense);
                tvCurrencyBalance.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating summary", e);
        }
    }

    private void updateSummaryUI(double balance, double income, double expense) {
        if (tvBalance == null || tvIncome == null || tvExpense == null) return;

        try {
            tvBalance.setText(currencyFormat.format(balance));
            tvIncome.setText(currencyFormat.format(income));
            tvExpense.setText(currencyFormat.format(expense));

            if (balance >= 0) {
                tvBalance.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                tvBalance.setTextColor(Color.parseColor("#ff3c3c"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating summary UI", e);
        }
    }

    private void loadCurrencyBalance() {
        if (!displayCurrency.equals("IDR") && currencyService != null) {
            currencyService.getExchangeRates(new CurrencyService.CurrencyCallback() {
                @Override
                public void onSuccess(Map<String, Double> rates) {
                    requireActivity().runOnUiThread(() -> updateSummary());
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Gagal memuat kurs mata uang", Toast.LENGTH_SHORT).show();
                        updateSummary();
                    });
                }

                @Override
                public void onNetworkError() {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(),
                                "Tidak ada koneksi internet, menggunakan kurs lokal",
                                Toast.LENGTH_LONG).show();
                        updateSummary();
                    });
                }
            });
        }
    }

    private void deleteTransaction(Transaction transaction) {
        try {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Transaksi")
                    .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        try {
                            if (dbHelper.deleteTransaction(transaction.getId())) {
                                int position = transactions.indexOf(transaction);
                                transactions.remove(transaction);
                                if (adapter != null) {
                                    adapter.notifyItemRemoved(position);
                                }
                                updateSummary();
                                loadTransactions();
                                Toast.makeText(getActivity(), "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting transaction", e);
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing delete dialog", e);
        }
    }

    public void refreshData() {
        try {
            displayCurrency = prefs.getString(KEY_DISPLAY_CURRENCY, "IDR");
            updateCurrencyFormat();
            loadTransactions();
            updateSummary();
            loadCurrencyBalance();
            if (adapter != null) {
                adapter.updateCurrency(displayCurrency, currencyService);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing data", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
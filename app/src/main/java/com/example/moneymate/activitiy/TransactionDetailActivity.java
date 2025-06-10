package com.example.moneymate.activitiy;


import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymate.services.CurrencyService;
import com.example.moneymate.database.DatabaseHelper;
import com.example.moneymate.R;
import com.example.moneymate.models.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

public class TransactionDetailActivity extends AppCompatActivity {
    private Transaction transaction;
    private DatabaseHelper dbHelper;
    private CurrencyService currencyService;
    private NumberFormat currencyFormat;
    private String displayCurrency;
    private Map<String, String> currencySymbols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        dbHelper = new DatabaseHelper(this);
        currencyService = new CurrencyService(this);
        displayCurrency = getSharedPreferences("MoneyMatePrefs", MODE_PRIVATE)
                .getString("display_currency", "IDR");

        // Initialize currency symbols
        initializeCurrencySymbols();
        updateCurrencyFormat();

        // Get transaction from intent
        transaction = (Transaction) getIntent().getSerializableExtra("transaction");
        if (transaction == null) {
            Toast.makeText(this, "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detail Transaksi");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        TextView tvDescription = findViewById(R.id.tv_detail_description);
        TextView tvCategory = findViewById(R.id.tv_detail_category);
        TextView tvType = findViewById(R.id.tv_detail_type);
        TextView tvDate = findViewById(R.id.tv_detail_date);
        TextView tvAmount = findViewById(R.id.tv_detail_amount);
        Button btnDelete = findViewById(R.id.btn_delete);

        // Set transaction details
        tvDescription.setText(transaction.getDescription());
        tvCategory.setText(transaction.getCategory());
        tvType.setText(transaction.getType().equals("income") ? "Pemasukan" : "Pengeluaran");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        tvDate.setText(dateFormat.format(transaction.getDate()));

        // Convert and display amount
        double amount = transaction.getAmount();
        if (!displayCurrency.equals("IDR")) {
            amount = currencyService.convertCurrency(amount, "IDR", displayCurrency);
        }
        tvAmount.setText(currencyFormat.format(amount));
        if (transaction.getType().equals("income")) {
            tvAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Delete button
        btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Hapus Transaksi")
                    .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        if (dbHelper.deleteTransaction(transaction.getId())) {
                            Toast.makeText(this, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(this, "Gagal menghapus transaksi", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });
    }

    private void initializeCurrencySymbols() {
        currencySymbols = new java.util.HashMap<>();
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
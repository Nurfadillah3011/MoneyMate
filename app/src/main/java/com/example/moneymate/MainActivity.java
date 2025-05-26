package com.example.moneymate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactions;
    private TextView tvBalance, tvIncome, tvExpense;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        loadTransactions();
        updateSummary();
    }

    private void initViews() {
        dbHelper = new DatabaseHelper(this);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        tvBalance = findViewById(R.id.tv_balance);
        tvIncome = findViewById(R.id.tv_income);
        tvExpense = findViewById(R.id.tv_expense);
        recyclerView = findViewById(R.id.recycler_view);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showAddTransactionDialog());
    }

    private void setupRecyclerView() {
        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(transactions, this::deleteTransaction);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadTransactions() {
        transactions.clear();
        transactions.addAll(dbHelper.getAllTransactions());
        adapter.notifyDataSetChanged();
    }

    private void updateSummary() {
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

        tvBalance.setText(currencyFormat.format(balance));
        tvIncome.setText(currencyFormat.format(totalIncome));
        tvExpense.setText(currencyFormat.format(totalExpense));

        // Set color based on balance
        if (balance >= 0) {
            tvBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void showAddTransactionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);

        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_type);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);
        Button btnDate = dialogView.findViewById(R.id.btn_date);

        // Setup spinners
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.transaction_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Date picker
        Calendar selectedDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        btnDate.setText(dateFormat.format(selectedDate.getTime()));

        btnDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        btnDate.setText(dateFormat.format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Tambah Transaksi")
                .setView(dialogView)
                .setPositiveButton("Simpan", (d, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String type = spinnerType.getSelectedItem().toString().toLowerCase();
                    String category = spinnerCategory.getSelectedItem().toString();

                    if (amountStr.isEmpty() || description.isEmpty()) {
                        Toast.makeText(this, "Mohon lengkapi semua field", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double amount = Double.parseDouble(amountStr);
                        Transaction transaction = new Transaction(0, amount, description, type, category, selectedDate.getTime());

                        long id = dbHelper.addTransaction(transaction);
                        if (id > 0) {
                            transaction.setId((int) id);
                            transactions.add(0, transaction);
                            adapter.notifyItemInserted(0);
                            recyclerView.scrollToPosition(0);
                            updateSummary();
                            Toast.makeText(this, "Transaksi berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Format jumlah tidak valid", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .create();

        dialog.show();
    }

    private void deleteTransaction(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    if (dbHelper.deleteTransaction(transaction.getId())) {
                        int position = transactions.indexOf(transaction);
                        transactions.remove(transaction);
                        adapter.notifyItemRemoved(position);
                        updateSummary();
                        Toast.makeText(this, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_chart) {
            startActivity(new Intent(this, ChartActivity.class));
            return true;
        } else if (id == R.id.action_monthly_report) {
            startActivity(new Intent(this, MonthlyReportActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
        updateSummary();
    }
}
package com.example.moneymate;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChartActivity extends AppCompatActivity {
    private static final String TAG = "ChartActivity";
    private DatabaseHelper dbHelper;
    private CurrencyService currencyService;
    private PieChart pieChart;
    private TextView tvMonth, tvTotalExpense, tvEmptyMessage;
    private Button btnPrevMonth, btnNextMonth, btnRefresh;
    private Calendar currentMonth;
    private NumberFormat currencyFormat;
    private SimpleDateFormat monthFormat;
    private String displayCurrency;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "MoneyMatePrefs";
    private static final String KEY_DISPLAY_CURRENCY = "display_currency";
    private Map<String, String> currencySymbols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        Log.d(TAG, "onCreate started");

        try {
            setupToolbar();
            initializeComponents();
            initViews();
            setupChart();
            loadChartData();

            Log.d(TAG, "onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error loading chart: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Laporan Pengeluaran");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeComponents() {
        try {
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            displayCurrency = prefs.getString(KEY_DISPLAY_CURRENCY, "IDR");

            dbHelper = new DatabaseHelper(this);
            currencyService = new CurrencyService(this);

            initializeCurrencySymbols();
            updateCurrencyFormat();

            monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            currentMonth = Calendar.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing components", e);
            Toast.makeText(this, "Error initializing components", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        try {
            pieChart = findViewById(R.id.pie_chart);
            tvMonth = findViewById(R.id.tv_month);
            tvTotalExpense = findViewById(R.id.tv_total_expense);
            btnPrevMonth = findViewById(R.id.btn_prev_month);
            btnNextMonth = findViewById(R.id.btn_next_month);
            btnRefresh = findViewById(R.id.btn_refresh);

            btnRefresh.setOnClickListener(v -> {
                Log.d(TAG, "Refresh button clicked");
                loadChartData();
            });

            btnPrevMonth.setOnClickListener(v -> {
                Log.d(TAG, "Previous month button clicked");
                if (currentMonth != null) {
                    currentMonth.add(Calendar.MONTH, -1);
                    loadChartData();
                } else {
                    Log.e(TAG, "currentMonth is null!");
                }
            });

            btnNextMonth.setOnClickListener(v -> {
                Log.d(TAG, "Next month button clicked");
                if (currentMonth != null) {
                    currentMonth.add(Calendar.MONTH, 1);
                    loadChartData();
                } else {
                    Log.e(TAG, "currentMonth is null!");
                }
            });

            // Load initial data
            loadChartData();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
        }
    }
    private void initializeCurrencySymbols() {
        currencySymbols = new HashMap<>();
        currencySymbols.put("IDR", "Rp");
        currencySymbols.put("USD", "$");
        currencySymbols.put("EUR", "€");
        currencySymbols.put("JPY", "¥");
        currencySymbols.put("GBP", "£");
    }

    private void updateCurrencyFormat() {
        try {
            String symbol = currencySymbols.getOrDefault(displayCurrency, displayCurrency);
            if (displayCurrency.equals("IDR")) {
                currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            } else {
                currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating currency format", e);
            currencyFormat = NumberFormat.getCurrencyInstance();
        }
    }

    private void setupChart() {
        try {
            if (pieChart == null) {
                Log.w(TAG, "PieChart is null, skipping setup");
                return;
            }

            pieChart.setUsePercentValues(true);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(Color.TRANSPARENT);
            pieChart.setHoleRadius(40f);
            pieChart.setTransparentCircleRadius(50f);
            pieChart.setDrawCenterText(true);
            pieChart.setCenterText("Pengeluaran\nper Kategori");
            pieChart.setCenterTextSize(12f);

            if (pieChart.getDescription() != null) {
                pieChart.getDescription().setEnabled(false);
            }

            if (pieChart.getLegend() != null) {
                pieChart.getLegend().setEnabled(true);
                pieChart.getLegend().setTextSize(10f);
            }

            Log.d(TAG, "Chart setup completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up chart", e);
        }
    }

    private void loadChartData() {
        try {
            if (tvMonth != null && monthFormat != null && currentMonth != null) {
                tvMonth.setText(monthFormat.format(currentMonth.getTime()));
            }

            if (btnRefresh != null) {
                btnRefresh.setVisibility(android.view.View.GONE);
            }

            if (dbHelper == null) {
                Log.e(TAG, "DatabaseHelper is null");
                showEmptyState();
                return;
            }

            // Get data from database
            List<DatabaseHelper.CategorySpending> categorySpending = dbHelper.getCategorySpending(
                    currentMonth.get(Calendar.YEAR),
                    currentMonth.get(Calendar.MONTH));

            if (categorySpending == null || categorySpending.isEmpty()) {
                showEmptyState();
                return;
            }

            showChart();
            updateChartData(categorySpending);
        } catch (Exception e) {
            Log.e(TAG, "Error loading chart data", e);
            Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            showEmptyState();
        }
    }

    private void showEmptyState() {
        try {
            if (pieChart != null) {
                pieChart.setVisibility(android.view.View.GONE);
            }
            if (tvEmptyMessage != null) {
                tvEmptyMessage.setVisibility(android.view.View.VISIBLE);
                tvEmptyMessage.setText("Tidak ada data pengeluaran untuk bulan ini");
            }
            if (tvTotalExpense != null && currencyFormat != null) {
                tvTotalExpense.setText(currencyFormat.format(0));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing empty state", e);
        }
    }

    private void showChart() {
        try {
            if (pieChart != null) {
                pieChart.setVisibility(android.view.View.VISIBLE);
            }
            if (tvEmptyMessage != null) {
                tvEmptyMessage.setVisibility(android.view.View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing chart", e);
        }
    }

    private void updateChartData(List<DatabaseHelper.CategorySpending> categorySpending) {
        try {
            double totalExpense = 0;
            for (DatabaseHelper.CategorySpending spending : categorySpending) {
                totalExpense += spending.getAmount();
            }

            final double finalTotalExpense = totalExpense;
            final List<DatabaseHelper.CategorySpending> finalCategorySpending = categorySpending;

            if (!displayCurrency.equals("IDR") && currencyService != null) {
                currencyService.getExchangeRates(new CurrencyService.CurrencyCallback() {
                    @Override
                    public void onSuccess(Map<String, Double> rates) {
                        try {
                            double convertedTotal = currencyService.convertCurrency(finalTotalExpense, "IDR", displayCurrency);
                            updateUI(convertedTotal, finalCategorySpending);
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting currency", e);
                            updateUI(finalTotalExpense, finalCategorySpending);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Currency conversion error: " + error);
                        updateUI(finalTotalExpense, finalCategorySpending);
                        if (btnRefresh != null) {
                            btnRefresh.setVisibility(android.view.View.VISIBLE);
                        }
                    }
                });
            } else {
                updateUI(finalTotalExpense, finalCategorySpending);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating chart data", e);
        }
    }

    private void updateUI(double total, List<DatabaseHelper.CategorySpending> spending) {
        try {
            if (tvTotalExpense != null && currencyFormat != null) {
                tvTotalExpense.setText(currencyFormat.format(total));
            }
            updateChartWithData(spending, total);
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
        }
    }

    private void updateChartWithData(List<DatabaseHelper.CategorySpending> categorySpending, double totalExpense) {
        try {
            if (pieChart == null) {
                Log.w(TAG, "PieChart is null, cannot update chart data");
                return;
            }

            ArrayList<PieEntry> entries = new ArrayList<>();

            for (DatabaseHelper.CategorySpending spending : categorySpending) {
                double amount = spending.getAmount();
                if (!displayCurrency.equals("IDR") && currencyService != null) {
                    amount = currencyService.convertCurrency(amount, "IDR", displayCurrency);
                }
                float percentage = (float) ((amount / totalExpense) * 100);
                entries.add(new PieEntry(percentage, spending.getCategory()));
            }

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(getChartColors());
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueFormatter(new PercentFormatter(pieChart));

            PieData data = new PieData(dataSet);
            pieChart.setData(data);
            pieChart.invalidate();
            pieChart.animateY(1000);

            Log.d(TAG, "Chart updated successfully with " + entries.size() + " entries");
        } catch (Exception e) {
            Log.e(TAG, "Error updating chart with data", e);
        }
    }

    private ArrayList<Integer> getChartColors() {
        ArrayList<Integer> colors = new ArrayList<>();

        // Add predefined colors
        colors.add(Color.rgb(255, 102, 102)); // Light Red
        colors.add(Color.rgb(102, 178, 255)); // Light Blue
        colors.add(Color.rgb(102, 255, 102)); // Light Green
        colors.add(Color.rgb(255, 255, 102)); // Light Yellow
        colors.add(Color.rgb(255, 178, 102)); // Light Orange
        colors.add(Color.rgb(178, 102, 255)); // Light Purple
        colors.add(Color.rgb(255, 102, 178)); // Light Pink
        colors.add(Color.rgb(102, 255, 178)); // Light Cyan

        // Add more colors from ColorTemplate if needed
        try {
            for (int color : ColorTemplate.MATERIAL_COLORS) {
                colors.add(color);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding ColorTemplate colors", e);
        }

        return colors;
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (prefs != null) {
                displayCurrency = prefs.getString(KEY_DISPLAY_CURRENCY, "IDR");
                updateCurrencyFormat();
                loadChartData();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        pieChart = null;
        dbHelper = null;
        currencyService = null;
    }
}
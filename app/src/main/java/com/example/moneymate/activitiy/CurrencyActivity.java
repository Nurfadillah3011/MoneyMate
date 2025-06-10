package com.example.moneymate.activitiy;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.moneymate.services.CurrencyService;
import com.example.moneymate.utils.NetworkUtils;
import com.example.moneymate.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class CurrencyActivity extends AppCompatActivity {
    private static final String TAG = "CurrencyActivity";

    private CurrencyService currencyService;
    private EditText etAmount;
    private Spinner spinnerFromCurrency, spinnerToCurrency;
    private TextView tvResult, tvLastUpdate;
    private ImageButton btnRefresh;
    private ImageButton btnSwapCurrencies;
    private ProgressBar progressBar;
    private DecimalFormat decimalFormat;
    private View noNetworkView;
    private View contentView;
    private Button btnRetry, btnSettings;
    private boolean isRatesLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        initViews();
        setupSpinners();

        // Check network on startup
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoNetwork();
        } else {
            loadExchangeRates();
        }
    }

    private void initViews() {
        setupToolbar();

        currencyService = new CurrencyService(this);
        decimalFormat = new DecimalFormat("#,##0.00####"); // Allow more decimal places for small amounts

        // Initialize views
        etAmount = findViewById(R.id.et_amount);
        spinnerFromCurrency = findViewById(R.id.spinner_from_currency);
        spinnerToCurrency = findViewById(R.id.spinner_to_currency);
        tvResult = findViewById(R.id.tv_result);
        tvLastUpdate = findViewById(R.id.tv_last_update);
        btnSwapCurrencies = findViewById(R.id.btn_swap_currencies);
        progressBar = findViewById(R.id.progress_bar);
        btnRefresh = findViewById(R.id.btn_refresh);
        noNetworkView = findViewById(R.id.no_network_view);
        contentView = findViewById(R.id.content_view);
        btnRetry = noNetworkView.findViewById(R.id.btn_retry);

        // Set toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Konversi Mata Uang");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set up retry button
        btnRetry.setOnClickListener(v -> {
            if (NetworkUtils.isNetworkAvailable(this)) {
                showContent();
                loadExchangeRates();
            } else {
                Toast.makeText(this, "Masih tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
            }
        });


        // Setup refresh button
        btnRefresh.setOnClickListener(v -> {
            Log.d(TAG, "Refresh button clicked");
            if (!NetworkUtils.isNetworkAvailable(this)) {
                showNoNetwork();
            } else {
                loadExchangeRates();
            }
        });

        // Setup text watcher for automatic conversion
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isRatesLoaded) {
                    convertCurrency();
                }
            }
        });

        // Swap currencies button
        btnSwapCurrencies.setOnClickListener(v -> swapCurrencies());
    }

    private void showNoNetwork() {
        contentView.setVisibility(View.GONE);
        noNetworkView.setVisibility(View.VISIBLE);

        // Update status text
        TextView tvStatus = noNetworkView.findViewById(R.id.tv_network_status);
        tvStatus.setText("Status: Tidak ada koneksi (" + NetworkUtils.getNetworkType(this) + ")");

        // Load cached rates if available
        if (currencyService.hasRates()) {
            isRatesLoaded = true;
            tvLastUpdate.setText("Menggunakan data kurs offline terakhir");
            convertCurrency();
        } else {
            isRatesLoaded = false;
            tvResult.setText("Tidak ada data kurs yang tersedia");
        }
    }

    private void showContent() {
        noNetworkView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Kembali ke activity sebelumnya
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSpinners() {
        String[] currencies = currencyService.getAvailableCurrencies();
        Log.d(TAG, "Available currencies: " + currencies.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFromCurrency.setAdapter(adapter);
        spinnerToCurrency.setAdapter(adapter);

        // Set default values
        setSpinnerSelection(spinnerFromCurrency, "IDR - Indonesia");
        setSpinnerSelection(spinnerToCurrency, "USD - Amerika Serikat");

        // Setup spinner listeners
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isRatesLoaded) {
                    convertCurrency();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerFromCurrency.setOnItemSelectedListener(spinnerListener);
        spinnerToCurrency.setOnItemSelectedListener(spinnerListener);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
                Log.d(TAG, "Set spinner selection: " + value + " at position " + position);
            } else {
                Log.w(TAG, "Could not find position for: " + value);
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Currency Exchange");
        }
    }

    private void loadExchangeRates() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoNetwork();
            return;
        }

        Log.d(TAG, "Loading exchange rates...");
        isRatesLoaded = false;
        progressBar.setVisibility(View.VISIBLE);
        tvLastUpdate.setText("Memuat data...");
        btnRefresh.setVisibility(View.GONE);
        tvResult.setText("Menunggu data...");

        currencyService.getExchangeRates(new CurrencyService.CurrencyCallback() {
            @Override
            public void onSuccess(Map<String, Double> rates) {
                Log.d(TAG, "Exchange rates loaded successfully: " + rates.size() + " currencies");
                runOnUiThread(() -> {
                    isRatesLoaded = true;
                    progressBar.setVisibility(View.GONE);
                    tvLastUpdate.setText("Data terakhir diperbarui: " +
                            new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
                    btnRefresh.setVisibility(View.VISIBLE);

                    // Trigger conversion if amount is already entered
                    convertCurrency();

                    // Show success message
                    Toast.makeText(CurrencyActivity.this,
                            "Data kurs berhasil dimuat", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading exchange rates: " + error);
                runOnUiThread(() -> {
                    isRatesLoaded = true; // Allow conversion with default rates
                    progressBar.setVisibility(View.GONE);
                    tvLastUpdate.setText("Menggunakan data cadangan: " + error);
                    btnRefresh.setVisibility(View.VISIBLE);

                    // Try conversion with fallback rates
                    convertCurrency();

                    Toast.makeText(CurrencyActivity.this,
                            "Menggunakan data kurs cadangan", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onNetworkError() {
                runOnUiThread(() -> {
                    isRatesLoaded = true;
                    progressBar.setVisibility(View.GONE);
                    tvLastUpdate.setText("Tidak ada koneksi internet - menggunakan data cadangan");
                    btnRefresh.setVisibility(View.VISIBLE);
                    convertCurrency();
                    showNoNetwork();
                    Toast.makeText(CurrencyActivity.this,
                            "Tidak ada koneksi internet, menggunakan data cadangan", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void convertCurrency() {
        try {
            // Validate UI components
            if (!validateComponents()) {
                return;
            }

            String amountStr = etAmount.getText().toString().trim();

            // Handle empty input
            if (amountStr.isEmpty()) {
                tvResult.setText("Masukkan jumlah untuk konversi");
                return;
            }

            // Parse amount
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount < 0) {
                    tvResult.setText("Jumlah tidak boleh negatif");
                    return;
                }
            } catch (NumberFormatException e) {
                tvResult.setText("Format angka tidak valid");
                return;
            }

            // Get selected currencies
            String fromCurrencyFull = spinnerFromCurrency.getSelectedItem().toString();
            String toCurrencyFull = spinnerToCurrency.getSelectedItem().toString();

            // Extract currency codes (first 3 characters)
            String fromCurrency = extractCurrencyCode(fromCurrencyFull);
            String toCurrency = extractCurrencyCode(toCurrencyFull);

            Log.d(TAG, String.format("Converting %.2f from %s to %s", amount, fromCurrency, toCurrency));

            // Perform conversion
            double convertedAmount = currencyService.convertCurrency(amount, fromCurrency, toCurrency);

            // Format and display result
            String result = formatResult(convertedAmount, toCurrency, toCurrencyFull);
            tvResult.setText(result);

            Log.d(TAG, String.format("Conversion complete: %.2f %s = %.6f %s",
                    amount, fromCurrency, convertedAmount, toCurrency));

        } catch (Exception e) {
            Log.e(TAG, "Error in convertCurrency: " + e.getMessage(), e);
            tvResult.setText("Terjadi kesalahan dalam konversi");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateComponents() {
        if (etAmount == null || spinnerFromCurrency == null || spinnerToCurrency == null ||
                tvResult == null || currencyService == null) {
            Log.e(TAG, "UI components not initialized properly");
            return false;
        }

        if (spinnerFromCurrency.getSelectedItem() == null ||
                spinnerToCurrency.getSelectedItem() == null) {
            tvResult.setText("Pilih mata uang terlebih dahulu");
            return false;
        }

        if (!currencyService.hasRates()) {
            tvResult.setText("Data kurs tidak tersedia");
            return false;
        }

        return true;
    }

    private String extractCurrencyCode(String currencyFull) {
        if (currencyFull != null && currencyFull.length() >= 3) {
            return currencyFull.substring(0, 3);
        }
        return "USD"; // Default fallback
    }

    private String formatResult(double amount, String currencyCode, String currencyFull) {
        // Use appropriate decimal places based on currency and amount
        DecimalFormat formatter;

        if (amount < 0.01) {
            // For very small amounts, show more decimal places
            formatter = new DecimalFormat("#,##0.00######");
        } else if (currencyCode.equals("JPY") || currencyCode.equals("KRW") ||
                currencyCode.equals("IDR") || currencyCode.equals("VND")) {
            // For currencies that don't typically use decimal places
            formatter = new DecimalFormat("#,##0");
        } else {
            // Standard formatting for most currencies
            formatter = new DecimalFormat("#,##0.00");
        }

        return formatter.format(amount) + " " + currencyCode;
    }

    private void swapCurrencies() {
        try {
            int fromPosition = spinnerFromCurrency.getSelectedItemPosition();
            int toPosition = spinnerToCurrency.getSelectedItemPosition();

            spinnerFromCurrency.setSelection(toPosition);
            spinnerToCurrency.setSelection(fromPosition);

            Log.d(TAG, "Currencies swapped");

            // Convert immediately if amount is entered
            if (isRatesLoaded && !etAmount.getText().toString().trim().isEmpty()) {
                convertCurrency();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error swapping currencies: " + e.getMessage());
            Toast.makeText(this, "Gagal menukar mata uang", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
        Log.d(TAG, "CurrencyActivity destroyed");
    }
}
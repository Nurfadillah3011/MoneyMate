package com.example.moneymate.fragment;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.moneymate.services.CurrencyService;
import com.example.moneymate.database.DatabaseHelper;
import com.example.moneymate.R;
import com.example.moneymate.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionFragment extends Fragment {
    private static final String TAG = "AddTransactionFragment";

    private EditText etAmount, etDescription;
    private Spinner spinnerType, spinnerCategory;
    private Button btnDate, btnSave;
    private Calendar selectedDate;
    private DatabaseHelper dbHelper;
    private CurrencyService currencyService;
    private String displayCurrency;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);

            Log.d(TAG, "Fragment view inflated successfully");

            // Initialize components with error handling
            initNavController();
            initDatabase();
            initCurrencyService();

            selectedDate = Calendar.getInstance();
            loadDisplayCurrency();

            initViews(view);
            setupSpinners();
            setupDatePicker();
            setupSaveButton();

            Log.d(TAG, "Fragment initialization completed");
            return view;

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error loading page", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void initNavController() {
        try {
            if (getActivity() != null) {
                navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                Log.d(TAG, "NavController initialized successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing NavController: " + e.getMessage(), e);
        }
    }

    private void initDatabase() {
        try {
            if (getContext() != null) {
                dbHelper = new DatabaseHelper(getContext());
                Log.d(TAG, "DatabaseHelper initialized successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing DatabaseHelper: " + e.getMessage(), e);
        }
    }

    private void initCurrencyService() {
        try {
            if (getContext() != null) {
                currencyService = new CurrencyService(getContext());
                Log.d(TAG, "CurrencyService initialized successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing CurrencyService: " + e.getMessage(), e);
            // Continue without currency service
        }
    }

    private void loadDisplayCurrency() {
        try {
            if (getActivity() != null) {
                SharedPreferences prefs = getActivity().getSharedPreferences("MoneyMatePrefs", 0);
                displayCurrency = prefs.getString("display_currency", "IDR");
                Log.d(TAG, "Display currency loaded: " + displayCurrency);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading display currency: " + e.getMessage(), e);
            displayCurrency = "IDR"; // Default fallback
        }
    }

    private void initViews(View view) {
        try {
            etAmount = view.findViewById(R.id.et_amount);
            etDescription = view.findViewById(R.id.et_description);
            spinnerType = view.findViewById(R.id.spinner_type);
            spinnerCategory = view.findViewById(R.id.spinner_category);
            btnDate = view.findViewById(R.id.btn_date);
            btnSave = view.findViewById(R.id.btn_save);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw e; // Re-throw to prevent further execution
        }
    }

    private void setupSpinners() {
        try {
            // Setup type spinner with error handling
            if (getContext() != null && spinnerType != null) {
                ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.transaction_types, android.R.layout.simple_spinner_item);
                typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerType.setAdapter(typeAdapter);
            }

            // Setup category spinner with error handling
            if (getContext() != null && spinnerCategory != null) {
                ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(getContext(),
                        R.array.categories, android.R.layout.simple_spinner_item);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(categoryAdapter);
            }

            // Set currency hint
            setupCurrencyHint();

            Log.d(TAG, "Spinners setup successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up spinners: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error loading dropdown menus", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCurrencyHint() {
        try {
            if (etAmount != null) {
                String currencyWithCountry = displayCurrency;
                if (currencyService != null) {
                    String[] availableCurrencies = currencyService.getAvailableCurrencies();
                    if (availableCurrencies != null) {
                        for (String currency : availableCurrencies) {
                            if (currency.startsWith(displayCurrency)) {
                                currencyWithCountry = currency;
                                break;
                            }
                        }
                    }
                }
                etAmount.setHint("Jumlah dalam " + currencyWithCountry);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting currency hint: " + e.getMessage(), e);
            if (etAmount != null) {
                etAmount.setHint("Jumlah");
            }
        }
    }

    private void setupDatePicker() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            if (btnDate != null) {
                btnDate.setText(dateFormat.format(selectedDate.getTime()));

                btnDate.setOnClickListener(v -> {
                    try {
                        if (getContext() != null) {
                            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                                    (view, year, month, dayOfMonth) -> {
                                        selectedDate.set(year, month, dayOfMonth);
                                        btnDate.setText(dateFormat.format(selectedDate.getTime()));
                                    },
                                    selectedDate.get(Calendar.YEAR),
                                    selectedDate.get(Calendar.MONTH),
                                    selectedDate.get(Calendar.DAY_OF_MONTH));
                            datePickerDialog.show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error showing date picker: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Error opening date picker", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Log.d(TAG, "Date picker setup successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up date picker: " + e.getMessage(), e);
        }
    }

    private void setupSaveButton() {
        try {
            if (btnSave != null) {
                btnSave.setOnClickListener(v -> saveTransaction());
                Log.d(TAG, "Save button setup successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up save button: " + e.getMessage(), e);
        }
    }

    private void saveTransaction() {
        try {
            // Validate inputs
            if (etAmount == null || etDescription == null || spinnerType == null || spinnerCategory == null) {
                Toast.makeText(getContext(), "Form tidak dapat diakses", Toast.LENGTH_SHORT).show();
                return;
            }

            String amountStr = etAmount.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (amountStr.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Mohon lengkapi semua field", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = spinnerType.getSelectedItem().toString().toLowerCase();
            String category = spinnerCategory.getSelectedItem().toString();

            double amount = Double.parseDouble(amountStr);
            double amountInIDR = amount;

            // Convert currency if needed
            if (currencyService != null && !displayCurrency.equals("IDR")) {
                try {
                    amountInIDR = currencyService.convertCurrency(amount, displayCurrency, "IDR");
                } catch (Exception e) {
                    Log.e(TAG, "Error converting currency: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error converting currency, using original amount", Toast.LENGTH_SHORT).show();
                    amountInIDR = amount;
                }
            }

            // Check balance for expenses
            if (type.equals("expense")) {
                double currentBalance = calculateCurrentBalance();
                if (amountInIDR > currentBalance) {
                    Toast.makeText(getContext(), "Saldo tidak cukup untuk pengeluaran ini", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Save transaction
            if (dbHelper != null) {
                Transaction transaction = new Transaction(0, amountInIDR, description, type, category, selectedDate.getTime());
                long id = dbHelper.addTransaction(transaction);

                if (id > 0) {
                    Toast.makeText(getContext(), "Transaksi berhasil ditambahkan", Toast.LENGTH_SHORT).show();

                    // Clear form
                    clearForm();

                    // Navigate back to home
                    navigateToHome();
                } else {
                    Toast.makeText(getContext(), "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Database tidak tersedia", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Format jumlah tidak valid", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving transaction: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error menyimpan transaksi", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        try {
            if (etAmount != null) etAmount.setText("");
            if (etDescription != null) etDescription.setText("");
            if (spinnerType != null) spinnerType.setSelection(0);
            if (spinnerCategory != null) spinnerCategory.setSelection(0);

            // Reset date to today
            selectedDate = Calendar.getInstance();
            if (btnDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                btnDate.setText(dateFormat.format(selectedDate.getTime()));
            }

            Log.d(TAG, "Form cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing form: " + e.getMessage(), e);
        }
    }

    private void navigateToHome() {
        try {
            if (navController != null) {
                // Navigate directly to home and clear back stack
                navController.navigate(R.id.homeFragment);
                Log.d(TAG, "Navigation to home successful");
            } else {
                Log.e(TAG, "NavController is null, cannot navigate");
                // Fallback: try to find NavController again
                try {
                    if (getActivity() != null) {
                        NavController fallbackNavController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                        fallbackNavController.navigate(R.id.homeFragment);
                    }
                } catch (Exception e2) {
                    Log.e(TAG, "Fallback navigation also failed: " + e2.getMessage(), e2);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to home: " + e.getMessage(), e);
        }
    }

    private double calculateCurrentBalance() {
        try {
            if (dbHelper == null && getContext() != null) {
                dbHelper = new DatabaseHelper(getContext());
            }

            if (dbHelper != null) {
                return dbHelper.getCurrentBalance();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating balance: " + e.getMessage(), e);
        }
        return 0; // Default value if error occurs
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references
        navController = null;
        Log.d(TAG, "Fragment view destroyed");
    }
}
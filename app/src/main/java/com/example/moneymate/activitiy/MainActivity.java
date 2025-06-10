package com.example.moneymate.activitiy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.moneymate.services.CurrencyService;
import com.example.moneymate.database.DatabaseHelper;
import com.example.moneymate.fragment.HomeFragment;
import com.example.moneymate.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DatabaseHelper dbHelper;
    private CurrencyService currencyService;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "MoneyMatePrefs";
    private static final String KEY_THEME = "app_theme";
    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";
    private static final String THEME_SYSTEM = "system";
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // PENTING: Apply theme SEBELUM super.onCreate()
        initializePreferences();
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            initializeComponents();
            setupNavigation();
            setupThemeButton();

            Log.d(TAG, "MainActivity initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializePreferences() {
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void applyTheme() {
        try {
            String savedTheme = prefs.getString(KEY_THEME, THEME_LIGHT);

            switch (savedTheme) {
                case THEME_DARK:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case THEME_SYSTEM:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
                default: // THEME_LIGHT
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
            }

            Log.d(TAG, "Theme applied: " + savedTheme);
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme: " + e.getMessage(), e);
            // Fallback to light theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void initializeComponents() {
        try {
            dbHelper = new DatabaseHelper(this);
            currencyService = new CurrencyService(this);
            Log.d(TAG, "Components initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing components: " + e.getMessage(), e);
        }
    }

    private void setupNavigation() {
        try {
            // Setup NavController
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);

            if (navHostFragment != null) {
                navController = navHostFragment.getNavController();

                // Setup BottomNavigationView
                BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    // Use NavigationUI for automatic navigation setup
                    NavigationUI.setupWithNavController(bottomNav, navController);

                    // Optional: Custom click handling if needed
                    bottomNav.setOnItemSelectedListener(item -> {
                        try {
                            int itemId = item.getItemId();

                            if (itemId == R.id.homeFragment) {
                                navController.navigate(R.id.homeFragment);
                                return true;
                            } else if (itemId == R.id.action_add_transaction) {
                                navController.navigate(R.id.addTransactionFragment);
                                return true;
                            } else if (itemId == R.id.monthlyReportFragment) {
                                navController.navigate(R.id.monthlyReportFragment);
                                return true;
                            }

                            return false;
                        } catch (Exception e) {
                            Log.e(TAG, "Navigation error: " + e.getMessage(), e);
                            return false;
                        }
                    });
                }

                Log.d(TAG, "Navigation setup completed");
            } else {
                Log.e(TAG, "NavHostFragment not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation: " + e.getMessage(), e);
        }
    }

    private void setupThemeButton() {
        try {
            ImageButton btnTheme = findViewById(R.id.btn_theme);
            if (btnTheme != null) {
                btnTheme.setOnClickListener(v -> showThemeDialog());
                Log.d(TAG, "Theme button setup completed");
            } else {
                Log.w(TAG, "Theme button not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up theme button: " + e.getMessage(), e);
        }
    }

    private void showThemeDialog() {
        try {
            String currentTheme = prefs.getString(KEY_THEME, THEME_LIGHT);
            String[] themes = {"Light Theme", "Dark Theme", "System Default"};

            int checkedItem;
            switch (currentTheme) {
                case THEME_DARK:
                    checkedItem = 1;
                    break;
                case THEME_SYSTEM:
                    checkedItem = 2;
                    break;
                default:
                    checkedItem = 0;
                    break;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pilih Tema");
            builder.setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                String newTheme;

                switch (which) {
                    case 1: // Dark
                        newTheme = THEME_DARK;
                        break;
                    case 2: // System
                        newTheme = THEME_SYSTEM;
                        break;
                    default: // Light
                        newTheme = THEME_LIGHT;
                        break;
                }

                // Save preference
                prefs.edit().putString(KEY_THEME, newTheme).apply();

                // Apply theme immediately
                applyTheme();

                Toast.makeText(this, "Tema berhasil diubah", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            builder.setNegativeButton("Batal", null);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing theme dialog: " + e.getMessage(), e);
            Toast.makeText(this, "Error showing theme options", Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshHomeFragment() {
        try {
            // Find current fragment and refresh if it's HomeFragment
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);

            if (navHostFragment != null) {
                androidx.fragment.app.Fragment currentFragment = navHostFragment.getChildFragmentManager()
                        .getPrimaryNavigationFragment();

                if (currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).refreshData();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing home fragment: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (dbHelper != null) {
                dbHelper.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (navController != null && !navController.popBackStack()) {
                super.onBackPressed();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling back press: " + e.getMessage(), e);
            super.onBackPressed();
        }
    }
}
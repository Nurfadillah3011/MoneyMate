package com.example.moneymate.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.moneymate.utils.NetworkUtils;
import com.example.moneymate.models.ExchangeRateResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CurrencyService {
    private static final String TAG = "CurrencyService";
    private static final String PREFS_NAME = "CurrencyPrefs";
    private static final String KEY_RATES = "exchange_rates";
    private static final String KEY_LAST_UPDATE = "last_update";
    private static final long CACHE_DURATION = 3600000; // 1 hour in milliseconds

    private Context context;
    private SharedPreferences prefs;
    private Map<String, Double> exchangeRates;
    private ApiService apiService;

    // Currency names mapping
    private Map<String, String> currencyNames;

    public interface CurrencyCallback {
        void onSuccess(Map<String, Double> rates);
        void onError(String error);
        void onNetworkError(); // New callback for network errors
    }

    public CurrencyService(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.apiService = RetrofitClient.getClient();
        this.exchangeRates = new HashMap<>();
        initializeCurrencyNames();
        loadCachedRates();
    }

    private void initializeCurrencyNames() {
        currencyNames = new HashMap<>();
        currencyNames.put("USD", "USD - Amerika Serikat");
        currencyNames.put("EUR", "EUR - Eropa");
        currencyNames.put("GBP", "GBP - Inggris");
        currencyNames.put("JPY", "JPY - Jepang");
        currencyNames.put("AUD", "AUD - Australia");
        currencyNames.put("CAD", "CAD - Kanada");
        currencyNames.put("CHF", "CHF - Swiss");
        currencyNames.put("CNY", "CNY - China");
        currencyNames.put("SGD", "SGD - Singapura");
        currencyNames.put("KRW", "KRW - Korea Selatan");
        currencyNames.put("THB", "THB - Thailand");
        currencyNames.put("MYR", "MYR - Malaysia");
        currencyNames.put("PHP", "PHP - Filipina");
        currencyNames.put("VND", "VND - Vietnam");
        currencyNames.put("IDR", "IDR - Indonesia");
        currencyNames.put("INR", "INR - India");
        currencyNames.put("HKD", "HKD - Hong Kong");
        currencyNames.put("NZD", "NZD - Selandia Baru");
        currencyNames.put("SEK", "SEK - Swedia");
        currencyNames.put("NOK", "NOK - Norwegia");
    }

    public String[] getAvailableCurrencies() {
        return currencyNames.values().toArray(new String[0]);
    }

    private void loadCachedRates() {
        try {
            long lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0);
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastUpdate < CACHE_DURATION) {
                String ratesJson = prefs.getString(KEY_RATES, "");
                if (!ratesJson.isEmpty()) {
                    Type type = new TypeToken<Map<String, Double>>(){}.getType();
                    Map<String, Double> cachedRates = new Gson().fromJson(ratesJson, type);
                    if (cachedRates != null) {
                        exchangeRates.clear();
                        exchangeRates.putAll(cachedRates);
                        Log.d(TAG, "Loaded cached exchange rates");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading cached rates: " + e.getMessage());
        }
    }

    /**
     * Check if network is available before making API call
     */
    public void getExchangeRates(CurrencyCallback callback) {
        // Check network availability first
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.w(TAG, "No network connection available");

            // Try to use cached rates as fallback
            if (!exchangeRates.isEmpty()) {
                Log.d(TAG, "Using cached rates due to no network");
                callback.onSuccess(exchangeRates);
                return;
            }

            // If no cached rates available, use default rates and notify about network issue
            Map<String, Double> defaultRates = getDefaultRates();
            exchangeRates.putAll(defaultRates);
            callback.onNetworkError(); // New callback for network error
            return;
        }

        Log.d(TAG, "Network available, fetching rates from API");

        // Try to get rates from EUR base (Frankfurter API default)
        apiService.getRates("EUR").enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExchangeRateResponse rateResponse = response.body();
                    Map<String, Double> rates = rateResponse.getRates();

                    // Add EUR as base currency (rate = 1.0)
                    rates.put("EUR", 1.0);

                    // Store rates for conversion
                    exchangeRates.clear();
                    exchangeRates.putAll(rates);

                    // Cache the rates
                    cacheRates(rates);

                    Log.d(TAG, "Successfully loaded " + rates.size() + " exchange rates");
                    callback.onSuccess(rates);
                } else {
                    Log.e(TAG, "API response not successful: " + response.code());
                    handleApiFailure(callback);
                }
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                handleApiFailure(callback);
            }
        });
    }

    /**
     * Handle API failure by using fallback options
     */
    private void handleApiFailure(CurrencyCallback callback) {
        // Try to use cached rates as fallback
        if (!exchangeRates.isEmpty()) {
            Log.d(TAG, "Using cached rates as fallback");
            callback.onSuccess(exchangeRates);
        } else {
            // Check if it's a network issue
            if (!NetworkUtils.isNetworkAvailable(context)) {
                // Provide default rates and notify about network issue
                Map<String, Double> defaultRates = getDefaultRates();
                exchangeRates.putAll(defaultRates);
                callback.onNetworkError();
            } else {
                // Network is available but API failed, use default rates
                Map<String, Double> defaultRates = getDefaultRates();
                exchangeRates.putAll(defaultRates);
                callback.onSuccess(defaultRates);
            }
        }
    }

    private Map<String, Double> getDefaultRates() {
        Map<String, Double> defaultRates = new HashMap<>();
        // These are approximate rates - in production you'd want more accurate fallback data
        defaultRates.put("USD", 1.08);
        defaultRates.put("EUR", 1.0);
        defaultRates.put("GBP", 0.86);
        defaultRates.put("JPY", 162.0);
        defaultRates.put("IDR", 17000.0);
        defaultRates.put("AUD", 1.63);
        defaultRates.put("CAD", 1.47);
        defaultRates.put("CHF", 0.97);
        defaultRates.put("CNY", 7.75);
        defaultRates.put("SGD", 1.45);

        Log.d(TAG, "Using default exchange rates");
        return defaultRates;
    }

    private void cacheRates(Map<String, Double> rates) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(KEY_LAST_UPDATE, System.currentTimeMillis());
            // Serialize rates map to JSON string
            String ratesJson = new Gson().toJson(rates);
            editor.putString(KEY_RATES, ratesJson);
            editor.apply();
            Log.d(TAG, "Exchange rates cached successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error caching rates: " + e.getMessage());
        }
    }

    public double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        try {
            Log.d(TAG, String.format("Converting %.2f from %s to %s", amount, fromCurrency, toCurrency));

            if (fromCurrency.equals(toCurrency)) {
                return amount;
            }

            if (exchangeRates.isEmpty()) {
                Log.w(TAG, "No exchange rates available, using default rates");
                exchangeRates.putAll(getDefaultRates());
            }

            // Get rates relative to EUR (base currency from Frankfurter API)
            Double fromRate = exchangeRates.get(fromCurrency);
            Double toRate = exchangeRates.get(toCurrency);

            if (fromRate == null || toRate == null) {
                Log.e(TAG, String.format("Missing exchange rate: %s=%s, %s=%s",
                        fromCurrency, fromRate, toCurrency, toRate));

                // Fallback: if one of the currencies is missing, return original amount
                return amount;
            }

            // Convert: Amount in FromCurrency -> EUR -> ToCurrency
            // Formula: (amount / fromRate) * toRate
            double result = (amount / fromRate) * toRate;

            Log.d(TAG, String.format("Conversion result: %.2f %s = %.6f %s (rates: %s=%.6f, %s=%.6f)",
                    amount, fromCurrency, result, toCurrency, fromCurrency, fromRate, toCurrency, toRate));

            return result;

        } catch (Exception e) {
            Log.e(TAG, "Error in currency conversion: " + e.getMessage(), e);
            return amount; // Return original amount if conversion fails
        }
    }

    // Method to check if rates are available
    public boolean hasRates() {
        return !exchangeRates.isEmpty();
    }

    // Method to get current exchange rates
    public Map<String, Double> getCurrentRates() {
        return new HashMap<>(exchangeRates);
    }

    /**
     * Check if network is available
     */
    public boolean isNetworkAvailable() {
        return NetworkUtils.isNetworkAvailable(context);
    }

    /**
     * Get network connection type
     */
    public String getNetworkType() {
        return NetworkUtils.getNetworkType(context);
    }
}
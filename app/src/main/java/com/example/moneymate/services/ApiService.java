package com.example.moneymate.services;

import com.example.moneymate.models.ExchangeRateResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// ApiService.java
public interface ApiService {
    @GET("latest")
    Call<ExchangeRateResponse> getRates(@Query("from") String baseCurrency);
}

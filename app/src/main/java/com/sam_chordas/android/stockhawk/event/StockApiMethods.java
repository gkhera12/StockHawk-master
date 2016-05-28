package com.sam_chordas.android.stockhawk.event;

import com.sam_chordas.android.stockhawk.model.StockHistory;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by gkhera on 28/05/16.
 */
public interface StockApiMethods {
    @GET("/")
    void getHistoricalData(@Query("q") String queryString,@Query("format") String format,@Query("diagnostics") String diagnostics,
                           @Query("env")String env, Callback<StockHistory> cb);

}

package com.sam_chordas.android.stockhawk.event;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sam_chordas.android.stockhawk.model.StockHistory;
import com.sam_chordas.android.stockhawk.otto.StockBus;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Subscribe;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.converter.SimpleXMLConverter;

/**
 * Created by gkhera on 28/05/16.
 */
public class EventExecutor {
    private StockApiMethods methods;
    final String STOCK_BASE_URL = "https://query.yahooapis.com/v1/public/yql";
    String format = "json";
    String diagnostics="true";
    String env="store://datatables.org/alltableswithkeys";
    public EventExecutor(Context context){
        StockBus.getInstance().register(this);
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(STOCK_BASE_URL)
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setClient(new OkClient(new OkHttpClient()))
                .build();
        //Implementation using Retrofit
        methods = restAdapter.create(StockApiMethods.class);
    }

    @Subscribe
    public void getHistoricalData(GetDataEvent event){
        methods.getHistoricalData(event.getQuery(),format,diagnostics,env, new Callback<StockHistory>() {
            @Override
            public void success(StockHistory stockHistory, Response response) {
                GetHistoricalResults results = new GetHistoricalResults();
                results.setResults(stockHistory.getQuery().getResults());
                StockBus.getInstance().post(results);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Stock Hawk","Retrofit Error");
            }
        });
    }
}

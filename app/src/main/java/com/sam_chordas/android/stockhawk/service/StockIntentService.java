package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.DisplayErrorEvent;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {
  private Handler mHandler;
  public StockIntentService(){
    super(StockIntentService.class.getName());
    mHandler = new Handler();
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra(getString(R.string.tag)).equals(getString(R.string.add))){
      args.putString(getString(R.string.extra_symbol), intent.getStringExtra(getString(R.string.extra_symbol)));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    int result = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra(getString(R.string.tag)), args));
    if(result == GcmNetworkManager.RESULT_FAILURE){
      mHandler.post(new DisplayErrorEvent(this,getString(R.string.stock_symbol_not_found)));
    }
  }
}

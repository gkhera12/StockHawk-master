package com.sam_chordas.android.stockhawk.otto;

import android.app.Activity;
import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by gkhera on 28/05/16.
 */
public class StockBus extends Bus {
    private static class StockBusHolder {
        private static StockBus STOCK_BUS = new StockBus(ThreadEnforcer.ANY);
    }
    public StockBus (ThreadEnforcer enforcer){super (enforcer);}

    public static StockBus getInstance(){ return StockBusHolder.STOCK_BUS;}
    public static void postOnUiThread(Context context, final Object event){
        Activity activity = (Activity)context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getInstance().post(event);
            }
        });
    }
}

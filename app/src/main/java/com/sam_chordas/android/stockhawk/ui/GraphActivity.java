package com.sam_chordas.android.stockhawk.ui;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.event.GetDataEvent;
import com.sam_chordas.android.stockhawk.event.GetHistoricalResults;
import com.sam_chordas.android.stockhawk.model.Quote;
import com.sam_chordas.android.stockhawk.model.Url;
import com.sam_chordas.android.stockhawk.otto.StockBus;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.otto.Subscribe;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit.RetrofitError;

import static java.net.URLEncoder.encode;

public class GraphActivity extends AppCompatActivity {
    private Context mContext;
    private LineChartView mChartView;
    private TextView symbol;
    private TextView currentPrice;
    private Tooltip mTip;
    private List<Quote> data;
    private static int DEFAULT_DURATION = 200;
    public static String EXTRA_SYMBOL = "symbol";
    private static int Y_DIMEN = 25;
    private static int X_DIMEN = 95;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_line_graph);
        mChartView = (LineChartView)findViewById(R.id.linechart);
        symbol = (TextView)findViewById(R.id.stock_symbol);
        currentPrice = (TextView)findViewById(R.id.bid_price);
        String symbolName = getIntent().getStringExtra(EXTRA_SYMBOL);
        getHistoricalData(symbolName);
        mTip = new Tooltip(mContext, R.layout.line_chart_tooltip, R.id.value);
        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(X_DIMEN), (int) Tools.fromDpToPx(Y_DIMEN));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(DEFAULT_DURATION);

            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(DEFAULT_DURATION);

            mTip.setPivotX(Tools.fromDpToPx(X_DIMEN) / 2);
            mTip.setPivotY(Tools.fromDpToPx(Y_DIMEN));
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        StockBus.getInstance().register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        StockBus.getInstance().unregister(this);
    }

    private void getHistoricalData(String symbol) {
        if (!Utils.isNetworkAvailable(mContext)){
            Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
            updateEmptyView(RetrofitError.Kind.NETWORK);
        } else{
            GetDataEvent event = new GetDataEvent();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH,-1);
            String startDate = dateFormat.format(calendar.getTime());
            String endDate = dateFormat.format(new Date());
            event.setQuery("select * from yahoo.finance.historicaldata where symbol = \"" +
                    symbol+"\" and startDate = \""+startDate+"\" and endDate = \""+endDate+"\"");
            StockBus.getInstance().post(event);
        }
    }

    @Subscribe
    public void getHistoricalResults(GetHistoricalResults results){
        LineSet dataset = new LineSet();
        String bidPrice="";
        int maxPrice ;
        int minPrice ;
        if(results.getError() != null){
            Log.i("StockHawk",results.getError().name());
            updateEmptyView(results.getError());
        }
        if(results.getResults() != null ) {
            data = results.getResults().getQuote();
            try {
                bidPrice = Utils.truncateBidPrice(encode(data.get(0).getClose(),"utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            currentPrice.setText(bidPrice);
            currentPrice.setContentDescription(mContext.getString(R.string.a11y_stock_price, bidPrice));
            symbol.setText(data.get(0).getSymbol());
            symbol.setContentDescription(mContext.getString(R.string.a11y_stock, data.get(0).getSymbol()));
            minPrice = maxPrice = Math.round(Float.valueOf(bidPrice));
            for (int i = data.size() - 1; i >= 0; i--) {
                String date = "";
                if (i % 5 == 0) {
                    date = data.get(i).getDate();
                }
                float value = Float.valueOf(data.get(i).getClose());
                if (value > maxPrice) {
                    maxPrice = (int)value;
                }
                if (value <= minPrice) {
                    minPrice = (int)value;
                }
                dataset.addPoint(date,value);
            }
            minPrice = minPrice - 1;
            maxPrice = (maxPrice + 1);
            int stepsize = (maxPrice - minPrice) / 10 + 1;
            dataset.setColor(Color.parseColor("#6a84c3"))
                    .setSmooth(true)
                    .setThickness(4)
                    .endAt(dataset.size());
            mChartView.addData(dataset);
            mChartView.setBorderSpacing(Tools.fromDpToPx(15))
                    .setAxisBorderValues(minPrice,maxPrice)
                    .setYLabels(AxisController.LabelPosition.OUTSIDE)
                    .setLabelsColor(Color.parseColor("#6a84c3"))
                    .setXAxis(true)
                    .setXLabels(AxisController.LabelPosition.OUTSIDE)
                    .setStep(stepsize)
                    .setYAxis(true);
            mChartView.setTooltips(mTip);
            mChartView.show();
            mChartView.setContentDescription(mContext.getString(R.string.a11y_stock_chart, data.get(0).getSymbol()));
            mChartView.setOnEntryClickListener(new OnEntryClickListener(){
                @Override
                public void onClick(int setIndex, int entryIndex, Rect entryRect) {
                    mChartView.dismissAllTooltips();
                    mTip.prepare(entryRect, Float.valueOf(data.get(entryIndex).getClose()));
                    mChartView.showTooltip(mTip,true);
                    mChartView.setContentDescription(mContext.getString(R.string.a11y_stock_price,
                            data.get(entryIndex).getClose()));
                }
            });

        }
    }

    private void updateEmptyView(RetrofitError.Kind error) {
        TextView emptyTextView = (TextView)findViewById(R.id.graph_stock_empty);
        if(emptyTextView != null){
            String message = mContext.getString(R.string.empty_stock_list);
            switch (error){
                case NETWORK:
                    message = mContext.getString(R.string.empty_stock_list_no_network);
                    break;
                case HTTP:
                    message = mContext.getString(R.string.empty_stock_list_server_down);
                    break;
             }
            emptyTextView.setText(message);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }
}

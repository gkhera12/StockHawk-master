package com.sam_chordas.android.stockhawk.ui;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
import com.sam_chordas.android.stockhawk.otto.StockBus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GraphActivity extends AppCompatActivity {
    private Context mContext;
    private LineChartView mChartView;
    private TextView symbol;
    private TextView currentPrice;
    private Tooltip mTip;
    private List<Quote> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        mChartView = (LineChartView)findViewById(R.id.linechart);
        symbol = (TextView)findViewById(R.id.stock_symbol);
        currentPrice = (TextView)findViewById(R.id.bid_price);
        String symbolName = getIntent().getStringExtra("symbol");
        mContext = getApplicationContext();
        getHistoricalData(symbolName);
        mTip = new Tooltip(mContext, R.layout.line_chart_tooltip, R.id.value);

        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(95), (int) Tools.fromDpToPx(25));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

            mTip.setPivotX(Tools.fromDpToPx(95) / 2);
            mTip.setPivotY(Tools.fromDpToPx(25));
        }
        mChartView.setTooltips(mTip);
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
        GetDataEvent event = new GetDataEvent();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,-1);
        String startDate = dateFormat.format(calendar.getTime());
        String endDate = dateFormat.format(new Date());
        event.setQuery("select * from yahoo.finance.historicaldata where symbol = \"" +
                symbol+"\" and startDate = \""+startDate+"\" and endDate = \""+endDate+"\"");
        StockBus.getInstance().post(event);
    }

    @Subscribe
    public void getHistoricalResults(GetHistoricalResults results){
        Log.i("StockHawk","Got Results in Graph Activity");

        LineSet dataset = new LineSet();
        float bidPrice=0;
        int maxPrice ;
        int minPrice ;
        if(results != null ) {
            data = results.getResults().getQuote();
            bidPrice = Float.valueOf(data.get(0).getClose());
            currentPrice.setText(data.get(0).getClose());
            currentPrice.setContentDescription(mContext.getString(R.string.a11y_stock_price,data.get(0).getClose()));
            symbol.setText(data.get(0).getSymbol());
            symbol.setContentDescription(mContext.getString(R.string.a11y_stock,data.get(0).getSymbol()));
            minPrice = maxPrice = (int) bidPrice;
            for (int i = data.size()-1;i>=0; i--) {
                String date="";
                if(i%5 == 0){
                    date = data.get(i).getDate();
                }
                bidPrice = Float.valueOf(data.get(i).getClose());
                if (bidPrice > maxPrice) {
                    maxPrice = (int) bidPrice;
                }
                if (bidPrice <= minPrice) {
                    minPrice = (int) bidPrice;
                }
                dataset.addPoint(date, bidPrice);
            }
            minPrice = minPrice - 1;
            maxPrice = (maxPrice + 1);
            int stepsize = (maxPrice - minPrice) /10+1;
            dataset.setColor(Color.parseColor("#6a84c3"))
                    .setSmooth(true)
                    .setThickness(4)
                    .endAt(dataset.size());
            mChartView.addData(dataset);
            mChartView.setBorderSpacing(Tools.fromDpToPx(15))
                    .setAxisBorderValues(minPrice, maxPrice)
                    .setYLabels(AxisController.LabelPosition.OUTSIDE)
                    .setLabelsColor(Color.parseColor("#6a84c3"))
                    .setXAxis(true)
                    .setXLabels(AxisController.LabelPosition.OUTSIDE)
                    .setStep(stepsize)
                    .setYAxis(true);
            mChartView.show();
            mChartView.setContentDescription(mContext.getString(R.string.a11y_stock_chart,data.get(0).getSymbol()));

        }
    }
}

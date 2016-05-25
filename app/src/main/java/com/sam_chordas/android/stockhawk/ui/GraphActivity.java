package com.sam_chordas.android.stockhawk.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.rest.Utils;

public class GraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int DETAIL_LOADER = 0;
    private Context mContext;
    private static final String[] DETAIL_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CREATED,
            QuoteColumns.ISUP,
            QuoteColumns.ISCURRENT
    };

    public static final int COL_QUOTE_ID = 0;
    public static final int COL_QUOTE_SYMBOL = 1;
    public static final int COL_QUOTE_PERCENT_CHANGE = 2;
    public static final int COL_QUOTE_CHANGE = 3;
    public static final int COL_QUOTE_BIDPRICE = 4;
    public static final int COL_QUOTE_CREATED = 5;
    public static final int COL_QUOTE_ISUP = 6;
    public static final int COL_QUOTE_ISCURRENT = 7;
    private LineChartView mChartView;
    private Uri mUri;
    private TextView symbol;
    private TextView change;
    private TextView currentPrice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        mChartView = (LineChartView)findViewById(R.id.linechart);
        symbol = (TextView)findViewById(R.id.stock_symbol);
        change = (TextView)findViewById(R.id.change);
        currentPrice = (TextView)findViewById(R.id.bid_price);
        mUri = getIntent().getData();
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        mContext = getApplicationContext();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if(null != mUri){
            return new CursorLoader(
                    this,
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LineSet dataset = new LineSet();
        float bidPrice=0;
        int maxPrice ;
        int minPrice ;
        if(data !=null && data.moveToFirst()) {
            bidPrice = Float.valueOf(data.getString(COL_QUOTE_BIDPRICE));
            minPrice = maxPrice = (int)bidPrice;
            dataset.addPoint("", bidPrice);
            while(data.moveToNext()) {
                if(data.getInt(COL_QUOTE_ISCURRENT) == 1){
                    setTextViews(data);
                }
                bidPrice = Float.valueOf(data.getString(COL_QUOTE_BIDPRICE));
                if(bidPrice >maxPrice){
                    maxPrice = (int)bidPrice;
                }
                if(bidPrice <= minPrice){
                    minPrice = (int)bidPrice;
                }
                dataset.addPoint("", bidPrice);
            }
            minPrice = minPrice-5;
            maxPrice = (maxPrice+5);
            int stepsize = 5;
            dataset.setColor(Color.parseColor("#6a84c3"))
                    .setSmooth(true)
                    .setThickness(4)
                    .endAt(data.getCount());
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
        }
    }


    @Override
    public void onLoaderReset(Loader loader) {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setTextViews(Cursor data) {
        String symbolName = data.getString(COL_QUOTE_SYMBOL);
        String changePrice = data.getString(COL_QUOTE_CHANGE);
        String changePercent = data.getString(COL_QUOTE_PERCENT_CHANGE);
        int isUp = data.getInt(COL_QUOTE_ISUP);
        currentPrice.setText(data.getString(COL_QUOTE_BIDPRICE));
        currentPrice.setContentDescription(mContext.getString(R.string.a11y_stock_price,currentPrice));
        symbol.setText(symbolName);
        symbol.setContentDescription(getApplicationContext().getString(R.string.a11y_stock,symbolName));
        int sdk = Build.VERSION.SDK_INT;
        if (isUp == 1){
            if (sdk < Build.VERSION_CODES.JELLY_BEAN){
                change.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }else {
                change.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }
        } else{
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                change.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            } else{
                change.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            }
        }
        if (Utils.showPercent){
            change.setText(changePercent);
            change.setContentDescription(mContext.getString(R.string.a11y_stock_percent_change,changePercent));
        } else{
            change.setText(changePrice);
            change.setContentDescription(mContext.getString(R.string.a11y_stock_change,changePrice));
        }
    }
}

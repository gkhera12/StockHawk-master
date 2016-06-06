package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.GraphActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gkhera on 31/05/16.
 */
public class StockWidgetService extends RemoteViewsService {
    public final String LOG_TAG = StockWidgetService.class.getSimpleName();
    // these indices must match the projection
    static final int INDEX_STOCK_ID = 0;
    static final int INDEX_STOCK_SYMBOL = 1;
    static final int INDEX_STOCK_PERCENT_CHANGE = 2;
    static final int INDEX_STOCK_CHANGE = 3;
    static final int INDEX_STOCK_BIDPRICE = 4;
    static final int INDEX_STOCK_CREATED = 5;
    static final int INDEX_STOCK_IS_UP = 6;
    static final int INDEX_STOCK_IS_CURRENT = 6;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.PERCENT_CHANGE,
                                QuoteColumns.CHANGE, QuoteColumns.BIDPRICE, QuoteColumns.ISUP},
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                Context context = getApplicationContext();
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.stock_widget_list_item);
                String symbolName = data.getString(INDEX_STOCK_SYMBOL);
                views.setTextViewText(R.id.stock_symbol,symbolName);
                views.setContentDescription(R.id.change,context.getString(R.string.a11y_stock,symbolName));
                String bidPrice = data.getString(INDEX_STOCK_BIDPRICE);
                views.setTextViewText(R.id.bid_price,bidPrice);
                views.setContentDescription(R.id.change,context.getString(R.string.a11y_stock_price,bidPrice));
                String change;
                if (Utils.showPercent){
                    change = data.getString(INDEX_STOCK_PERCENT_CHANGE);
                    views.setTextViewText(R.id.change,change);
                } else{
                    change = data.getString(INDEX_STOCK_CHANGE);
                    views.setTextViewText(R.id.change, change);
                }
                views.setContentDescription(R.id.change,context.getString(R.string.a11y_stock_percent_change,change));

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(GraphActivity.EXTRA_SYMBOL,symbolName);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.stock_widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_STOCK_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}

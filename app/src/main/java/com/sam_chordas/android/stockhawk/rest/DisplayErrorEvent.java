package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by gkhera on 29/05/16.
 */
public class DisplayErrorEvent implements Runnable {
    private final Context mContext;
    String mText;

    public DisplayErrorEvent(Context mContext, String text){
        this.mContext = mContext;
        mText = text;
    }

    public void run(){
        Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
    }
}

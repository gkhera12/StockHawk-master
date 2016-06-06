package com.sam_chordas.android.stockhawk.event;

/**
 * Created by gkhera on 28/05/16.
 */
public class GetDataEvent {
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    private String query;
}

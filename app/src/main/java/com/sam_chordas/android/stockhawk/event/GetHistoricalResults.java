package com.sam_chordas.android.stockhawk.event;

import com.sam_chordas.android.stockhawk.model.Results;

/**
 * Created by gkhera on 28/05/16.
 */
public class GetHistoricalResults {
    private Results results;
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }

}

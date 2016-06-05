package com.sam_chordas.android.stockhawk.event;

import com.sam_chordas.android.stockhawk.model.Results;

import retrofit.RetrofitError;

/**
 * Created by gkhera on 28/05/16.
 */
public class GetHistoricalResults {
    private Results results;
    private RetrofitError.Kind error;

    public RetrofitError.Kind getError() {
        return error;
    }

    public void setError(RetrofitError.Kind error) {
        this.error = error;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }

}

package com.okeefe.peter.movieapp.detailloader;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.okeefe.peter.movieapp.data.MoviePosterDataContract;

/**
 * Created by peterokeefe on 8/27/15.
 */
public class AsyncUpdateFavoritesTask extends AsyncTask<String, Void, Boolean> {

    public AsyncUpdateFavoritesTask(ContentResolver mContentResolver) {
        super();
        this.mContentResolver = mContentResolver;
    }

    private ContentResolver mContentResolver;


    @Override
    protected Boolean doInBackground(String... params) {

        Uri u = Uri.parse(params[0]);
        boolean favorited = Boolean.valueOf(params[1]);
        ContentValues cv = new ContentValues();
        cv.put(MoviePosterDataContract.MovieEntry.FAVORITE, favorited ? 1 : 0);
        mContentResolver.update(u, cv, null, null);

        return true;
    }
}
